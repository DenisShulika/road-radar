package com.denisshulika.road_radar.pages

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.denisshulika.road_radar.AuthViewModel
import com.denisshulika.road_radar.IncidentCreationState
import com.denisshulika.road_radar.IncidentManager
import com.denisshulika.road_radar.Routes
import com.denisshulika.road_radar.SettingsViewModel
import com.denisshulika.road_radar.model.CustomDrawerState
import com.denisshulika.road_radar.model.IncidentType
import com.denisshulika.road_radar.model.ThemeState
import com.denisshulika.road_radar.ui.components.AutocompleteTextFieldForAddress
import com.denisshulika.road_radar.ui.components.AutocompleteTextFieldForRegion
import com.denisshulika.road_radar.ui.components.StyledBasicTextField
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.libraries.places.api.net.PlacesClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewIncidentPage(
    @Suppress("UNUSED_PARAMETER") modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    incidentManager: IncidentManager,
    placesClient: PlacesClient
) {
    val context = LocalContext.current

    val localization = settingsViewModel.localization.observeAsState().value!!
    val theme = settingsViewModel.themeColors.observeAsState().value!!

    val incidentTypes = listOf(
        localization["incident_type_car_accident"]!!,
        localization["incident_type_roadblock"]!!,
        localization["incident_type_weather_conditions"]!!,
        localization["incident_type_traffic_jam"]!!,
        localization["incident_type_other"]!!
    )

    val incidentCreationState = incidentManager.incidentCreationState.observeAsState()

    LaunchedEffect(incidentCreationState.value) {
        when(incidentCreationState.value) {
            is IncidentCreationState.Success -> {
                navController.navigate(Routes.INCIDENTS)
                incidentManager.setIncidentCreationState(IncidentCreationState.Null)
            }
            is IncidentCreationState.Error -> {
                Toast.makeText(context, (incidentCreationState.value as IncidentCreationState.Error).message, Toast.LENGTH_LONG).show()
                incidentManager.setIncidentCreationState(IncidentCreationState.Null)
            }
            else -> Unit
        }
    }

    var selectedIncidentType by remember { mutableStateOf<IncidentType?>(null) }
    var isIncidentTypeDropdownExpanded by remember { mutableStateOf(false) }

    var incidentDescription by remember { mutableStateOf("") }
    var isIncidentDescriptionEmpty by remember { mutableStateOf(false) }

    var incidentPhotos by remember { mutableStateOf<List<Uri?>>(emptyList()) }

    var selectedRegion by remember { mutableStateOf("") }
    var isRegionSelected by remember { mutableStateOf(false) }
    var isSelectedRegionEmpty by remember { mutableStateOf(false) }

    var selectedAddress by remember { mutableStateOf("") }
    var isAddressSelected by remember { mutableStateOf(false) }
    var isSelectedAddressEmpty by remember { mutableStateOf(false) }

    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }

    val getContent = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(
            maxItems = 3
        ),
        onResult = { uriList ->
            val imageUris = uriList.filter { uri ->
                val mimeType = context.contentResolver.getType(uri)
                if (mimeType?.startsWith("image/") == false) {
                    Toast.makeText(context, localization["select_videos_error"], Toast.LENGTH_SHORT).show()
                }
                mimeType?.startsWith("image/") == true

            }

            if (imageUris.size + incidentPhotos.size <= 3) {
                incidentPhotos = incidentPhotos + imageUris
            } else {
                Toast.makeText(context, localization["photo_limit_error"], Toast.LENGTH_SHORT).show()
            }
        }
    )

    val systemUiController = rememberSystemUiController()

    systemUiController.setStatusBarColor(
        color = theme["top_bar_background"]!!,
        darkIcons = settingsViewModel.getTheme() != ThemeState.DARK || !isSystemInDarkTheme()
    )
    systemUiController.setNavigationBarColor(
        color = theme["background"]!!,
        darkIcons = settingsViewModel.getTheme() != ThemeState.DARK || !isSystemInDarkTheme()
    )

    Box(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = theme["top_bar_background"]!!,
                        titleContentColor = theme["text"]!!,
                        navigationIconContentColor = theme["icon"]!!
                    ),
                    title = {
                        Text(
                            text = localization["add_new_incident_title"]!!,
                            textAlign = TextAlign.Center,
                            fontFamily = RubikFont,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                navController.popBackStack()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = ""
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(theme["background"]!!)
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = localization["incident_type_title"]!!,
                            fontSize = 24.sp,
                            fontFamily = RubikFont,
                            color = theme["text"]!!
                        )
                        Column(
                            modifier = Modifier
                                .drawBehind {
                                    val strokeWidth = 1.dp.toPx()
                                    val y = size.height - strokeWidth / 2
                                    drawLine(
                                        color = theme["placeholder"]!!,
                                        start = Offset(0f, 0.75f * y),
                                        end = Offset(size.width, 0.75f * y),
                                        strokeWidth = strokeWidth
                                    )
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(36.dp)
                                    .fillMaxWidth()
                                    .clickable { isIncidentTypeDropdownExpanded = true }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = when(selectedIncidentType) {
                                            null -> localization["incident_type_placeholder"]!!
                                            IncidentType.CAR_ACCIDENT -> localization["incident_type_car_accident"]!!
                                            IncidentType.ROADBLOCK -> localization["incident_type_roadblock"]!!
                                            IncidentType.WEATHER_CONDITIONS -> localization["incident_type_weather_conditions"]!!
                                            IncidentType.TRAFFIC_JAM -> localization["incident_type_traffic_jam"]!!
                                            IncidentType.OTHER -> localization["incident_type_other"]!!
                                        },
                                        color = if (selectedIncidentType != null) theme["text"]!! else theme["placeholder"]!!,
                                        fontSize = 22.sp,
                                        fontFamily = RubikFont
                                    )
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "")
                                }
                                DropdownMenu(
                                    modifier = Modifier.background(theme["primary"]!!),
                                    expanded = isIncidentTypeDropdownExpanded,
                                    onDismissRequest = { isIncidentTypeDropdownExpanded = false }
                                ) {
                                    incidentTypes.forEach { type ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = type,
                                                    color = Color.White,
                                                    fontSize = 20.sp,
                                                    fontFamily = RubikFont
                                                )
                                            },
                                            onClick = {
                                                selectedIncidentType = when(type) {
                                                    localization["incident_type_car_accident"] -> IncidentType.CAR_ACCIDENT
                                                    localization["incident_type_roadblock"] -> IncidentType.ROADBLOCK
                                                    localization["incident_type_weather_conditions"] -> IncidentType.WEATHER_CONDITIONS
                                                    localization["incident_type_traffic_jam"] -> IncidentType.TRAFFIC_JAM
                                                    localization["incident_type_other"] -> IncidentType.OTHER
                                                    else -> IncidentType.OTHER
                                                }
                                                isIncidentTypeDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = localization["description_title"]!!,
                            fontSize = 24.sp,
                            fontFamily = RubikFont,
                            color = theme["text"]!!
                        )
                        StyledBasicTextField(
                            value = incidentDescription,
                            onValueChange = {
                                incidentDescription = it
                                isIncidentDescriptionEmpty = incidentDescription.isEmpty()
                            },
                            placeholder = localization["description_placeholder"]!!,
                            singleLine = false,
                            theme = theme
                        )
                    }
                    if (isIncidentDescriptionEmpty) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            localization["description_empty_text"]!!,
                            color = theme["error"]!!,
                            fontSize = 12.sp,
                            fontFamily = RubikFont,
                            fontWeight = FontWeight.Normal
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Column {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = localization["region_title"]!!,
                                fontSize = 24.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal,
                                color = theme["text"]!!
                            )
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

                                    selectedAddress = ""
                                    isAddressSelected = false
                                    isSelectedAddressEmpty = false
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
                    Spacer(modifier = Modifier.height(32.dp))
                    Column {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = localization["address_title"]!!,
                                fontSize = 24.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal,
                                color = theme["text"]!!
                            )
                            if (!isRegionSelected) {
                                Text(
                                    text = localization["enter_region_firstly"]!!,
                                    fontSize = 20.sp,
                                    fontFamily = RubikFont,
                                    fontWeight = FontWeight.Normal,
                                    color = theme["placeholder"]!!
                                )
                            } else {
                                AutocompleteTextFieldForAddress(
                                    modifier = Modifier.heightIn(min = 0.dp, max = 300.dp),
                                    value = selectedAddress,
                                    placesClient = placesClient,
                                    onPlaceSelected = { value, latitudeVal, longitudeVal ->
                                        selectedAddress = value
                                        isAddressSelected = true

                                        latitude = latitudeVal.toString()
                                        longitude = longitudeVal.toString()
                                    },
                                    onValueChange = { value ->
                                        selectedAddress = value
                                        isAddressSelected = false
                                        isSelectedAddressEmpty = selectedAddress.isEmpty()
                                    },
                                    placeholder = localization["address_placeholder"]!!,
                                    region = selectedRegion,
                                    context = context,
                                    settingsViewModel = settingsViewModel
                                )
                            }
                        }
                        if (isSelectedAddressEmpty) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                localization["address_empty"]!!,
                                color = theme["error"]!!,
                                fontSize = 12.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = localization["photos_title"]!!,
                            fontSize = 24.sp,
                            fontFamily = RubikFont,
                            color = theme["text"]!!
                        )
                        Column {
                            if (incidentPhotos.isEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = localization["no_photos_selected"]!!,
                                        fontSize = 16.sp,
                                        fontFamily = RubikFont,
                                        color = theme["placeholder"]!!
                                    )
                                }
                            }
                            incidentPhotos.forEachIndexed { index, uri ->
                                val fileName = getFileNameFromUri(
                                    uri = uri ?: Uri.EMPTY,
                                    context = context,
                                    localization = localization
                                )
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            AsyncImage(
                                                model = uri,
                                                contentDescription = "",
                                                modifier = Modifier
                                                    .size(50.dp)
                                                    .padding(end = 8.dp)
                                            )
                                            Text(
                                                text = fileName,
                                                fontSize = 18.sp,
                                                fontFamily = RubikFont,
                                                color = theme["text"]!!
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                incidentPhotos = incidentPhotos.toMutableList().apply { removeAt(index) }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Delete,
                                                contentDescription = "",
                                                tint = theme["error"]!!
                                            )
                                        }
                                    }
                                    if (index < incidentPhotos.size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier
                                                .padding(vertical = 8.dp, horizontal = 36.dp),
                                            thickness = 1.dp,
                                            color = theme["accent"]!!
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (incidentPhotos.size < 3) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            TextButton(
                                onClick = {
                                    getContent.launch(
                                        PickVisualMediaRequest()
                                    )
                                },
                                enabled = incidentCreationState.value != IncidentCreationState.Loading
                            ) {
                                Text(
                                    text = localization["add_photos_button"]!!,
                                    fontSize = 20.sp,
                                    color = theme["primary"]!!,
                                    fontFamily = RubikFont
                                )
                                Spacer(modifier = Modifier.size(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "",
                                    tint = theme["primary"]!!
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        onClick = {
                            if (selectedIncidentType == null) {
                                Toast.makeText(context, localization["type_error"], Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            isIncidentDescriptionEmpty = incidentDescription.isEmpty()
                            if (isIncidentDescriptionEmpty) {
                                Toast.makeText(context, localization["description_error"], Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            isSelectedRegionEmpty = selectedRegion.isEmpty()
                            if(isSelectedRegionEmpty) {
                                Toast.makeText(context, localization["region_enter_error"], Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            if(!isRegionSelected) {
                                Toast.makeText(context, localization["region_select_error"], Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            isSelectedAddressEmpty = selectedAddress.isEmpty()
                            if(isSelectedAddressEmpty) {
                                Toast.makeText(context, localization["address_enter_error"], Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            if(!isAddressSelected) {
                                Toast.makeText(context, localization["address_select_error"], Toast.LENGTH_LONG).show()
                                return@Button
                            }

                            incidentManager.addNewIncident(
                                authViewModel = authViewModel,
                                context = context,
                                photoUris = incidentPhotos,
                                type = selectedIncidentType!!,
                                description = incidentDescription,
                                region = selectedRegion,
                                address = selectedAddress,
                                latitude = latitude,
                                longitude = longitude,
                                localization = localization
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme["primary"]!!,
                            disabledContainerColor = theme["drawer_background"]!!
                        ),
                        enabled = incidentCreationState.value != IncidentCreationState.Loading && incidentCreationState.value != IncidentCreationState.UploadingPhotos && incidentCreationState.value != IncidentCreationState.CreatingIncident
                    ) {
                        when (incidentCreationState.value) {
                            is IncidentCreationState.Loading -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = localization["loading"]!!,
                                        fontSize = 24.sp,
                                        fontFamily = RubikFont,
                                        fontWeight = FontWeight.Medium,
                                        color = theme["text"]!!
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    CircularProgressIndicator(
                                        color = theme["icon"]!!
                                    )
                                }
                            }
                            is IncidentCreationState.UploadingPhotos -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = localization["uploading_photos"]!!,
                                        fontSize = 24.sp,
                                        fontFamily = RubikFont,
                                        fontWeight = FontWeight.Medium,
                                        color = theme["text"]!!
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    CircularProgressIndicator(
                                        color = theme["icon"]!!
                                    )
                                }
                            }
                            is IncidentCreationState.CreatingIncident -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = localization["creating_incident"]!!,
                                        fontSize = 24.sp,
                                        fontFamily = RubikFont,
                                        fontWeight = FontWeight.Medium,
                                        color = theme["text"]!!
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    CircularProgressIndicator(
                                        color = theme["icon"]!!
                                    )
                                }
                            }
                            else -> {
                                Text(
                                    text = localization["publish_incident_button"]!!,
                                    fontSize = 24.sp,
                                    fontFamily = RubikFont,
                                    color = theme["text"]!!
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getFileNameFromUri(
    uri: Uri,
    context: Context,
    localization: Map<String, String>
): String {
    var result = localization["unknown_file_name"]!!

    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor.use { cur ->
            if (cur != null && cur.moveToFirst()) {
                val index = cur.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    result = cur.getString(index)
                }
            }
        }
    }

    return result
}