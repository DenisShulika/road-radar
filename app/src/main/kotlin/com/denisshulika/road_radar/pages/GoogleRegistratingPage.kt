package com.denisshulika.road_radar.pages

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.denisshulika.road_radar.AuthState
import com.denisshulika.road_radar.AuthViewModel
import com.denisshulika.road_radar.IncidentManager
import com.denisshulika.road_radar.R
import com.denisshulika.road_radar.Routes
import com.denisshulika.road_radar.SettingsViewModel
import com.denisshulika.road_radar.isValidPhoneNumber
import com.denisshulika.road_radar.model.ThemeState
import com.denisshulika.road_radar.ui.components.AutocompleteTextFieldForRegion
import com.denisshulika.road_radar.ui.components.StyledBasicTextField
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleRegistratingPage(
    @Suppress("UNUSED_PARAMETER") modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    placesClient: PlacesClient,
    incidentManager: IncidentManager
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val systemUiController = rememberSystemUiController()

    val localization = settingsViewModel.localization.observeAsState().value!!
    val theme = settingsViewModel.themeColors.observeAsState().value!!

    systemUiController.setStatusBarColor(
        color = Color.Transparent,
        darkIcons = settingsViewModel.getTheme() != ThemeState.DARK || !isSystemInDarkTheme()
    )
    systemUiController.setNavigationBarColor(
        color = theme["background"]!!,
        darkIcons = settingsViewModel.getTheme() != ThemeState.DARK || !isSystemInDarkTheme()
    )

    val authState = authViewModel.authState.observeAsState()

    var phoneNumber by remember { mutableStateOf("") }
    var phoneNumberError by remember { mutableStateOf(false) }
    var isPhoneNumberEmpty by remember { mutableStateOf(false) }

    var selectedRegion by remember { mutableStateOf("") }
    var isRegionSelected by remember { mutableStateOf(false) }
    var isSelectedRegionEmpty by remember { mutableStateOf(false) }

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

    val tooltipState = rememberTooltipState()
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(painterResource(id = if (settingsViewModel.getTheme() == ThemeState.DARK) R.drawable.auth_dark_background else R.drawable.auth_light_background), contentScale = ContentScale.Crop)
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
                    text = localization["google_sign_in_title_1"]!!,
                    fontSize = 52.sp,
                    color = theme["text"]!!,
                    fontFamily = RubikFont,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = localization["google_sign_in_title_2"]!!,
                    fontSize = 52.sp,
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
                                text = localization["google_sign_in_info"]!!,
                                fontSize = 14.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal,
                                color = theme["placeholder"]!!
                            )
                        }
                        Spacer(modifier = Modifier.size(20.dp))
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
                                        fontWeight = FontWeight.Normal,
                                        color = theme["text"]!!
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))

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
                                                    containerColor = theme["primary"]!!,
                                                    contentColor = theme["text"]!!,
                                                    titleContentColor = theme["text"]!!,
                                                    actionContentColor = theme["text"]!!
                                                )
                                            )
                                        },
                                        state = tooltipState
                                    ) {
                                        IconButton(
                                            onClick = { scope.launch { tooltipState.show() } },
                                            modifier = Modifier
                                                .size(20.dp)
                                        ) {
                                            Icon(
                                                imageVector = ImageVector.vectorResource(R.drawable.info),
                                                contentDescription = "",
                                                tint = theme["icon"]!!
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
                                    placeholder = localization["region_placeholder"]!!,
                                    settingsViewModel = settingsViewModel
                                )
                            }
                            if (isSelectedRegionEmpty) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    localization["region_empty"]!!,
                                    color = theme["error"]!!,
                                    fontSize = 12.sp,
                                    fontFamily = RubikFont,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                        Spacer(modifier = Modifier.size(32.dp))
                        Column (
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
                        Button(
                            onClick = {
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
                                isSelectedRegionEmpty = selectedRegion.isEmpty()
                                if(isSelectedRegionEmpty) {
                                    Toast.makeText(context, localization["region_enter_error"]!!, Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                if(!isRegionSelected) {
                                    Toast.makeText(context, localization["region_select_error"]!!, Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                authViewModel.completeRegistrationViaGoogle(
                                    phoneNumber,
                                    selectedRegion,
                                    context,
                                    coroutineScope,
                                    localization
                                )
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
                                    text = localization["complete_google_sign_in_button"]!!,
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
                                    authViewModel.deleteAccount(
                                        email = "",
                                        password = "",
                                        context = context,
                                        coroutineScope = coroutineScope,
                                        incidentManager = incidentManager,
                                        localization = localization
                                    )
                                }
                            ) {
                                Text(
                                    modifier = Modifier,
                                    text = localization["terminate_google_sign_in_button"]!!,
                                    fontSize = 14.sp,
                                    color = theme["primary"]!!,
                                    fontFamily = RubikFont,
                                    fontWeight = FontWeight.Medium,
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
