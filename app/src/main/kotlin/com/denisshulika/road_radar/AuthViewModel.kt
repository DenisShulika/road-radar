package com.denisshulika.road_radar

import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denisshulika.road_radar.local.UserLocalStorage
import com.denisshulika.road_radar.model.UserData
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Timestamp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _resetPasswordState = MutableLiveData<ResetPasswordState?>()
    val resetPasswordState: MutableLiveData<ResetPasswordState?> = _resetPasswordState

    private val _resetEmailState = MutableLiveData<ResetEmailState?>()
    val resetEmailState: MutableLiveData<ResetEmailState?> = _resetEmailState

    private val webClientID =
        "634464591851-se26913skmd19o6li8ul9jcie2dt4lkc.apps.googleusercontent.com"

    init {
        checkAuthStatus()
    }

     fun checkAuthStatus() {
        val user = auth.currentUser

        if (user == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            viewModelScope.launch {
                if (userFinishedRegistrating()) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.GoogleRegistrating
                }
            }
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun setAuthState(state: AuthState) {
        _authState.value = state
    }

    fun setResetPasswordState(state: ResetPasswordState) {
        _resetPasswordState.value = state
    }

    fun setResetEmailState(state: ResetEmailState) {
        _resetEmailState.value = state
    }

    private fun isUserLoggedInWithGoogle(): Boolean {
        val user = auth.currentUser
        return user?.providerData?.any { it.providerId == GoogleAuthProvider.PROVIDER_ID } == true
    }

    fun isUserLoggedInWithEmailPassword(): Boolean {
        val user = auth.currentUser
        return user?.providerData?.any { it.providerId == EmailAuthProvider.PROVIDER_ID } == true
    }

    fun updateUserProfile(
        name: String,
        photo: String,
        phoneNumber: String,
        context: Context,
        localization: Map<String, String>,
    ) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val firestore = FirebaseFirestore.getInstance()

        val profileUpdates = userProfileChangeRequest {
            displayName = name
            photoUri = Uri.parse(photo)
        }

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, localization["profile_updating_success"]!!, Toast.LENGTH_SHORT).show()

                    val userData = mapOf(
                        "name" to name,
                        "photoUrl" to photo,
                        "phoneNumber" to phoneNumber
                    )

                    firestore.collection("users")
                        .document(user.uid)
                        .set(userData, SetOptions.merge())
                        .addOnSuccessListener {

                        }
                        .addOnFailureListener {
                            Toast.makeText(context, localization["user_data_saving_fail"]!!, Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, localization["profile_updating_fail"]!!, Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun login(
        email: String,
        password: String,
        context: Context,
        coroutineScope: CoroutineScope,
        localization: Map<String, String>
    ) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    if (user != null && !user.isEmailVerified) {
                        auth.signOut()
                        _authState.value = AuthState.Error(localization["email_not_verified"]!!)
                        return@addOnCompleteListener
                    }

                    user?.let { firebaseUser ->
                        val uid = firebaseUser.uid
                        firestore.collection("users").document(uid).get()
                            .addOnSuccessListener { document ->
                                val phoneNumber = document.getString("phoneNumber") ?: ""
                                val photoUrl = firebaseUser.photoUrl?.toString()

                                val userData = UserData(
                                    uid = uid,
                                    email = email,
                                    password = password,
                                    name = firebaseUser.displayName ?: "",
                                    phoneNumber = phoneNumber,
                                    photoUrl = photoUrl
                                )

                                coroutineScope.launch {
                                    val localStorage = UserLocalStorage(context)
                                    localStorage.saveUser(userData)
                                    _authState.value = AuthState.Authenticated
                                }
                            }
                            .addOnFailureListener {
                                _authState.value = AuthState.Error(localization["user_data_loading_fail"]!!)
                            }
                    }
                } else {
                    if (task.exception?.message!!.contains("The supplied auth credential is incorrect")) {
                        _authState.value = AuthState.Error(
                            localization["wrong_email_or_password"]!!
                        )
                    } else {
                        _authState.value = AuthState.Error(
                            task.exception?.message ?: localization["something_went_wrong"]!!
                        )
                    }
                }
            }
    }

    fun signup(
        email: String,
        password: String,
        name: String,
        phoneNumber: String,
        photo: String,
        localization: Map<String, String>,
        callback: () -> Unit
    ) {
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val uid = user?.uid ?: ""

                    user?.sendEmailVerification()

                    val profileUpdates = userProfileChangeRequest {
                        displayName = name
                        photoUri = Uri.parse(photo)
                    }

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                val userData = hashMapOf(
                                    "phoneNumber" to phoneNumber,
                                    "photoUrl" to user.photoUrl,
                                    "name" to name,
                                    "accountAge" to Timestamp.now(),
                                    "experience" to 0,
                                    "reportsCount" to 0,
                                    "thanksCount" to 0,
                                    "thanksGivenCount" to 0
                                )
                                firestore.collection("users")
                                    .document(uid)
                                    .set(userData)
                                    .addOnSuccessListener {
                                        callback()
                                        _authState.value = AuthState.Error(localization["email_verification_sent"]!!)
                                    }
                                    .addOnFailureListener {
                                        user.delete()
                                        _authState.value = AuthState.Error(localization["user_data_saving_fail"]!!)
                                    }
                            } else {
                                user.delete()
                                _authState.value = AuthState.Error(
                                    task.exception?.message ?: localization["something_went_wrong"]!!
                                )
                            }
                        }

                } else {
                    if (task.exception?.message!!.contains("is already in use")) {
                        _authState.value = AuthState.Error(
                            localization["email_in_use"]!!
                        )
                    } else {
                        _authState.value = AuthState.Error(
                            task.exception?.message ?: localization["something_went_wrong"]!!
                        )
                    }
                }
            }
    }

    fun signInWithGoogle(
        context: Context,
        coroutineScope: CoroutineScope,
        localization: Map<String, String>
    ) {
        _authState.value = AuthState.Loading

        val credentialManager = CredentialManager.create(context = context)

        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientID)
            .setNonce(hashedNonce)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = context,
                )
                val credential = result.credential
                val googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(credential.data)
                val googleIdToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)

                val auth = FirebaseAuth.getInstance()
                auth.signInWithCredential(authCredential)
                    .addOnCompleteListener { task ->
                        coroutineScope.launch(Dispatchers.Main) {
                            if (task.isSuccessful) {
                                val isRegistered = userFinishedRegistrating()
                                if (isRegistered) {
                                    val user = auth.currentUser
                                    user?.let { firebaseUser ->
                                        val uid = firebaseUser.uid
                                        firestore.collection("users").document(uid).get()
                                            .addOnSuccessListener { document ->
                                                val phoneNumber = document.getString("phoneNumber") ?: ""
                                                val photoUrl = firebaseUser.photoUrl?.toString()

                                                val userData = UserData(
                                                    uid = uid,
                                                    email = "",
                                                    password = "",
                                                    name = firebaseUser.displayName ?: "",
                                                    phoneNumber = phoneNumber,
                                                    photoUrl = photoUrl
                                                )

                                                coroutineScope.launch {
                                                    val localStorage = UserLocalStorage(context)
                                                    localStorage.saveUser(userData)
                                                }
                                                _authState.value = AuthState.Authenticated
                                            }
                                            .addOnFailureListener {
                                                _authState.value = AuthState.Error(localization["user_data_loading_fail"]!!)
                                            }
                                    }
                                } else {
                                    _authState.value = AuthState.GoogleRegistrating
                                }
                            } else {
                                _authState.value = AuthState.Error(
                                    task.exception?.message ?: localization["something_went_wrong"]!!
                                )
                            }
                        }
                    }
            } catch (e: GetCredentialException) {
                coroutineScope.launch(Dispatchers.Main) {
                    _authState.value = AuthState.Error(e.message ?: localization["something_went_wrong"]!!)
                }
            } catch (e: GoogleIdTokenParsingException) {
                coroutineScope.launch(Dispatchers.Main) {
                    _authState.value = AuthState.Error(e.message ?: localization["something_went_wrong"]!!)
                }
            } catch (e: Exception) {
                coroutineScope.launch(Dispatchers.Main) {
                    _authState.value = AuthState.Error(e.message ?: localization["unknown_error"]!!)
                }
            }
        }
    }

    fun completeRegistrationViaGoogle(
        phoneNumber: String,
        context: Context,
        coroutineScope: CoroutineScope,
        localization: Map<String, String>
    ) {
        val user = auth.currentUser
        if (user == null) {
            _authState.value = AuthState.Error(localization["user_not_authenticated"]!!)
            return
        }

        val name = user.displayName
        val photo = user.photoUrl

        val firestore = FirebaseFirestore.getInstance()
        val userData = hashMapOf(
            "phoneNumber" to phoneNumber,
            "photoUrl" to photo,
            "name" to name,
            "accountAge" to Timestamp.now(),
            "experience" to 0,
            "reportsCount" to 0,
            "thanksCount" to 0,
            "thanksGivenCount" to 0
        )

        val uid = user.uid
        firestore.collection("users")
            .document(uid)
            .set(userData)
            .addOnSuccessListener {
                val userDataToSave = UserData(
                    uid = uid,
                    email = user.email ?: "",
                    password = "",
                    name = user.displayName ?: "",
                    phoneNumber = phoneNumber,
                    photoUrl = user.photoUrl?.toString() ?: ""
                )

                coroutineScope.launch {
                    val localStorage = UserLocalStorage(context)
                    localStorage.saveUser(userDataToSave)
                    _authState.value = AuthState.Authenticated
                }
                _authState.value = AuthState.Authenticated
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error("${localization["user_data_saving_fail"]!!}: ${e.message}")
            }
    }

    private suspend fun userFinishedRegistrating(): Boolean {
        val user = auth.currentUser
        val uid = user?.uid ?: return false

        val firestore = FirebaseFirestore.getInstance()
        val userRef = firestore.collection("users").document(uid)

        return try {
            val document = userRef.get().await()
            val phone = document.getString("phoneNumber")
            !phone.isNullOrEmpty()
        } catch (e: Exception) {
            false
        }
    }

    fun signout(
        context: Context,
        coroutineScope: CoroutineScope,
        incidentsManager: IncidentsManager
    ) {
        _authState.value = AuthState.Unauthenticated

        incidentsManager.resetDocumentList()

        auth.signOut()

        coroutineScope.launch {
            val localStorage = UserLocalStorage(context)
            localStorage.clearUserData()
        }
    }

    fun deleteAccount(
        email : String,
        password: String,
        context: Context,
        coroutineScope: CoroutineScope,
        incidentsManager: IncidentsManager,
        localization: Map<String, String>
    ) {
        val user = auth.currentUser

        user?.let { firebaseUser ->
            val uid = firebaseUser.uid

            val credential = if (isUserLoggedInWithGoogle()) {
                GoogleAuthProvider.getCredential(user.getIdToken(true).toString(), null)
            } else if(isUserLoggedInWithEmailPassword()) {
                EmailAuthProvider.getCredential(email, password)
            } else {
                null
            }

            if(credential != null) {
                user.reauthenticate(credential)
                    .addOnCompleteListener {}
            } else {
                Toast.makeText(context, localization["reauthenticate_fail"], Toast.LENGTH_LONG).show()
                return
            }

            firestore.collection("users").document(uid).delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        firebaseUser.delete()
                            .addOnCompleteListener { deleteTask ->
                                if (deleteTask.isSuccessful) {
                                    signout(context, coroutineScope, incidentsManager)
                                } else {
                                    Toast.makeText(
                                        context,
                                        deleteTask.exception?.message ?: localization["account_deleting_fail"],
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    } else {
                        Toast.makeText(
                            context,
                            task.exception?.message ?: localization["firestore_deleting_fail"],
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    fun resetPassword(
        emailAddress: String,
        context: Context,
        coroutineScope: CoroutineScope,
        incidentsManager: IncidentsManager,
        localization: Map<String, String>
    ) {
        _resetPasswordState.value = ResetPasswordState.Loading

        auth.sendPasswordResetEmail(emailAddress)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _resetPasswordState.value = ResetPasswordState.Success
                    signout(context, coroutineScope, incidentsManager)
                    Toast.makeText(context, localization["password_reset_email_success"]!!, Toast.LENGTH_LONG).show()
                } else {
                    _resetPasswordState.value = ResetPasswordState.Error(task.exception?.message ?: localization["password_reset_email_fail"]!!)
                }
            }
    }

    fun resetEmail(
        newEmailAddress: String,
        email : String,
        password: String,
        context: Context,
        coroutineScope: CoroutineScope,
        incidentsManager: IncidentsManager,
        localization: Map<String, String>
    ) {
        _resetEmailState.value = ResetEmailState.Loading

        val user = auth.currentUser

        user?.let {
            val credential = if (isUserLoggedInWithGoogle()) {
                GoogleAuthProvider.getCredential(user.getIdToken(true).toString(), null)
            } else if(isUserLoggedInWithEmailPassword()) {
                EmailAuthProvider.getCredential(email, password)
            } else {
                null
            }

            if(credential != null) {
                user.reauthenticate(credential)
                    .addOnCompleteListener {}
            } else {
                Toast.makeText(context, localization["reauthenticate_fail"]!!, Toast.LENGTH_LONG).show()
                return
            }
        }
        if (user != null) {
            user.verifyBeforeUpdateEmail(newEmailAddress)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _resetEmailState.value = ResetEmailState.Success
                        signout(context, coroutineScope, incidentsManager)
                        Toast.makeText(context, localization["email_reset_email_success"]!!, Toast.LENGTH_LONG).show()
                    } else {
                        _resetEmailState.value = ResetEmailState.Error(task.exception?.message ?: localization["email_reset_email_fail"]!!)
                    }
                }
        } else {
            Toast.makeText(context, localization["reauthenticate_fail"]!!, Toast.LENGTH_LONG).show()
        }
    }
}

fun isValidPhoneNumber(phoneNumber: String): Boolean {
    val phone = phoneNumber.replace(" ", "")
    val codesUkraine = arrayOf("50", "66", "95", "99", "75", "67", "68", "96", "97", "98", "63", "73", "93", "91", "92", "94")

    if (phone.length == 13 && phone.startsWith("+380")) {
        val remainingPhone = phone.substring(4)
        val code = remainingPhone.take(2)
        return code in codesUkraine && remainingPhone.all { it.isDigit() }
    }

    if (phone.length == 10 && phone.startsWith("0")) {
        val remainingPhone = phone.substring(1)
        val code = remainingPhone.take(2)
        return code in codesUkraine && remainingPhone.all { it.isDigit() }
    }

    return false
}

fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

sealed class AuthState {
    data object Authenticated : AuthState()
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
    data object GoogleRegistrating : AuthState()
    data object Null : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class ResetPasswordState {
    data object Success : ResetPasswordState()
    data object Loading : ResetPasswordState()
    data object Null : ResetPasswordState()
    data class Error(val message: String) : ResetPasswordState()
}

sealed class ResetEmailState {
    data object Success : ResetEmailState()
    data object Loading : ResetEmailState()
    data object Null : ResetEmailState()
    data class Error(val message: String) : ResetEmailState()
}