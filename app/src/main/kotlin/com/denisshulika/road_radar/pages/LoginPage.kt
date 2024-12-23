package com.denisshulika.road_radar.pages

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.denisshulika.road_radar.AuthState
import com.denisshulika.road_radar.AuthViewModel
import com.denisshulika.road_radar.Routes

@Composable
fun LoginPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var isEmailEmpty by remember { mutableStateOf(false) }

    var password by remember { mutableStateOf("") }
    var isPasswordEmpty by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val authState = authViewModel.authState.observeAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Authenticated ->
                navController.navigate(Routes.NEWS)
            is AuthState.Error ->
                Toast.makeText(context, (authState.value as AuthState.Error).message, Toast.LENGTH_LONG).show()
            else -> Unit
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Login Page", fontSize = 32.sp)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = !isValidEmail(it)
                isEmailEmpty = email.isEmpty()
            },
            label = {
                Text(text = "Email")
            },
            isError = emailError,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
        )

        if (emailError) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Invalid email address",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (isEmailEmpty) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Email cant be empty",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                isPasswordEmpty = password.isEmpty()
            },
            label = {
                Text(text = "Password")
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null)
                }
            }
        )

        if (isPasswordEmpty) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Password cant be empty",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(context, "Email or password cannot be empty", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                authViewModel.login(email, password)
            },
            enabled = authState.value != AuthState.Loading
        ) {
            Text(text = "Login")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = {
                navController.navigate(Routes.SIGNUP)
            }
        ) {
            Text(text = "Don't have an account? Register here")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Or login with")

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                authViewModel.signInWithGoogle(context, coroutineScope)
            }
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                navController.navigate(Routes.PASSWORD_RESET)
            }
        ) {
            Text(text = "PASSWORD RESET")
        }
    }
}

private fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

//TODO()
// add a visual loading indicator (eg CircularProgressIndicator)

//TODO()
// Icon for login through Google