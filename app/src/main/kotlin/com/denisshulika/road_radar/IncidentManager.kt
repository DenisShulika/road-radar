package com.denisshulika.road_radar

import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denisshulika.road_radar.model.IncidentInfo
import com.denisshulika.road_radar.model.IncidentType
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

class IncidentManager(application: Application) : AndroidViewModel(application) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _incidentCreationState = MutableLiveData<IncidentCreationState>(IncidentCreationState.Null)
    val incidentCreationState: LiveData<IncidentCreationState> = _incidentCreationState

    private val _loadingDocumentsState = MutableLiveData<LoadingDocumentsState>(LoadingDocumentsState.Null)
    val loadingDocumentsState: LiveData<LoadingDocumentsState> = _loadingDocumentsState

    private val _documentsList = MutableLiveData<List<DocumentSnapshot>>(emptyList())
    val documentsList: LiveData<List<DocumentSnapshot>> = _documentsList

    private val _selectedDocumentInfo = MutableLiveData<IncidentInfo>()
    val selectedDocumentInfo: LiveData<IncidentInfo> = _selectedDocumentInfo

    private val _userRegion = MutableLiveData("")
    val userRegion: LiveData<String> = _userRegion

    fun setIncidentCreationState(state: IncidentCreationState){
        _incidentCreationState.value = state
    }

    fun setLoadingDocumentsState(state: LoadingDocumentsState){
        _loadingDocumentsState.value = state
    }

    fun setSelectedDocumentInfo(selectedDocumentInfo: IncidentInfo) {
        _selectedDocumentInfo.value = selectedDocumentInfo
    }

    fun resetDocumentList() {
        _documentsList.value = emptyList()
    }

    fun setUserRegion(region: String) {
        _userRegion.value = region
    }

    fun deleteOldIncidents() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentTime = System.currentTimeMillis()
            val threeHoursAgo = Timestamp(Date(currentTime - 3 * 60 * 60 * 1000))

            db.collection("incidents")
                .whereLessThan("creationDate", threeHoursAgo)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val id = document.id
                        val documentRef = db.collection("incidents").document(id)
                        documentRef.delete()
                            .addOnSuccessListener {
                                val storageReference = storage.reference.child("/incidents_photos/incident_$id")

                                storageReference.listAll()
                                    .addOnSuccessListener { listResult ->
                                        listResult.items.forEach { file ->
                                            file.delete()
                                        }
                                    }
                            }

                    }
                }
        }
    }

    fun addNewIncident(
        authViewModel: AuthViewModel,
        localization: Map<String, String>,
        context: Context,
        photoUris: List<Uri?>,
        type: IncidentType,
        description: String,
        region: String,
        address: String,
        latitude: String,
        longitude: String
    ) {
        _incidentCreationState.value = IncidentCreationState.Loading

        CoroutineScope(Dispatchers.Main).launch {
            val creatorName = authViewModel.getCurrentUser()?.displayName ?: localization["unknown_user"] ?: "Unknown user"
            val currentTime = Timestamp.now()

            val incidentID = UUID.randomUUID().toString()
            val photos: MutableList<String> = mutableListOf()

            _incidentCreationState.value = IncidentCreationState.UploadingPhotos
            val storageReference = storage.reference.child("incidents_photos/${"incident_$incidentID"}")

            val uploadTasks = mutableListOf<Task<Uri>>()

            if (photoUris.isNotEmpty()) {
                photoUris.forEachIndexed { index, uri ->
                    uri?.let {
                        val photoRef = storageReference.child("photo_${index}")
                        val uploadTask = photoRef.putFile(it)
                            .continueWithTask { task ->
                                if (!task.isSuccessful) {
                                    throw task.exception ?: Exception(localization["unknown_error"] ?: localization["something_went_wrong"] ?: "Something went wrong")
                                }
                                photoRef.downloadUrl
                            }
                        uploadTasks.add(uploadTask)
                    }
                }
            }

            try {
                val downloadUrls = Tasks.whenAllSuccess<Uri>(uploadTasks).await()

                _incidentCreationState.value = IncidentCreationState.CreatingIncident
                downloadUrls.forEach { uri ->
                    photos.add(uri.toString())
                }

                val data = hashMapOf(
                    "address" to address,
                    "createdBy" to creatorName,
                    "creationDate" to currentTime,
                    "description" to description,
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "photos" to photos,
                    "region" to region,
                    "type" to when(type){
                        IncidentType.CAR_ACCIDENT -> "CAR_ACCIDENT"
                        IncidentType.ROADBLOCK -> "ROADBLOCK"
                        IncidentType.WEATHER_CONDITIONS -> "WEATHER_CONDITIONS"
                        IncidentType.TRAFFIC_JAM -> "TRAFFIC_JAM"
                        IncidentType.OTHER -> "OTHER"
                    }
                )

                FirebaseFirestore.getInstance()
                    .collection("incidents")
                    .document(incidentID)
                    .set(data)
                    .addOnSuccessListener {
                        _incidentCreationState.value = IncidentCreationState.Success
                        Toast.makeText(context, localization["incident_adding_success"] ?: localization["something_went_wrong"] ?: "Something went wrong", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        _incidentCreationState.value = IncidentCreationState.Error(e.message ?: localization["incident_adding_fail"] ?: localization["something_went_wrong"] ?: "Something went wrong")
                    }
            } catch (e: Exception) {
                _incidentCreationState.value = IncidentCreationState.Error(e.message ?: localization["photos_adding_fail"] ?: localization["something_went_wrong"] ?: "Something went wrong")
            }
        }
    }

    fun loadIncidentsByRegion(
        region: String,
        localization: Map<String, String>
    ) {
        _loadingDocumentsState.value = LoadingDocumentsState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val currentTime = System.currentTimeMillis()
            val threeHoursAgo = Timestamp(Date(currentTime - 3 * 60 * 60 * 1000))

            db.collection("incidents")
                .whereEqualTo("region", region)
                .whereGreaterThan("creationDate", threeHoursAgo)
                .orderBy("creationDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val documents = querySnapshot.documents
                    if (documents.isEmpty()) {
                        _loadingDocumentsState.value = LoadingDocumentsState.Success
                    } else {
                        _documentsList.value = documents
                        _loadingDocumentsState.value = LoadingDocumentsState.Success
                    }
                }
                .addOnFailureListener { e ->
                    _loadingDocumentsState.value = LoadingDocumentsState.Error(e.message ?: localization["incidents_loading_fail"] ?: localization["something_went_wrong"] ?: "Something went wrong")
                }
        }
    }

    suspend fun getLatestIncident(
        region: String,
        localization: Map<String, String>
    ): DocumentSnapshot? {
        return try {
            val querySnapshot = db.collection("incidents")
                .whereEqualTo("region", region)
                .orderBy("creationDate", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                querySnapshot.documents.first()
            } else {
                null
            }
        } catch (e: Exception) {
            _loadingDocumentsState.value = LoadingDocumentsState.Error(e.message ?: localization["incidents_loading_fail"] ?: localization["something_went_wrong"] ?: "Something went wrong")
            null
        }
    }

    suspend fun getOldestIncident(
        region: String,
        localization: Map<String, String>
    ): DocumentSnapshot? {
        return try {
            val querySnapshot = db.collection("incidents")
                .whereEqualTo("region", region)
                .orderBy("creationDate", Query.Direction.ASCENDING)
                .limit(1)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                querySnapshot.documents.first()
            } else {
                null
            }
        } catch (e: Exception) {
            _loadingDocumentsState.value = LoadingDocumentsState.Error(e.message ?: localization["incidents_loading_fail"] ?: localization["something_went_wrong"] ?: "Something went wrong")
            null
        }
    }
}

sealed class IncidentCreationState {
    data object Success : IncidentCreationState()
    data object Loading : IncidentCreationState()
    data object UploadingPhotos : IncidentCreationState()
    data object CreatingIncident : IncidentCreationState()
    data object Null : IncidentCreationState()
    data class Error(val message: String) : IncidentCreationState()
}

sealed class LoadingDocumentsState {
    data object Success : LoadingDocumentsState()
    data object Loading : LoadingDocumentsState()
    data object Null : LoadingDocumentsState()
    data class Error(val message: String) : LoadingDocumentsState()
}