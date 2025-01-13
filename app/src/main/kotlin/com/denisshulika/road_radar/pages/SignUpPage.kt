package com.denisshulika.road_radar.pages

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.RichTooltipColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.denisshulika.road_radar.AuthState
import com.denisshulika.road_radar.AuthViewModel
import com.denisshulika.road_radar.R
import com.denisshulika.road_radar.Routes
import com.denisshulika.road_radar.SettingsViewModel
import com.denisshulika.road_radar.isValidEmail
import com.denisshulika.road_radar.isValidPhoneNumber
import com.denisshulika.road_radar.ui.components.AutocompleteTextFieldForRegion
import com.denisshulika.road_radar.ui.components.StyledBasicTextField
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpPage(
    @Suppress("UNUSED_PARAMETER") modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    placesClient: PlacesClient
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val systemUiController = rememberSystemUiController()

    systemUiController.setStatusBarColor(
        color = Color.Transparent,
        darkIcons = false
    )
    systemUiController.setNavigationBarColor(
        color = Color.Transparent,
        darkIcons = false
    )

    val localization = settingsViewModel.localization.observeAsState().value!!

    val authState = authViewModel.authState.observeAsState()

    var name by remember { mutableStateOf("") }
    var isNameEmpty by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var isEmailEmpty by remember { mutableStateOf(false) }

    var phoneNumber by remember { mutableStateOf("") }
    var phoneNumberError by remember { mutableStateOf(false) }
    var isPhoneNumberEmpty by remember { mutableStateOf(false) }

    var selectedRegion by remember { mutableStateOf("") }
    var isRegionSelected by remember { mutableStateOf(false) }
    var isSelectedRegionEmpty by remember { mutableStateOf(false) }

    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }
    var isPasswordEmpty by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf(false) }
    var isConfirmPasswordEmpty by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    var userPhoto by remember { mutableStateOf("") }
    var croppedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isImageSelected by remember { mutableStateOf(false) }
    val getContent = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            isImageSelected = true

            val croppedBitmap = cropImageToSquare(it, context.contentResolver)
            croppedImageUri = if (croppedBitmap != null) bitmapToUri(context, croppedBitmap) else null

            userPhoto = croppedImageUri.toString()
        }
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


    val tooltipState = rememberTooltipState()
    val scope = rememberCoroutineScope()
    
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
                    text = localization["sign_up_title"]!!,
                    fontSize = 52.sp,
                    color = Color.White,
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
                        .background(Color.White)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Top
                ) {
                    Column(
                        modifier = Modifier
                            .padding(
                                start = 20.dp,
                                top = 50.dp,
                                end = 20.dp,
                                bottom = 10.dp)
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
                                fontWeight = FontWeight.Normal
                            )
                            StyledBasicTextField(
                                value = name,
                                onValueChange = {
                                    name = it
                                    isNameEmpty = name.isEmpty()
                                },
                                placeholder = localization["name_placeholder"]!!
                            )
                        }
                        if (isNameEmpty) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                localization["name_empty"]!!,
                                color = Color(0xFFB71C1C),
                                style = MaterialTheme.typography.bodySmall
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
                                fontWeight = FontWeight.Normal
                            )
                            StyledBasicTextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    emailError = !isValidEmail(it)
                                    isEmailEmpty = email.isEmpty()
                                },
                                placeholder = localization["email_placeholder_sign_up"]!!,
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
                            )
                        }
                        if (isEmailEmpty) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                localization["email_empty"]!!,
                                color = Color(0xFFB71C1C),
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else if (emailError) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                localization["email_invalid"]!!,
                                color = Color(0xFFB71C1C),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.size(32.dp))
                        Column {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = localization["region_title"]!!,
                                        fontSize = 24.sp,
                                        fontFamily = RubikFont,
                                        fontWeight = FontWeight.Normal
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))

                                    TooltipBox(
                                        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
                                        tooltip = {
                                            RichTooltip(
                                                modifier = Modifier.padding(20.dp),
                                                title = {
                                                    Text(
                                                        text = localization["region_tip_title"]!!,
                                                        fontSize = 20.sp,
                                                        fontFamily = RubikFont,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                },
                                                text = {
                                                    Text(
                                                        text = localization["region_tip_text"]!!,
                                                        fontSize = 16.sp,
                                                        fontFamily = RubikFont,
                                                        fontWeight = FontWeight.Normal
                                                    )
                                                },
                                                colors = RichTooltipColors(
                                                    containerColor = Color(0xFF474EFF),
                                                    contentColor = Color(0xFFFFFFFF),
                                                    titleContentColor = Color(0xFFFFFFFF),
                                                    actionContentColor = Color(0xFFFFFFFF)
                                                )
                                            )
                                        },
                                        state = tooltipState
                                    ) {
                                        IconButton(
                                            onClick = {
                                                scope.launch {
                                                    tooltipState.show()
                                                }
                                            },
                                            modifier = Modifier
                                                .size(20.dp)
                                        ) {
                                            Icon(
                                                imageVector = ImageVector.vectorResource(R.drawable.info),
                                                contentDescription = "",
                                                tint = Color(0xFFADADAD)
                                            )
                                        }
                                    }
                                }
                                AutocompleteTextFieldForRegion(
                                    modifier = Modifier.heightIn(min = 0.dp, max = 300.dp),
                                    value = selectedRegion,
                                    placesClient = placesClient,
                                    onPlaceSelected = { value ->
                                        selectedRegion = value
                                        isRegionSelected = true
                                    },
                                    onValueChange = { value ->
                                        selectedRegion = value
                                        isRegionSelected = false
                                        isSelectedRegionEmpty = selectedRegion.isEmpty()
                                    },
                                    placeholder = localization["region_placeholder"]!!
                                )
                            }
                            if (isSelectedRegionEmpty) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    localization["region_empty"]!!,
                                    color = Color(0xFFB71C1C),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Spacer(modifier = Modifier.size(32.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = localization["phone_title"]!!,
                                fontSize = 24.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal
                            )
                            StyledBasicTextField(
                                value = phoneNumber,
                                onValueChange = {
                                    phoneNumber = it
                                    isPhoneNumberEmpty = phoneNumber.isEmpty()
                                    phoneNumberError = !isValidPhoneNumber(it)
                                },
                                placeholder = localization["phone_placeholder"]!!
                            )
                        }
                        if (isPhoneNumberEmpty) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                localization["phone_empty"]!!,
                                color = Color(0xFFB71C1C),
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else if (phoneNumberError) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                localization["phone_invalid"]!!,
                                color = Color(0xFFB71C1C),
                                style = MaterialTheme.typography.bodySmall
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
                                            Icons.Rounded.Visibility
                                        } else {
                                            Icons.Rounded.VisibilityOff
                                        },
                                        tint = Color(0xFFADADAD)
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
                                isVisible = isPasswordVisible
                            )
                        }
                        if (isPasswordEmpty) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                localization["password_empty"]!!,
                                color = Color(0xFFB71C1C),
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else if (passwordError) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                localization["password_length"]!!,
                                color = Color(0xFFB71C1C),
                                style = MaterialTheme.typography.bodySmall
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
                                    fontWeight = FontWeight.Normal
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
                                        tint = Color(0xFFADADAD)
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
                                isVisible = isConfirmPasswordVisible
                            )
                        }
                        if (isConfirmPasswordEmpty) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                localization["confirm_password_empty"]!!,
                                color = Color(0xFFB71C1C),
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else if (confirmPasswordError) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                localization["passwords_do_not_match"]!!,
                                color = Color(0xFFB71C1C),
                                style = MaterialTheme.typography.bodySmall
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
                                isSelectedRegionEmpty = selectedRegion.isEmpty()
                                if(isSelectedRegionEmpty) {
                                    Toast.makeText(context, localization["region_select_error"]!!, Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                if(!isRegionSelected) {
                                    Toast.makeText(context, localization["region_select_error"]!!, Toast.LENGTH_LONG).show()
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
                                if(croppedImageUri == null) {
                                    Toast.makeText(context, localization["re_add_avatar_error"]!!, Toast.LENGTH_LONG).show()
                                    return@Button
                                } else {
                                    authViewModel.signup(
                                        email = email,
                                        password = password,
                                        name = name,
                                        phoneNumber = phoneNumber.replace(" ", ""),
                                        region = selectedRegion,
                                        photo = userPhoto,
                                        context = context,
                                        coroutineScope = coroutineScope,
                                        localization = localization
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
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
                                    text = localization["sign_up_button"]!!,
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
                                    navController.navigate(Routes.LOGIN)
                                }
                            ) {
                                Text(
                                    modifier = Modifier,
                                    text = localization["login_here_button"]!!,
                                    fontSize = 14.sp,
                                    color = Color(0xFF6369FF),
                                    fontFamily = RubikFont,
                                    fontWeight = FontWeight.Normal
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
                        .background(Color(0xFFEFF1F3))
                        .clickable {
                            getContent.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isImageSelected) {
                        Image(
                            painter = rememberAsyncImagePainter(croppedImageUri),
                            contentDescription = "",
                            modifier = Modifier.size(100.dp)
                        )
                    } else {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.add_photo_alternate),
                            contentDescription = "",
                            tint = Color(0xFF606060),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    }
}

fun cropImageToSquare(uri: Uri, contentResolver: ContentResolver): Bitmap? {
    val inputStream: InputStream? = contentResolver.openInputStream(uri)
    val originalBitmap = BitmapFactory.decodeStream(inputStream)

    return originalBitmap?.let {
        val width = it.width
        val height = it.height

        val size = if (width > height) height else width

        Bitmap.createBitmap(it, 0, 0, size, size)
    }
}

fun bitmapToUri(context: Context, bitmap: Bitmap): Uri? {
    val file = File(context.cacheDir, "profile_photo_${System.currentTimeMillis()}.jpg")

    try {
        val fileOutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()

        return Uri.fromFile(file)
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }
}