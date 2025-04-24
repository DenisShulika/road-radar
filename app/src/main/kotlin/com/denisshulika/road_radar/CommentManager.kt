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

    private var listenerRegistration: ListenerRegistration? = null

    fun addComment(
        comment: Comment,
        authorName: String,
        authorAvatar: String,
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

                val authorData = mapOf("name" to authorName, "avatar" to authorAvatar)
                db.collection("incidents")
                    .document(comment.incidentId)
                    .collection("commentAuthors")
                    .document(comment.authorId)
                    .set(authorData)
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
                    .collection("commentAuthors")
                    .get()
                    .addOnSuccessListener { documents ->
                        _authors.value = documents.documents.mapNotNull { doc ->
                            val author = doc.toObject(Author::class.java)
                            val id = doc.id
                            if (author != null) id to author else null
                        }.toMap()
                        _comments.value = commentList
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
    val name: String = "",
    val avatar: String = ""
)

sealed class CommentAdditionState {
    data object Success : CommentAdditionState()
    data object Loading : CommentAdditionState()
    data object UploadingPhotos : CommentAdditionState()
    data object Idle : CommentAdditionState()
    data class Error(val message: String) : CommentAdditionState()
}