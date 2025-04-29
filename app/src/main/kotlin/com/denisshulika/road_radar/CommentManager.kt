package com.denisshulika.road_radar

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class CommentManager(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>()

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _commentAdditionState = MutableLiveData<CommentAdditionState>(CommentAdditionState.Idle)
    val commentAdditionState: LiveData<CommentAdditionState> = _commentAdditionState

    private val _comments = MutableLiveData<List<Comment>>()
    val comments: LiveData<List<Comment>> = _comments

    private val _authors = MutableLiveData<Map<String, Author>>()
    val authors: LiveData<Map<String, Author>> = _authors

    private val _selectedProfileID = MutableLiveData("")
    val selectedProfileID: LiveData<String> = _selectedProfileID

    fun setSelectedProfileID(selectedProfileID: String) {
        _selectedProfileID.value = selectedProfileID
    }

    private var listenerRegistration: ListenerRegistration? = null

    fun clearCommentsAndAuthors() {
        _comments.value = emptyList()
        _authors.value = emptyMap()
    }

    fun addComment(
        comment: Comment,
        photoUris: List<Uri> = emptyList(),
        localization: Map<String, String>
    ) {
        viewModelScope.launch {
            _commentAdditionState.value = CommentAdditionState.Loading

            val photoUrls: MutableList<String> = mutableListOf()
            val storageReference =
                storage.reference.child("incidents_photos/${"incident_${comment.incidentId}"}/comment_${comment.id}")

            val uploadTasks = mutableListOf<Task<Uri>>()
            try {
                if (photoUris.isNotEmpty()) {
                    _commentAdditionState.value = CommentAdditionState.UploadingPhotos

                    photoUris.forEachIndexed { index, uri ->
                        val photoRef = storageReference.child("photo_$index")
                        val uploadTask = photoRef.putFile(uri)
                            .continueWithTask { task ->
                                if (!task.isSuccessful) {
                                    throw task.exception ?: Exception(localization["unknown_error"]!!)
                                }
                                photoRef.downloadUrl
                            }
                        uploadTasks.add(uploadTask)
                    }

                    val downloadUrls = Tasks.whenAllSuccess<Uri>(uploadTasks).await()
                    downloadUrls.forEach { uri -> photoUrls.add(uri.toString()) }
                    comment.photos = photoUrls
                }


                db.collection("incidents")
                    .document(comment.incidentId)
                    .get()
                    .addOnSuccessListener { document ->
                        val authors = document.get("authors")?.let {
                            (it as? List<*>)?.filterIsInstance<String>()
                        } ?: emptyList()

                        val updatedAuthors = (authors + comment.authorId).toSet().toList()

                        val updatedData = mapOf(
                            "authors" to updatedAuthors
                        )

                        db.collection("incidents")
                            .document(comment.incidentId)
                            .update(updatedData)
                            .addOnSuccessListener {
                                db.collection("incidents")
                                    .document(comment.incidentId)
                                    .get()
                                    .addOnSuccessListener {
                                        db.collection("incidents")
                                            .document(comment.incidentId)
                                            .collection("comments")
                                            .document(comment.id)
                                            .set(comment)
                                            .addOnSuccessListener {
                                                _commentAdditionState.value = CommentAdditionState.Success
                                            }
                                            .addOnFailureListener { e ->
                                                _commentAdditionState.value = CommentAdditionState.Error(
                                                    e.localizedMessage ?: localization["unknown_error"]!!
                                                )
                                                Toast.makeText(context, e.localizedMessage, Toast.LENGTH_LONG).show()
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        _commentAdditionState.value = CommentAdditionState.Error(
                                            e.localizedMessage ?: localization["unknown_error"]!!
                                        )
                                        Toast.makeText(context, e.localizedMessage, Toast.LENGTH_LONG).show()
                                    }
                            }
                            .addOnFailureListener {

                            }
                    }
                    .addOnFailureListener {

                    }
            } catch (e: Exception) {
                _commentAdditionState.value = CommentAdditionState.Error(
                    e.message ?: localization["photos_adding_fail"]!!
                )
            }
        }
    }

    fun startListeningComments(incidentId: String) {
        listenerRegistration?.remove()

        listenerRegistration = db.collection("incidents")
            .document(incidentId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                val commentList = snapshot.documents.mapNotNull { it.toObject(Comment::class.java) }

                db.collection("incidents")
                    .document(incidentId)
                    .get()
                    .addOnSuccessListener { document ->
                        val authorIds = (document.get("authors") as? List<*>)?.filterIsInstance<String>() ?: emptyList()

                        if (authorIds.isNotEmpty()) {
                            db.collection("users")
                                .whereIn(FieldPath.documentId(), authorIds)
                                .get()
                                .addOnSuccessListener { userDocuments ->
                                    _authors.value = userDocuments.documents.mapNotNull { doc ->
                                        doc.toObject(Author::class.java)?.let { author ->
                                            doc.id to author.apply { id = doc.id }
                                        }
                                    }.toMap()

                                    _comments.value = commentList
                                }
                                .addOnFailureListener {
                                }
                        } else {
                            _comments.value = commentList
                        }
                    }
                    .addOnFailureListener {
                    }
            }
    }

    fun stopListeningComments() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }
}

data class Comment(
    val id: String = UUID.randomUUID().toString(),
    val incidentId: String = "",
    var authorId: String = "",
    val text: String = "",
    var photos: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val systemComment: Boolean = false
)

data class Author(
    var id: String = "",
    val name: String = "",
    val photoUrl: String = ""
)

sealed class CommentAdditionState {
    data object Success : CommentAdditionState()
    data object Loading : CommentAdditionState()
    data object UploadingPhotos : CommentAdditionState()
    data object Idle : CommentAdditionState()
    data class Error(val message: String) : CommentAdditionState()
}