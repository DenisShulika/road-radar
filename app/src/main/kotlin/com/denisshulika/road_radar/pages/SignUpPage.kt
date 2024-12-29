package com.denisshulika.road_radar.pages

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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
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
import com.denisshulika.road_radar.isValidEmail
import com.denisshulika.road_radar.isValidPhoneNumber
import com.denisshulika.road_radar.ui.components.StyledBasicTextField
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

@Composable
fun SignUpPage(
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

    val authState = authViewModel.authState.observeAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    @Suppress("SpellCheckingInspection") val regionsByArea = mapOf(
        "Avtonomna Respublika Krym" to listOf("Bakhchysaraiskyi", "Bilogirskyi", "Dzhankoyskyi", "Yevpatoriiskyi", "Kerchenskyi", "Kurmanskyi", "Perekopskyi", "Simferopolskyi", "Feodosiiskyi", "Yaltynskyi"),
        "Vinnytska" to listOf("Vinnytskyi", "Haisynskyi", "Zhmerynskyi", "Mohyliv-Podilskyi", "Tulchynskyi", "Khmilnytskyi"),
        "Volynska" to listOf("Volodymyr-Volynskyi", "Kamin-Kashyrskyi", "Kovelskyi", "Lutskyi"),
        "Dnipropetrovska" to listOf("Dniprovskiyi", "Kamianchskyi", "Kryvorizkyi", "Nikopolskyi", "Novomoskovskyi", "Pavlohradskiyi", "Synelnykivskyi"),
        "Donetska" to listOf("Bakhmutskyi", "Volnovaskyi", "Horlivskyi", "Donetskyi", "Kalmiuskyi", "Kramatorskyi", "Mariupolskyi", "Pokrovskyi"),
        "Zhytomyrska" to listOf("Berdychivskyi", "Zhytomyrskyi", "Korostenskyi", "Novograd-Volynskyi"),
        "Zakarpatska" to listOf("Beregivskyi", "Mukachivskyi", "Rakhivskyi", "Tyachivskyi", "Uzhhorodskyi", "Khustskyi"),
        "Zaporizka" to listOf("Berdyanskyi", "Vasylivskyi", "Zaporizkyi", "Melitopolskyi", "Polohivskyi"),
        "Ivano-Frankivska" to listOf("Verkhovynskyi", "Ivano-Frankivskyi", "Kalushskyi", "Kolomyiskyi", "Kosivskyi", "Nadvirnianskyi"),
        "Kyivska" to listOf("Bilotserkivskyi", "Boryspilskyi", "Brovarskyi", "Buchanskyi", "Vyshhorodskyi", "Obukhivskyi", "Fastivskyi"),
        "Kirovohradska" to listOf("Holovanyivskyi", "Kropyvnytskyi", "Novoukrainskyi", "Oleksandriiskyi"),
        "Luhanska" to listOf("Alchevskyi", "Dovzhanskyi", "Luhanskyi", "Rovenkivskyi", "Svatovskyi", "Severodonetskyi", "Starobilskyi", "Shchastynskyi"),
        "Lvivska" to listOf("Drohobychskyi", "Zolochivskyi", "Lvivskyi", "Sambirskyi", "Striiskyi", "Chervonogradskyi", "Yavorivskyi"),
        "Mykolaivska" to listOf("Bashtanskyi", "Voznesenskyi", "Mykolaivskyi", "Pervomaiskyi"),
        "Odeska" to listOf("Berezivskyi", "Bilhorod-Dnistrovskyi", "Bolhradskyi", "Izmailskyi", "Odeskyi", "Podilskyi", "Rozdilnianskyi"),
        "Poltavska" to listOf("Kremenchutskyi", "Lubenskyi", "Myrohorodskyi", "Poltavskyi"),
        "Rivnenska" to listOf("Varashskyi", "Dubenskyi", "Rivnenskyi", "Sarnenskyi"),
        "Sumska" to listOf("Konotopskyi", "Okhtyrskyi", "Romenskyi", "Sumskyi", "Shostkynskyi"),
        "Ternopilska" to listOf("Kremenetskyi", "Ternopilskyi", "Chortkivskyi"),
        "Kharkivska" to listOf("Bohodukhivskyi", "Iziumskyi", "Krasnohradskyi", "Kupyanskyi", "Lozivskyi", "Kharkivskyi", "Chuhuivskyi"),
        "Khersonska" to listOf("Beryslavskyi", "Henicheskyi", "Kakhovsky", "Skadovsky", "Khersonskiy"),
        "Khmelnytska" to listOf("Kamianets-Podilskyi", "Khmelnytskyi", "Shepetivskyi"),
        "Cherkaska" to listOf("Zvenyhorodskyi", "Zolotoniskyi", "Umanskyi", "Cherkaskyi"),
        "Chernivetska" to listOf("Vyzhnytskyi", "Dnistrovskyi", "Chernivetskyi"),
        "Chernihivska" to listOf("Koriukivskyi", "Nizhynskyi", "Novhorod-Siverskyi", "Prilutskyi", "Chernihivskyi"),
        "Misto Kyiv" to listOf("Holosiivskyi", "Darnytskyi", "Desnianskyi", "Dniprovskiyi", "Obolonskyi", "Pecherskyi", "Podilskyi", "Sviatoshynskyi", "Solomianskyi", "Shevchenkivskyi"),
        "Misto Simferopol" to listOf("Leninskyi", "Balaklavskyi", "Haharinskyi", "Nakhimovskyi")
    )
    val areas = regionsByArea.keys.toList()


    var name by remember { mutableStateOf("") }
    var isNameEmpty by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var isEmailEmpty by remember { mutableStateOf(false) }

    var phoneNumber by remember { mutableStateOf("") }
    var phoneNumberError by remember { mutableStateOf(false) }
    var isPhoneNumberEmpty by remember { mutableStateOf(false) }

    var selectedArea by remember { mutableStateOf<String?>(null) }
    val isAreaDropdownExpanded = remember { mutableStateOf(false) }
    val areaItemPosition = remember { mutableIntStateOf(0) }

    var selectedRegion by remember { mutableStateOf<String?>(null) }
    val isRegionDropdownExpanded = remember { mutableStateOf(false) }
    val regionItemPosition = remember { mutableIntStateOf(0) }

    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }
    var isPasswordEmpty by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf(false) }
    var isConfirmPasswordEmpty by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    var croppedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isImageSelected by remember { mutableStateOf(false) }
    val getContent = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            isImageSelected = true

            val croppedBitmap = cropImageToSquare(it, context.contentResolver)
            croppedImageUri = if (croppedBitmap != null) bitmapToUri(context, croppedBitmap) else null
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
                    text = "Sign Up",
                    fontSize = 64.sp,
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
                                bottom = 20.dp)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Text(
                                text = "Name",
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
                                placeholder = "Your Name, e.g: John Doe"
                            )
                        }
                        if (isNameEmpty) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Name cant be empty",
                                color = Color(0xFFB71C1C),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.size(32.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(20.dp)
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
                                placeholder = "Your email, e.g: john@gmail.com",
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
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Text(
                                text = "Phone Number",
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
                                placeholder = "Your phone, e.g: +380 xx xxx xx xx"
                            )
                        }
                        if (isPhoneNumberEmpty) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Phone number cant be empty",
                                color = Color(0xFFB71C1C),
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else if (phoneNumberError) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Invalid phone number",
                                color = Color(0xFFB71C1C),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.size(32.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Text(
                                text = "Your area",
                                fontSize = 24.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .drawBehind {
                                        val strokeWidth = 1.dp.toPx()
                                        val y = size.height - strokeWidth / 2
                                        drawLine(
                                            color = Color(0xFFADADAD),
                                            start = Offset(0f, 0.75f * y),
                                            end = Offset(size.width, 0.75f * y),
                                            strokeWidth = strokeWidth
                                        )
                                    },
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .height(36.dp)
                                        .fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clickable {
                                                isAreaDropdownExpanded.value = true
                                            }
                                            .fillMaxWidth()
                                    ) {
                                        Text(
                                            text = selectedArea?.takeIf { it.isNotBlank() } ?: "Choose your area",
                                            style = TextStyle(
                                                color = Color(0xFFADADAD),
                                                fontSize = 20.sp,
                                                lineHeight = 20.sp
                                            ),
                                            fontFamily = RubikFont,
                                            fontWeight = FontWeight.Normal
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = ""
                                        )
                                    }
                                    DropdownMenu(
                                        modifier = Modifier
                                            .background(Color(0xFF474EFF)),
                                        expanded = isAreaDropdownExpanded.value,
                                        onDismissRequest = {
                                            isAreaDropdownExpanded.value = false
                                        }) {
                                        areas.forEachIndexed { index, area ->
                                            DropdownMenuItem(text = {
                                                Text(
                                                    text = area,
                                                    style = TextStyle(
                                                        color = Color(0xFFFFFFFF),
                                                        fontSize = 20.sp,
                                                        lineHeight = 20.sp
                                                    ),
                                                    fontFamily = RubikFont,
                                                    fontWeight = FontWeight.Normal
                                                )
                                            },
                                                onClick = {
                                                    isAreaDropdownExpanded.value = false
                                                    areaItemPosition.intValue = index
                                                    selectedArea = area
                                                    selectedRegion = null
                                                })
                                        }
                                    }
                                }

                            }
                        }
                        Spacer(modifier = Modifier.size(32.dp))
                        if (selectedArea != null) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                Text(
                                    text = "Your district",
                                    fontSize = 24.sp,
                                    fontFamily = RubikFont,
                                    fontWeight = FontWeight.Normal
                                )
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .drawBehind {
                                            val strokeWidth = 1.dp.toPx()
                                            val y = size.height - strokeWidth / 2
                                            drawLine(
                                                color = Color(0xFFADADAD),
                                                start = Offset(0f, 0.75f * y),
                                                end = Offset(size.width, 0.75f * y),
                                                strokeWidth = strokeWidth
                                            )
                                        },
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .height(36.dp)
                                            .fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clickable {
                                                    isRegionDropdownExpanded.value = true
                                                }
                                                .fillMaxWidth()
                                        ) {
                                            Text(
                                                text = selectedRegion?.takeIf { it.isNotBlank() } ?: "Choose your district",
                                                style = TextStyle(
                                                    color = Color(0xFFADADAD),
                                                    fontSize = 20.sp,
                                                    lineHeight = 20.sp
                                                ),
                                                fontFamily = RubikFont,
                                                fontWeight = FontWeight.Normal
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = ""
                                            )
                                        }
                                        DropdownMenu(
                                            modifier = Modifier
                                                .background(Color(0xFF474EFF)),
                                            expanded = isRegionDropdownExpanded.value,
                                            onDismissRequest = {
                                                isRegionDropdownExpanded.value = false
                                            }) {
                                            regionsByArea[selectedArea]?.forEachIndexed { index, region ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = region,
                                                            style = TextStyle(
                                                                color = Color(0xFFFFFFFF),
                                                                fontSize = 20.sp,
                                                                lineHeight = 20.sp
                                                            ),
                                                            fontFamily = RubikFont,
                                                            fontWeight = FontWeight.Normal
                                                        )
                                                    },
                                                    onClick = {
                                                        isRegionDropdownExpanded.value = false
                                                        regionItemPosition.intValue = index
                                                        selectedRegion = region
                                                    })
                                            }
                                        }
                                    }

                                }
                            }
                            Spacer(modifier = Modifier.size(32.dp))
                        }
                        Column(
                            verticalArrangement = Arrangement.spacedBy(20.dp)
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
                                placeholder = "Your password, at least 6 symbols",
                                isVisible = isPasswordVisible
                            )
                        }
                        if (isPasswordEmpty) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Email cant be empty",
                                color = Color(0xFFB71C1C),
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else if (passwordError) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Password must be at least 6 symbols",
                                color = Color(0xFFB71C1C),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.size(32.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Confirm Password",
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
                                placeholder = "Re-type your password",
                                isVisible = isConfirmPasswordVisible
                            )
                        }
                        if (isConfirmPasswordEmpty) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Email cant be empty",
                                color = Color(0xFFB71C1C),
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else if (confirmPasswordError) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Passwords don't match",
                                color = Color(0xFFB71C1C),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.size(24.dp))
                        Button(
                            onClick = {
                                isNameEmpty = name.isEmpty()
                                if(isNameEmpty) {
                                    Toast.makeText(context, "Please, enter your name", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                isEmailEmpty = email.isEmpty()
                                if(isEmailEmpty) {
                                    Toast.makeText(context, "Please, enter your email", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                emailError = !isValidEmail(email)
                                if(emailError) {
                                    Toast.makeText(context, "Please, enter correct email address", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                isPhoneNumberEmpty = phoneNumber.isEmpty()
                                if(isPhoneNumberEmpty) {
                                    Toast.makeText(context, "Please, enter your phone", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                phoneNumberError = !isValidPhoneNumber(phoneNumber)
                                if(phoneNumberError) {
                                    Toast.makeText(context, "Please, enter correct phone number", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                if(selectedArea == null) {
                                    Toast.makeText(context, "Please, select your area", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                if(selectedRegion == null) {
                                    Toast.makeText(context, "Please, select your region", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                isPasswordEmpty = password.isEmpty()
                                if(isPasswordEmpty) {
                                    Toast.makeText(context, "Please, enter a password", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                passwordError = password.length < 6
                                if(passwordError) {
                                    Toast.makeText(context, "Password must be at least 6 symbols", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                isConfirmPasswordEmpty = confirmPassword.isEmpty()
                                if(isConfirmPasswordEmpty) {
                                    Toast.makeText(context, "Please, confirm your password", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                confirmPasswordError = password != confirmPassword
                                if(confirmPasswordError) {
                                    Toast.makeText(context, "Passwords don't match", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                if(!isImageSelected) {
                                    Toast.makeText(context, "Please, add your avatar", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                if(croppedImageUri == null) {
                                    Toast.makeText(context, "Please, re-add your avatar", Toast.LENGTH_LONG).show()
                                    return@Button
                                } else {
                                    authViewModel.signup(
                                        email = email,
                                        password = password,
                                        name = name,
                                        phoneNumber = phoneNumber.replace(" ", ""),
                                        area = selectedArea!!,
                                        region = selectedRegion!!,
                                        photo = croppedImageUri!!,
                                        context,
                                        coroutineScope
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
                                    text = "Sign Up",
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
                                    text = "Already have an account? Login here",
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
                    if (croppedImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(croppedImageUri),
                            contentDescription = "Cropped Image",
                            modifier = Modifier.size(100.dp)
                        )
                    } else {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.add_photo_alternate),
                            contentDescription = "Upload Photo",
                            tint = Color(0xFF606060),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    }
}

fun cropImageToSquare(uri: Uri, contentResolver: android.content.ContentResolver): Bitmap? {
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