package com.denisshulika.road_radar.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.denisshulika.road_radar.AuthViewModel
import com.denisshulika.road_radar.R
import com.denisshulika.road_radar.ResetEmailState
import com.denisshulika.road_radar.Routes
import com.denisshulika.road_radar.local.UserLocalStorage
import com.denisshulika.road_radar.ui.components.StyledBasicTextField
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun EmailResetPage(
    @Suppress("UNUSED_PARAMETER") modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val systemUiController = rememberSystemUiController()

    systemUiController.setStatusBarColor(
        color = Color.Transparent,
        darkIcons = false
    )
    systemUiController.setNavigationBarColor(
        color = Color.Transparent,
        darkIcons = false
    )

    val context = LocalContext.current

    val resetEmailState = authViewModel.resetEmailState.observeAsState()

    LaunchedEffect(resetEmailState.value) {
        when (resetEmailState.value) {
            is ResetEmailState.Success -> {
                navController.navigate(Routes.LOGIN)
                authViewModel.resetEmailState.value = ResetEmailState.Null
            }
            is ResetEmailState.Error -> {
                Toast.makeText(
                    context,
                    (resetEmailState.value as ResetEmailState.Error).message,
                    Toast.LENGTH_LONG).show()
                authViewModel.setResetEmailState(ResetEmailState.Null)
            }
            else -> Unit
        }
    }

    var newEmail by remember { mutableStateOf("") }
    var newEmailError by remember { mutableStateOf(false) }
    var isNewEmailEmpty by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val userLocalStorage = UserLocalStorage(context)
    LaunchedEffect(Unit) {
        email = userLocalStorage.getUserEmail().toString()
        password = userLocalStorage.getUserPassword().toString()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(painterResource(R.drawable.auth_background), contentScale = ContentScale.Crop)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Reset",
                    fontSize = 64.sp,
                    color = Color.White,
                    fontFamily = RubikFont,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Email",
                    fontSize = 64.sp,
                    color = Color.White,
                    fontFamily = RubikFont,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
                    .background(Color.White),
                verticalArrangement = Arrangement.Top
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 10.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.size(20.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = "New Email",
                            fontSize = 24.sp,
                            fontFamily = RubikFont,
                            fontWeight = FontWeight.Normal
                        )
                        StyledBasicTextField(
                            value = newEmail,
                            onValueChange = {
                                newEmail = it
                                newEmailError = !isValidEmail(it)
                                isNewEmailEmpty = newEmail.isEmpty()
                            },
                            placeholder = "Enter your new email",
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
                        )
                    }
                    if (isNewEmailEmpty) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Email cant be empty",
                            color = Color(0xFFB71C1C),
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else if (newEmailError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Invalid email address",
                            color = Color(0xFFB71C1C),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.size(32.dp))
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        onClick = {
                            isNewEmailEmpty = newEmail.isEmpty()
                            if(isNewEmailEmpty) {
                                Toast.makeText(context, "Please, enter your email", Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            newEmailError = !isValidEmail(newEmail)
                            if(newEmailError) {
                                Toast.makeText(context, "Please, enter correct email address", Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            authViewModel.resetEmail(
                                newEmailAddress = newEmail,
                                email = email,
                                password = password,
                                context = context
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF474EFF)),
                        enabled = resetEmailState.value != ResetEmailState.Loading
                    ) {
                        if (resetEmailState.value is ResetEmailState.Loading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .align(Alignment.Center),
                                    color = Color(0xFF474EFF)
                                )
                            }
                        } else {
                            Text(
                                text = "Reset Email",
                                fontSize = 24.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                    Spacer(modifier = Modifier.size(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(
                            onClick = {
                                navController.navigate(Routes.LOGIN)
                            },
                            enabled = resetEmailState.value != ResetEmailState.Loading
                        ) {
                            Text(
                                text = "Go back",
                                fontSize = 16.sp,
                                color = Color(0xFF6369FF),
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                    Spacer(modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

private fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}