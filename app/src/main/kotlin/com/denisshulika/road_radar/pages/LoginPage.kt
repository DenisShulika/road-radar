package com.denisshulika.road_radar.pages

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.denisshulika.road_radar.AuthState
import com.denisshulika.road_radar.AuthViewModel
import com.denisshulika.road_radar.R
import com.denisshulika.road_radar.Routes
import com.denisshulika.road_radar.SettingsViewModel
import com.denisshulika.road_radar.model.ThemeState
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
    navController: NavController,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val localization = settingsViewModel.localization.observeAsState().value!!
    val theme = settingsViewModel.themeColors.observeAsState().value!!

    val systemUiController = rememberSystemUiController()

    systemUiController.setStatusBarColor(
        color = Color.Transparent,
        darkIcons = settingsViewModel.getTheme() != ThemeState.DARK || !isSystemInDarkTheme()
    )
    systemUiController.setNavigationBarColor(
        color = theme["background"]!!,
        darkIcons = settingsViewModel.getTheme() != ThemeState.DARK || !isSystemInDarkTheme()
    )

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var isEmailEmpty by remember { mutableStateOf(false) }

    var password by remember { mutableStateOf("") }
    var isPasswordEmpty by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Authenticated ->
                navController.navigate(Routes.INCIDENTS) {
                    popUpTo(0) { inclusive = true }
                }
            is AuthState.GoogleRegistrating ->
                navController.navigate(Routes.GOOGLE_REGISTRATING) {
                    popUpTo(0) { inclusive = true }
                }
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
            .paint(
                painterResource(id = if (settingsViewModel.getTheme() == ThemeState.DARK) R.drawable.auth_dark_background else R.drawable.auth_light_background),
                contentScale = ContentScale.Crop
            )
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
                    text = localization["login_title"]!!,
                    fontSize = 72.sp,
                    color = theme["text"]!!,
                    fontFamily = RubikFont,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Column(
                modifier = Modifier
                    .weight(2f)
                    .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
                    .background(theme["background"]!!),
                verticalArrangement = Arrangement.Top
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                ) {
                    Spacer(modifier = Modifier.size(16.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = localization["email_title"]!!,
                            fontSize = 24.sp,
                            fontFamily = RubikFont,
                            fontWeight = FontWeight.Normal,
                            color = theme["text"]!!
                        )
                        StyledBasicTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                emailError = !isValidEmail(it)
                                isEmailEmpty = email.isEmpty()
                            },
                            placeholder = localization["email_placeholder_login"]!!,
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                            theme = theme
                        )
                    }
                    if (isEmailEmpty) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            localization["email_empty"]!!,
                            color = theme["error"]!!,
                            fontSize = 12.sp,
                            fontFamily = RubikFont,
                            fontWeight = FontWeight.Normal
                        )
                    } else if (emailError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            localization["email_invalid"]!!,
                            color = theme["error"]!!,
                            fontSize = 12.sp,
                            fontFamily = RubikFont,
                            fontWeight = FontWeight.Normal
                        )
                    }
                    Spacer(modifier = Modifier.size(32.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = localization["password_title"]!!,
                                fontSize = 24.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal,
                                color = theme["text"]!!
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
                                    tint = theme["accent"]!!
                                )
                            }
                        }
                        StyledBasicTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                isPasswordEmpty = password.isEmpty()
                            },
                            placeholder = localization["password_placeholder_login"]!!,
                            isVisible = isPasswordVisible,
                            theme = theme
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
                                localization["password_empty"]!!,
                                color = theme["error"]!!,
                                fontSize = 12.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal
                            )
                        }
                        TextButton(
                            onClick = {
                                navController.navigate(Routes.PASSWORD_RESET)
                            },
                            enabled = authState.value != AuthState.Loading
                        ) {
                            Text(
                                text = localization["forgot_password_button"]!!,
                                fontSize = 14.sp,
                                color = theme["primary"]!!,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Medium
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
                                Toast.makeText(context, localization["email_empty_error"]!!, Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            emailError = email.isEmpty()
                            if (emailError) {
                                Toast.makeText(context, localization["email_invalid_error"]!!, Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            isPasswordEmpty = password.isEmpty()
                            if(isPasswordEmpty) {
                                Toast.makeText(context, localization["password_empty_error"]!!, Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            authViewModel.login(email, password, context, coroutineScope, localization)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme["primary"]!!,
                            disabledContainerColor = theme["drawer_background"]!!
                        ),
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
                                    color = theme["primary"]!!
                                )
                            }
                        } else {
                            Text(
                                text = localization["login_button"]!!,
                                fontSize = 24.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Medium
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
                                text = localization["register_here_button"]!!,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                color = theme["primary"]!!,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Box {
                        HorizontalDivider(
                            modifier = Modifier
                                .padding(top = 8.dp),
                            thickness = 1.dp,
                            color = theme["accent"]!!
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                modifier = Modifier
                                    .background(theme["background"]!!)
                                    .padding(start = 4.dp, end = 4.dp),
                                text = localization["or_sign_in_with"]!!,
                                color = theme["placeholder"]!!,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            authViewModel.signInWithGoogle(context, coroutineScope, localization)
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