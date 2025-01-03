package com.denisshulika.road_radar.pages

import android.util.Patterns
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.denisshulika.road_radar.AuthState
import com.denisshulika.road_radar.AuthViewModel
import com.denisshulika.road_radar.R
import com.denisshulika.road_radar.Routes
import com.denisshulika.road_radar.ui.components.StyledBasicTextField
import com.google.accompanist.systemuicontroller.rememberSystemUiController

val RubikFont = FontFamily(
    Font(R.font.rubik, FontWeight.Normal),
    Font(R.font.rubik_medium, FontWeight.Medium),
    Font(R.font.rubik_semibold, FontWeight.SemiBold),
    Font(R.font.rubik_bold, FontWeight.Bold),
    Font(R.font.rubik_extrabold, FontWeight.ExtraBold)
)

@Composable
fun LoginPage(
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

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var isEmailEmpty by remember { mutableStateOf(false) }

    var password by remember { mutableStateOf("") }
    var isPasswordEmpty by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val authState = authViewModel.authState.observeAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Authenticated ->
                navController.navigate(Routes.INCIDENTS)
            is AuthState.Registrating ->
                navController.navigate(Routes.GOOGLE_REGISTRATING)
            is AuthState.Error -> {
                Toast.makeText(context, (authState.value as AuthState.Error).message, Toast.LENGTH_LONG).show()
                authViewModel.setAuthState(AuthState.Null)
            }
            else -> Unit
        }
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
                    .fillMaxSize()
                    .weight(1f)
                    .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Login",
                    fontSize = 60.sp,
                    color = Color.White,
                    fontFamily = RubikFont,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Column(
                modifier = Modifier
                    .weight(2f)
                    .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
                    .background(Color.White),
                verticalArrangement = Arrangement.Top
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                ) {
                    Spacer(modifier = Modifier.size(16.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Email",
                            fontSize = 24.sp,
                            fontFamily = RubikFont,
                            fontWeight = FontWeight.Normal
                        )
                        StyledBasicTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                emailError = !isValidEmail(it)
                                isEmailEmpty = email.isEmpty()
                            },
                            placeholder = "Enter your email",
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
                        )
                    }
                    if (isEmailEmpty) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Email cant be empty",
                            color = Color(0xFFB71C1C),
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else if (emailError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Invalid email address",
                            color = Color(0xFFB71C1C),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.size(32.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Password",
                                fontSize = 24.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal
                            )

                            IconButton(
                                modifier = Modifier
                                    .size(24.dp),
                                onClick = {
                                    isPasswordVisible = !isPasswordVisible
                                }
                            ) {
                                Icon(
                                    contentDescription = "",
                                    imageVector =
                                    if (isPasswordVisible) {
                                        ImageVector.vectorResource(R.drawable.visibility)
                                    } else {
                                        ImageVector.vectorResource(R.drawable.visibility_off)
                                    },
                                    tint = Color(0xFFADADAD)
                                )
                            }
                        }
                        StyledBasicTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                isPasswordEmpty = password.isEmpty()
                            },
                            placeholder = "Enter your password",
                            isVisible = isPasswordVisible
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        horizontalArrangement =
                            if (isPasswordEmpty) {
                                Arrangement.SpaceBetween
                            } else {
                                Arrangement.End
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isPasswordEmpty) {
                            Text(
                                "Password cant be empty",
                                color = Color(0xFFB71C1C),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        TextButton(
                            onClick = {
                                navController.navigate(Routes.PASSWORD_RESET)
                            },
                            enabled = authState.value != AuthState.Loading
                        ) {
                            Text(
                                text = "Forgot password?",
                                fontSize = 14.sp,
                                color = Color(0xFF6369FF),
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                    Spacer(modifier = Modifier.size(24.dp))
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        onClick = {
                            isEmailEmpty = email.isEmpty()
                            if(isEmailEmpty) {
                                Toast.makeText(context, "Please, enter your email", Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            isPasswordEmpty = password.isEmpty()
                            if(isPasswordEmpty) {
                                Toast.makeText(context, "Please, enter a password", Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            emailError = email.isEmpty()
                            if (emailError) {
                                Toast.makeText(context, "Invalid email address", Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            authViewModel.login(email, password, context, coroutineScope)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF474EFF)),
                        enabled = authState.value != AuthState.Loading
                    ) {
                        if (authState.value is AuthState.Loading) {
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
                                text = "Login",
                                fontSize = 24.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(
                            modifier = Modifier
                                .padding(top = 2.dp, bottom = 2.dp),
                            onClick = {
                                navController.navigate(Routes.SIGNUP)
                            },
                            enabled = authState.value != AuthState.Loading
                        ) {
                            Text(
                                text = "Don't have an account yet? Register here",
                                fontSize = 14.sp,
                                color = Color(0xFF6369FF),
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                    Box {
                        HorizontalDivider(
                            modifier = Modifier
                                .padding(top = 8.dp),
                            thickness = 1.dp,
                            color = Color(0xFFADADAD)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                modifier = Modifier
                                    .background(Color.White)
                                    .padding(start = 4.dp, end = 4.dp),
                                text = "Or Sign In with",
                                color = Color(0xFF707070),
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            authViewModel.signInWithGoogle(context, coroutineScope)
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(40.dp),
                            imageVector = ImageVector.vectorResource(R.drawable.google_icon),
                            contentDescription = "",
                            tint = Color.Unspecified
                        )
                    }
                }
            }
        }
    }
}

private fun isValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}