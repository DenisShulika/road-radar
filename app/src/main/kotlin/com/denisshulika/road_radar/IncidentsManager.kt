package com.denisshulika.road_radar

import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denisshulika.road_radar.model.IncidentType
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class IncidentsManager(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>()

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _incidentCreationState =
        MutableLiveData<IncidentCreationState>(IncidentCreationState.Idle)
    val incidentCreationState: LiveData<IncidentCreationState> = _incidentCreationState

    private val _loadingDocumentsState =
        MutableLiveData<LoadingDocumentsState>(LoadingDocumentsState.Idle)
    val loadingDocumentsState: LiveData<LoadingDocumentsState> = _loadingDocumentsState

    private val _incidents = MutableLiveData<List<Incident>>(emptyList())
    val incidents: LiveData<List<Incident>> = _incidents

    private val _selectedDocumentInfo = MutableLiveData<Incident>()
    val selectedDocumentInfo: LiveData<Incident> = _selectedDocumentInfo

    fun setIncidentCreationState(state: IncidentCreationState) {
        _incidentCreationState.value = state
    }

    fun setSelectedDocumentInfo(selectedDocumentInfo: Incident) {
        _selectedDocumentInfo.value = selectedDocumentInfo
    }

    fun resetDocumentList() {
        _incidents.value = emptyList()
    }

    private var listenerRegistration: ListenerRegistration? = null

    fun deleteOldIncidents(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            db.collection("incidents")
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val lifetimeTimestamp = document.getTimestamp("lifetime")?.seconds ?: 0L
                        val currentTime = System.currentTimeMillis()

                        if (lifetimeTimestamp * 1000 <= currentTime) {
                            val id = document.id
                            val documentRef = db.collection("incidents").document(id)

                            documentRef.collection("comments")
                                .get()
                                .addOnSuccessListener { commentsSnapshot ->
                                    val deleteCommentsTasks = commentsSnapshot.documents.map { it.reference.delete() }

                                    Tasks.whenAllSuccess<Void>(deleteCommentsTasks)
                                        .addOnSuccessListener {
                                            documentRef.collection("commentAuthors")
                                                .get()
                                                .addOnSuccessListener { authorsSnapshot ->
                                                    val deleteAuthorsTasks = authorsSnapshot.documents.map { it.reference.delete() }

                                                    Tasks.whenAllSuccess<Void>(deleteAuthorsTasks)
                                                        .addOnSuccessListener {
                                                            documentRef.delete()
                                                                .addOnSuccessListener {
                                                                    val storageReference = storage.reference.child("/incidents_photos/incident_$id")
                                                                    storageReference.listAll()
                                                                        .addOnSuccessListener { listResult ->
                                                                            listResult.items.forEach { file ->
                                                                                file.delete()
                                                                            }
                                                                        }
                                                                        .addOnFailureListener { e ->
                                                                            Toast.makeText(
                                                                                context,
                                                                                "Error deleting photos for incident $id: ${e.message}",
                                                                                Toast.LENGTH_SHORT
                                                                            ).show()
                                                                        }
                                                                }
                                                                .addOnFailureListener { e ->
                                                                    Toast.makeText(
                                                                        context,
                                                                        "Error deleting incident $id: ${e.message}",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                        }
                                                        .addOnFailureListener { e ->
                                                            Toast.makeText(
                                                                context,
                                                                "Error deleting comment authors for incident $id: ${e.message}",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(
                                                        context,
                                                        "Error fetching comment authors for incident $id: ${e.message}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(
                                                context,
                                                "Error deleting comments for incident $id: ${e.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        context,
                                        "Error fetching comments for incident $id: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        context,
                        "Error fetching incidents: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    fun addNewIncident(
        authViewModel: AuthViewModel,
        commentManager: CommentManager,
        localization: Map<String, String>,
        context: Context,
        photoUris: List<Uri?>,
        type: IncidentType,
        description: String,
        address: String,
        latitude: String,
        longitude: String
    ) {
        _incidentCreationState.value = IncidentCreationState.Loading

        CoroutineScope(Dispatchers.Main).launch {
            val incidentsSnapshot = try {
                db.collection("incidents").get().await()
            } catch (e: Exception) {
                _incidentCreationState.value = IncidentCreationState.Error(
                    e.message ?: localization["incident_adding_fail"] ?: "Something went wrong"
                )
                return@launch
            }

            val currentUser = authViewModel.getCurrentUser()!!
            val currentUserId = currentUser.uid

            val currentTime = Timestamp.now()

            val newLatitude = latitude.toDouble()
            val newLongitude = longitude.toDouble()

            for (document in incidentsSnapshot.documents) {
                val reporters = document.get("reporters")?.let {
                    (it as? List<*>)?.filterIsInstance<String>()
                } ?: emptyList()

                if (currentUserId in reporters) {
                    _incidentCreationState.value = IncidentCreationState.Error(localization["incident_was_reported"]!!)
                    return@launch
                }

                val storedLatitude = document.getString("latitude")?.toDouble()!!
                val storedLongitude = document.getString("longitude")?.toDouble()!!

                if (calculateDistance(
                        storedLatitude,
                        storedLongitude,
                        newLatitude,
                        newLongitude
                    ) * 1000 < 100
                ) {
                    val updatedData = mapOf(
                        "lifetime" to Timestamp(
                            currentTime.seconds + 30 * 60,
                            currentTime.nanoseconds
                        ),
                        "reporters" to reporters + currentUserId
                    )

                    db.collection("incidents")
                        .document(document.id)
                        .update(updatedData)
                        .addOnSuccessListener {
                            db.collection("users")
                                .document(currentUserId)
                                .get()
                                .addOnSuccessListener { documentSnapshot ->
                                    val currentExperience = documentSnapshot.getLong("experience") ?: 0
                                    val currentReportsCount = documentSnapshot.getLong("reportsCount") ?: 0

                                    val data = mapOf(
                                        "experience" to currentExperience + 1,
                                        "reportsCount" to currentReportsCount + 1
                                    )
                                    db.collection("users")
                                        .document(currentUserId)
                                        .update(data)
                                        .addOnSuccessListener {

                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(context, e.localizedMessage, Toast.LENGTH_LONG).show()
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, e.localizedMessage, Toast.LENGTH_LONG).show()
                                }

                            commentManager.addComment(
                                comment = Comment(
                                    incidentId = document.id,
                                    authorId = currentUser.uid,
                                    text = "<system_report_duplicate>",
                                    systemComment = true
                                ),
                                photoUris = emptyList(),
                                localization = localization,
                            )
                            if (description.isNotEmpty() || photoUris.isNotEmpty()) {
                                incrementCommentCount(document.id, 2)
                                commentManager.addComment(
                                    comment = Comment(
                                        incidentId = document.id,
                                        authorId = currentUser.uid,
                                        text = description
                                    ),
                                    photoUris = if (photoUris.isNotEmpty()) photoUris.filterNotNull() else emptyList(),
                                    localization = localization,
                                )
                            } else {
                                incrementCommentCount(document.id, 1)
                            }
                            _incidentCreationState.value = IncidentCreationState.Success
                            Toast.makeText(
                                context,
                                localization["incident_extended"]!!,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            _incidentCreationState.value = IncidentCreationState.Error(
                                e.message ?: "Failed to update incident"
                            )
                        }

                    return@launch
                }
            }

            val incidentID = UUID.randomUUID().toString()
            val photos: MutableList<String> = mutableListOf()
            val storageReference = storage.reference.child("incidents_photos/incident_$incidentID")

            val uploadTasks = photoUris.mapIndexedNotNull { index, uri ->
                uri?.let {
                    val photoRef = storageReference.child("photo_$index")
                    photoRef.putFile(it)
                        .continueWithTask { task ->
                            if (!task.isSuccessful) {
                                throw task.exception ?: Exception(localization["unknown_error"]!!)
                            }
                            photoRef.downloadUrl
                        }
                }
            }

            try {
                _incidentCreationState.value = IncidentCreationState.UploadingPhotos
                val downloadUrls = Tasks.whenAllSuccess<Uri>(uploadTasks).await()
                photos.addAll(downloadUrls.map { it.toString() })

                val creatorName =
                    authViewModel.getCurrentUser()?.displayName ?: localization["unknown_user"]!!

                val incident = Incident(
                    id = incidentID,
                    type = type.toString(),
                    address = address,
                    commentCount = 0,
                    createdBy = creatorName,
                    creationDate = currentTime,
                    lifetime = Timestamp(currentTime.seconds + 30 * 60, currentTime.nanoseconds),
                    description = description,
                    latitude = newLatitude.toString(),
                    longitude = newLongitude.toString(),
                    photos = photos,
                    usersLiked = listOf(currentUserId),
                    reporters = listOf(currentUserId),
                    authors = emptyList()
                )

                db.collection("users")
                    .document(currentUserId)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        val currentExperience = documentSnapshot.getLong("experience") ?: 0
                        val currentReportsCount = documentSnapshot.getLong("reportsCount") ?: 0

                        val data = mapOf(
                            "experience" to currentExperience + 2,
                            "reportsCount" to currentReportsCount + 1
                        )
                        db.collection("users")
                            .document(currentUserId)
                            .update(data)
                            .addOnSuccessListener {

                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, e.localizedMessage, Toast.LENGTH_LONG).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, e.localizedMessage, Toast.LENGTH_LONG).show()
                    }

                _incidentCreationState.value = IncidentCreationState.CreatingIncident

                db.collection("incidents").document(incidentID).set(incident)
                    .addOnSuccessListener {
                        _incidentCreationState.value = IncidentCreationState.Success
                        Toast.makeText(
                            context,
                            localization["incident_adding_success"]!!,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        _incidentCreationState.value =
                            IncidentCreationState.Error(e.message ?: localization["incident_adding_fail"]!!)
                    }

            } catch (e: Exception) {
                _incidentCreationState.value = IncidentCreationState.Error(
                    e.message ?: localization["photos_adding_fail"]!!
                )
            }
        }
    }

    fun incrementCommentCount(incidentId: String, incrementBy: Int) {
        db.collection("incidents")
            .document(incidentId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val currentCommentCount = documentSnapshot.getLong("commentCount") ?: 0

                FirebaseFirestore.getInstance().collection("incidents")
                    .document(incidentId)
                    .update("commentCount", currentCommentCount + incrementBy)
                    .addOnSuccessListener {

                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            context,
                            e.localizedMessage,
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    context,
                    e.localizedMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    fun startListeningIncidents(
        usersLat: Double,
        usersLng: Double,
        radiusToShow: Double,
        localization: Map<String, String>
    ) {
        if (listenerRegistration != null) return

        _loadingDocumentsState.value = LoadingDocumentsState.Loading
        listenerRegistration?.remove()

        listenerRegistration = db.collection("incidents")
            .orderBy("creationDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    _loadingDocumentsState.value = LoadingDocumentsState.Error(e?.message ?: localization["incidents_loading_fail"] ?: localization["something_went_wrong"] ?: "Something went wrong")
                    return@addSnapshotListener
                }

                val incidentList = snapshot.documents.mapNotNull { document ->
                    val lifetimeTimestamp = document.getTimestamp("lifetime")?.seconds ?: 0L

                    if (System.currentTimeMillis() < lifetimeTimestamp * 1000 && calculateDistance(document.getString("latitude")!!.toDouble(), document.getString("longitude")!!.toDouble(), usersLat,usersLng) <= radiusToShow) {
                        Incident.fromDocument(document)
                    } else null
                }
                _incidents.value = incidentList
                _loadingDocumentsState.value = LoadingDocumentsState.Success
            }
    }

    fun stopListeningIncidents() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }


    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    fun updateUserThanksGivenCount(userId: String, incidentId: String) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val currentThanksGivenCount = documentSnapshot.getLong("thanksGivenCount") ?: 0
                db.collection("users")
                    .document(userId)
                    .update("thanksGivenCount", currentThanksGivenCount + 1)
                    .addOnSuccessListener {
                        updateReportersThanksCount(incidentId)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, e.localizedMessage, Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, e.localizedMessage, Toast.LENGTH_LONG).show()
            }
    }

    private fun updateReportersThanksCount(incidentId: String) {
        db.collection("incidents")
            .document(incidentId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val reporters = documentSnapshot.get("reporters")?.let {
                    (it as? List<*>)?.filterIsInstance<String>()
                } ?: emptyList()

                if (reporters.isEmpty()) return@addOnSuccessListener

                val batch = db.batch()
                val tasks = reporters.map { reporterId ->
                    db.collection("users").document(reporterId).get()
                }

                Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            val userRef = document.reference
                            val currentThanksCount = document.getLong("thanksCount") ?: 0
                            val currentExperience = document.getLong("experience") ?: 0
                            batch.update(userRef, mapOf(
                                "thanksCount" to currentThanksCount + 1,
                                "experience" to currentExperience + 1
                            ))
                        }
                        batch.commit().addOnFailureListener { e ->
                            Toast.makeText(context, e.localizedMessage, Toast.LENGTH_LONG).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, e.localizedMessage, Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, e.localizedMessage, Toast.LENGTH_LONG).show()
            }
    }

    fun addUserLike(incidentId: String, userId: String) {
        val incidentRef = db.collection("incidents").document(incidentId)
        incidentRef.update("usersLiked", FieldValue.arrayUnion(userId))
    }
}

data class Incident(
    val id: String = "",
    val type: String = "",
    val address: String = "",
    val commentCount: Int = 0,
    val createdBy: String = "",
    val creationDate: Timestamp = Timestamp(Date()),
    val lifetime: Timestamp = Timestamp(Date()),
    val description: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val photos: List<String> = emptyList(),
    var usersLiked: List<String> = emptyList(),
    var reporters: List<String> = emptyList(),
    var authors: List<String> = emptyList()
) {
    companion object {
        fun fromDocument(doc: DocumentSnapshot): Incident? {
            return try {
                Incident(
                    id = doc.id,
                    latitude = doc.getString("latitude")!!,
                    longitude = doc.getString("longitude")!!,
                    creationDate = doc.getTimestamp("creationDate")!!,
                    lifetime = doc.getTimestamp("lifetime")!!,
                    type = doc.getString("type")!!,
                    address = doc.getString("address")!!,
                    description = doc.getString("description")!!,
                    photos = (doc.get("photos") as? List<*>)?.filterIsInstance<String>()?.takeIf { it.isNotEmpty() } ?: emptyList(),
                    commentCount = doc.getLong("commentCount")!!.toInt(),
                    createdBy = doc.getString("createdBy")!!,
                    usersLiked = (doc.get("usersLiked") as? List<*>)?.filterIsInstance<String>()?.takeIf { it.isNotEmpty() } ?: emptyList(),
                    authors = (doc.get("authros") as? List<*>)?.filterIsInstance<String>()?.takeIf { it.isNotEmpty() } ?: emptyList(),
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

sealed class IncidentCreationState {
    data object Success : IncidentCreationState()
    data object Loading : IncidentCreationState()
    data object UploadingPhotos : IncidentCreationState()
    data object CreatingIncident : IncidentCreationState()
    data object Idle : IncidentCreationState()
    data class Error(val message: String) : IncidentCreationState()
}

sealed class LoadingDocumentsState {
    data object Success : LoadingDocumentsState()
    data object Loading : LoadingDocumentsState()
    data object Idle : LoadingDocumentsState()
    data class Error(val message: String) : LoadingDocumentsState()
}