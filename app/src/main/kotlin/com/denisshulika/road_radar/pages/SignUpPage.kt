package com.denisshulika.road_radar.pages

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.denisshulika.road_radar.AuthState
import com.denisshulika.road_radar.AuthViewModel
import com.denisshulika.road_radar.R
import com.denisshulika.road_radar.Routes
import com.denisshulika.road_radar.SettingsViewModel
import com.denisshulika.road_radar.isValidEmail
import com.denisshulika.road_radar.isValidPhoneNumber
import com.denisshulika.road_radar.model.ThemeState
import com.denisshulika.road_radar.ui.components.StyledBasicTextField
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.io.File

@Composable
fun SignUpPage(
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

    val authState = authViewModel.authState.observeAsState()

    var name by remember { mutableStateOf("") }
    var isNameEmpty by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var isEmailEmpty by remember { mutableStateOf(false) }

    var phoneNumber by remember { mutableStateOf("") }
    var phoneNumberError by remember { mutableStateOf(false) }
    var isPhoneNumberEmpty by remember { mutableStateOf(false) }

    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }
    var isPasswordEmpty by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf(false) }
    var isConfirmPasswordEmpty by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isImageSelected by remember { mutableStateOf(false) }

    val imageCropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            imageUri = result.uriContent
            isImageSelected = true
        } else {
            val exception = result.error
            if (exception != null) {
                Toast.makeText(context, exception.message, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, localization["image_cropping_error"], Toast.LENGTH_LONG).show()
            }
        }
    }

    if (imageUri != null) {
        val source = ImageDecoder.createSource(context.contentResolver, imageUri!!)
        bitmap = ImageDecoder.decodeBitmap(source)
    }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            isImageSelected = false
            val cropOption = CropImageContractOptions(
                uri,
                CropImageOptions().apply {
                    guidelines = CropImageView.Guidelines.ON
                    aspectRatioX = 1
                    aspectRatioY = 1
                    fixAspectRatio = true
                    cropShape = CropImageView.CropShape.RECTANGLE
                    showCropOverlay = true
                    autoZoomEnabled = true
                }
            )
            imageCropLauncher.launch(cropOption)
    }

    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Authenticated ->
                navController.navigate(Routes.INCIDENTS)
            is AuthState.Error ->
                Toast.makeText(context, (authState.value as AuthState.Error).message, Toast.LENGTH_LONG).show()
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
                    .weight(1f)
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = localization["sign_up_title"]!!,
                    fontSize = 60.sp,
                    color = theme["text"]!!,
                    fontFamily = RubikFont,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Box (
                modifier = Modifier
                    .weight(2f)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
                        .background(theme["background"]!!)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Top
                ) {
                    Column(
                        modifier = Modifier
                            .padding(
                                start = 20.dp,
                                top = 50.dp,
                                end = 20.dp,
                                bottom = 10.dp
                            )
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = localization["name_title"]!!,
                                fontSize = 24.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal,
                                color = theme["text"]!!
                            )
                            StyledBasicTextField(
                                value = name,
                                onValueChange = {
                                    name = it
                                    isNameEmpty = name.isEmpty()
                                },
                                placeholder = localization["name_placeholder"]!!,
                                theme = theme
                            )
                        }
                        if (isNameEmpty) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                localization["name_empty"]!!,
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
                                placeholder = localization["email_placeholder_sign_up"]!!,
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
                            Text(
                                text = localization["phone_title"]!!,
                                fontSize = 24.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal,
                                color = theme["text"]!!
                            )
                            StyledBasicTextField(
                                value = phoneNumber,
                                onValueChange = {
                                    phoneNumber = it
                                    isPhoneNumberEmpty = phoneNumber.isEmpty()
                                    phoneNumberError = !isValidPhoneNumber(it)
                                },
                                placeholder = localization["phone_placeholder"]!!,
                                theme = theme
                            )
                        }
                        if (isPhoneNumberEmpty) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                localization["phone_empty"]!!,
                                color = theme["error"]!!,
                                fontSize = 12.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal
                            )
                        } else if (phoneNumberError) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                localization["phone_invalid"]!!,
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
                                            Icons.Rounded.Visibility
                                        } else {
                                            Icons.Rounded.VisibilityOff
                                        },
                                        tint = theme["accent"]!!
                                    )
                                }
                            }
                            StyledBasicTextField(
                                value = password,
                                onValueChange = {
                                    password = it
                                    passwordError = password.length < 6
                                    isPasswordEmpty = password.isEmpty()
                                },
                                placeholder = localization["password_placeholder_sign_up"]!!,
                                isVisible = isPasswordVisible,
                                theme = theme
                            )
                        }
                        if (isPasswordEmpty) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                localization["password_empty"]!!,
                                color = theme["error"]!!,
                                fontSize = 12.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal
                            )
                        } else if (passwordError) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                localization["password_length"]!!,
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
                                    text = localization["confirm_password_title"]!!,
                                    fontSize = 24.sp,
                                    fontFamily = RubikFont,
                                    fontWeight = FontWeight.Normal,
                                    color = theme["text"]!!
                                )
                                IconButton(
                                    modifier = Modifier
                                        .size(24.dp),
                                    onClick = {
                                        isConfirmPasswordVisible = !isConfirmPasswordVisible
                                    }
                                ) {
                                    Icon(
                                        contentDescription = "",
                                        imageVector =
                                        if (isConfirmPasswordVisible) {
                                            Icons.Rounded.Visibility
                                        } else {
                                            Icons.Rounded.VisibilityOff
                                        },
                                        tint = theme["accent"]!!
                                    )
                                }
                            }
                            StyledBasicTextField(
                                value = confirmPassword,
                                onValueChange = {
                                    confirmPassword = it
                                    isConfirmPasswordEmpty = confirmPassword.isEmpty()
                                },
                                placeholder = localization["confirm_password_placeholder"]!!,
                                isVisible = isConfirmPasswordVisible,
                                theme = theme
                            )
                        }
                        if (isConfirmPasswordEmpty) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                localization["confirm_password_empty"]!!,
                                color = theme["error"]!!,
                                fontSize = 12.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal
                            )
                        } else if (confirmPasswordError) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                localization["passwords_do_not_match"]!!,
                                color = theme["error"]!!,
                                fontSize = 12.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal
                            )
                        }
                        Spacer(modifier = Modifier.size(24.dp))
                        Button(
                            onClick = {
                                isNameEmpty = name.isEmpty()
                                if(isNameEmpty) {
                                    Toast.makeText(context, localization["name_empty_error"]!!, Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                isEmailEmpty = email.isEmpty()
                                if(isEmailEmpty) {
                                    Toast.makeText(context, localization["email_empty_error"]!!, Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                emailError = !isValidEmail(email)
                                if(emailError) {
                                    Toast.makeText(context, localization["email_invalid_error"]!!, Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                isPhoneNumberEmpty = phoneNumber.isEmpty()
                                if(isPhoneNumberEmpty) {
                                    Toast.makeText(context, localization["phone_empty_error"]!!, Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                phoneNumberError = !isValidPhoneNumber(phoneNumber)
                                if(phoneNumberError) {
                                    Toast.makeText(context, localization["phone_invalid_error"]!!, Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                isPasswordEmpty = password.isEmpty()
                                if(isPasswordEmpty) {
                                    Toast.makeText(context, localization["password_empty_error"]!!, Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                passwordError = password.length < 6
                                if(passwordError) {
                                    Toast.makeText(context, localization["password_length_error"]!!, Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                isConfirmPasswordEmpty = confirmPassword.isEmpty()
                                if(isConfirmPasswordEmpty) {
                                    Toast.makeText(context, localization["confirm_password_empty_error"]!!, Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                confirmPasswordError = password != confirmPassword
                                if(confirmPasswordError) {
                                    Toast.makeText(context, localization["passwords_do_not_match_error"]!!, Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                if(!isImageSelected) {
                                    Toast.makeText(context, localization["no_avatar_error"]!!, Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                if (bitmap == null) {
                                    Toast.makeText(context, localization["re_add_avatar_error"]!!, Toast.LENGTH_LONG).show()
                                    return@Button
                                } else {
                                    authViewModel.signup(
                                        email = email,
                                        password = password,
                                        name = name,
                                        phoneNumber = phoneNumber.replace(" ", ""),
                                        photo = bitmapToUri(context, bitmap!!).toString(),
                                        localization = localization,
                                        callback = {
                                            navController.navigate(Routes.LOGIN)
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
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
                                    text = localization["sign_up_button"]!!,
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
                                    navController.navigate(Routes.LOGIN)
                                }
                            ) {
                                Text(
                                    modifier = Modifier,
                                    text = localization["login_here_button"]!!,
                                    fontSize = 14.sp,
                                    color = theme["primary"]!!,
                                    fontFamily = RubikFont,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.size(4.dp))
                    }
                }
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .offset(y = (-50).dp)
                        .align(Alignment.TopCenter)
                        .clip(RoundedCornerShape(10.dp))
                        .background(theme["drawer_background"]!!)
                        .clickable {
                            imagePickerLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isImageSelected) {
                        Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = "",
                            modifier = Modifier.size(100.dp)
                        )
                    } else {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.add_photo_alternate),
                            contentDescription = "",
                            tint = theme["placeholder"]!!,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    }
}

fun bitmapToUri(context: Context, bitmap: Bitmap): Uri {
    val file = File(context.cacheDir, "profile_image_${System.currentTimeMillis()}.jpg")
    file.outputStream().use {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
    }
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
}