package com.denisshulika.road_radar.pages

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.denisshulika.road_radar.AuthViewModel
import com.denisshulika.road_radar.ResetPasswordState
import com.denisshulika.road_radar.Routes

@Composable
fun PasswordResetPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current

    val resetPasswordState = authViewModel.resetPasswordState.observeAsState()

    LaunchedEffect(resetPasswordState.value) {
        when (resetPasswordState.value) {
            is ResetPasswordState.Success -> {
                navController.navigate(Routes.LOGIN)
                authViewModel.resetPasswordState.value = ResetPasswordState.Null
            }
            is ResetPasswordState.Error -> {
                Toast.makeText(
                    context,
                    (resetPasswordState as ResetPasswordState.Error).message,
                    Toast.LENGTH_LONG).show()
            }
            else -> Unit
        }
    }

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var isEmailEmpty by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Password Rest Page", fontSize = 32.sp)

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

        Button(
            onClick = {
                isEmailEmpty = email.isEmpty()
                if(isEmailEmpty) {
                    return@Button
                }
                authViewModel.resetPassword(
                    emailAddress = email,
                    context = context
                )
            },
            enabled = resetPasswordState.value != ResetPasswordState.Loading
        ) {
            Text(text = "RESET IT")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                navController.navigate(Routes.LOGIN)
            },
            enabled = resetPasswordState.value != ResetPasswordState.Loading
        ) {
            Text(text = "GO BACK")
        }
    }
}

private fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

//TODO()
// add a visual loading indicator (eg CircularProgressIndicator)