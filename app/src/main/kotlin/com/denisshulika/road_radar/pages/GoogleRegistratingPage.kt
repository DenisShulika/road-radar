package com.denisshulika.road_radar.pages

import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.denisshulika.road_radar.AuthState
import com.denisshulika.road_radar.AuthViewModel
import com.denisshulika.road_radar.R
import com.denisshulika.road_radar.Routes
import com.denisshulika.road_radar.isValidPhoneNumber
import com.denisshulika.road_radar.ui.components.StyledBasicTextField
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.io.InputStreamReader

@Composable
fun GoogleRegistratingPage(
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

    val regionsByArea = loadRegionsFromJson(context)
    val areas = regionsByArea.keys.toList()

    var phoneNumber by remember { mutableStateOf("") }
    var phoneNumberError by remember { mutableStateOf(false) }
    var isPhoneNumberEmpty by remember { mutableStateOf(false) }

    var selectedArea by remember { mutableStateOf<String?>(null) }
    val isAreaDropdownExpanded = remember { mutableStateOf(false) }
    val areaItemPosition = remember { mutableIntStateOf(0) }

    var selectedRegion by remember { mutableStateOf<String?>(null) }
    val isRegionDropdownExpanded = remember { mutableStateOf(false) }
    val regionItemPosition = remember { mutableIntStateOf(0) }

    LaunchedEffect(authState.value) {
        authViewModel.checkAuthStatus()
        when(authState.value) {
            is AuthState.Authenticated ->
                navController.navigate(Routes.INCIDENTS)
            is AuthState.Unauthenticated ->
                navController.navigate(Routes.LOGIN)
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
                    text = "Sign in",
                    fontSize = 64.sp,
                    color = Color.White,
                    fontFamily = RubikFont,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "with Google",
                    fontSize = 64.sp,
                    color = Color.White,
                    fontFamily = RubikFont,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Box (
                modifier = Modifier
                    .weight(1f)
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
                            .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 10.dp)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.size(8.dp))
                        Column (
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "To complete Google sign in, please provide us with additional information",
                                fontSize = 14.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFFADADAD)
                            )
                        }
                        Spacer(modifier = Modifier.size(20.dp))
                        Column (
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
                                placeholder = "Your phone, e.g: +380.. or 0.."
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
                                            color = Color(0xFFD3D3D3),
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
                                                color = if (selectedArea != null) Color(0xFF000000) else Color(0xFFADADAD),
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

                        if (selectedArea != null) {
                            Spacer(modifier = Modifier.size(32.dp))
                            Column (
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                Text(
                                    text = "Your region",
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
                                                color = Color(0xFFD3D3D3),
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
                                                text = selectedRegion?.takeIf { it.isNotBlank() } ?: "Choose your region",
                                                style = TextStyle(
                                                    color = if (selectedRegion != null) Color(0xFF000000) else Color(0xFFADADAD),
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

                        }
                        Spacer(modifier = Modifier.size(24.dp))
                        Button(
                            onClick = {
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
                                } else if(selectedRegion == null) {
                                    Toast.makeText(context, "Please, select your region", Toast.LENGTH_LONG).show()
                                    return@Button
                                } else {
                                    authViewModel.completeRegistrationViaGoogle(
                                        phoneNumber,
                                        selectedArea!!,
                                        selectedRegion!!,
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
                                    text = "Complete Signing Up",
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
                                    authViewModel.deleteAccount(
                                        email = "",
                                        password = "",
                                        context = context,
                                        coroutineScope = coroutineScope
                                    )
                                }
                            ) {
                                Text(
                                    modifier = Modifier,
                                    text = "Changed your mind about signing in with Google?",
                                    fontSize = 14.sp,
                                    color = Color(0xFF6369FF),
                                    fontFamily = RubikFont,
                                    fontWeight = FontWeight.Normal,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Spacer(modifier = Modifier.size(4.dp))
                    }
                }
            }
        }
    }
}

fun loadRegionsFromJson(context: Context): Map<String, List<String>> {
    val inputStream = context.assets.open("regions.json")
    val reader = InputStreamReader(inputStream)
    return Gson().fromJson(reader, object : TypeToken<Map<String, List<String>>>() {}.type)
}
