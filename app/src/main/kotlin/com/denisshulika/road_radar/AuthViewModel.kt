package com.denisshulika.road_radar

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val auth : FirebaseAuth = FirebaseAuth.getInstance()
    private val user = auth.currentUser

    private val _authState = MutableLiveData<AuthState>()
    val authState : LiveData<AuthState> = _authState

    private val _resetPasswordState = MutableLiveData<ResetPasswordState?>()
    val resetPasswordState: MutableLiveData<ResetPasswordState?> = _resetPasswordState

    private val webClientID =
        "634464591851-se26913skmd19o6li8ul9jcie2dt4lkc.apps.googleusercontent.com"

    init { checkAuthStatus() }

    private fun checkAuthStatus() {
        _authState.value = if (auth.currentUser == null) {
            AuthState.Unauthenticated
        } else {
            AuthState.Authenticated
        }
    }

    fun login(
        email : String,
        password : String
    ) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error(
                        task.exception?.message?:"Something went wrong"
                    )
                }
            }
    }

    fun signInWithGoogle(
        context: Context,
        coroutineScope: CoroutineScope
    ) {
        _authState.value = AuthState.Loading

        val credentialManager = CredentialManager.create(context = context)

        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") {str, it -> str + "%02x".format(it)}

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
                                _authState.value = AuthState.Authenticated
                            } else {
                                _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong")
                            }
                        }
                    }
            } catch (e: GetCredentialException) {
                coroutineScope.launch(Dispatchers.Main) {
                    _authState.value = AuthState.Error(e.message ?: "Something went wrong")
                }
            } catch (e: GoogleIdTokenParsingException) {
                coroutineScope.launch(Dispatchers.Main) {
                    _authState.value = AuthState.Error(e.message ?: "Something went wrong")
                }
            } catch (e: Exception) {
                coroutineScope.launch(Dispatchers.Main) {
                    _authState.value = AuthState.Error(e.message ?: "Unknown error occurred")
                }
            }
        }
    }

    fun signup(
        email : String,
        password : String
    ) {
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error(
                        task.exception?.message?:"Something went wrong"
                    )
                }
            }
    }

    fun signout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    fun deleteAccount() {
        user?.let {
            it.delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        signout()
                    } else {
                        _authState.value = AuthState.Error(
                            task.exception?.message ?: "Something went wrong"
                        )
                    }
                }
        } ?: run {
            _authState.value = AuthState.Error("No user logged in")
        }
    }

    fun resetPassword(
        emailAddress: String?,
        context: Context
    ) {
        _resetPasswordState.value = ResetPasswordState.Loading

        if (emailAddress == null) {
            showToast(context = context, message = "No email address found")
        } else if (emailAddress.isEmpty()) {
            showToast(context = context, message = "Email cant be empty")
        } else {
            auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _resetPasswordState.value = ResetPasswordState.Success
                        Toast.makeText(context, "Password reset email sent. Please check your inbox.", Toast.LENGTH_LONG).show()
                    } else {
                        _resetPasswordState.value = ResetPasswordState.Error(task.exception?.message?:"Failed to send reset email")
                    }
                }
        }
    }
}

private fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}

sealed class AuthState {
    data object Authenticated : AuthState()
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class ResetPasswordState {
    data object Success : ResetPasswordState()
    data object Loading : ResetPasswordState()
    data object Null : ResetPasswordState()
    data class Error(val message: String) : ResetPasswordState()
}

//TODO() Expand exceptions that may occur
// Google them and walk through each one

//TODO() remove hardcoded strings and colors