package com.denisshulika.road_radar.pages

import android.Manifest
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.denisshulika.road_radar.AuthViewModel
import com.denisshulika.road_radar.CommentManager
import com.denisshulika.road_radar.IncidentCreationState
import com.denisshulika.road_radar.IncidentsManager
import com.denisshulika.road_radar.LocationHandler
import com.denisshulika.road_radar.LocationRequestState
import com.denisshulika.road_radar.R
import com.denisshulika.road_radar.Routes
import com.denisshulika.road_radar.SettingsViewModel
import com.denisshulika.road_radar.model.IncidentType
import com.denisshulika.road_radar.model.ThemeState
import com.denisshulika.road_radar.ui.components.PhotoPickerDialog
import com.denisshulika.road_radar.ui.components.StyledBasicTextField
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.libraries.places.api.model.AddressComponent
import com.squareup.moshi.Json
import java.io.File
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewIncidentPage(
    navController: NavController,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    incidentsManager: IncidentsManager,
    locationHandler: LocationHandler,
    commentManager: CommentManager
) {
    val context = LocalContext.current

    val localization = settingsViewModel.localization.observeAsState().value!!
    val theme = settingsViewModel.themeColors.observeAsState().value!!

    val incidentCreationState = incidentsManager.incidentCreationState.observeAsState()

    LaunchedEffect(incidentCreationState.value) {
        when(incidentCreationState.value) {
            is IncidentCreationState.Success -> {
                navController.navigate(Routes.INCIDENTS)
                incidentsManager.setIncidentCreationState(IncidentCreationState.Idle)
            }
            is IncidentCreationState.Error -> {
                Toast.makeText(context, (incidentCreationState.value as IncidentCreationState.Error).message, Toast.LENGTH_LONG).show()
                incidentsManager.setIncidentCreationState(IncidentCreationState.Idle)
            }
            else -> Unit
        }
    }

    val incidentTypes = listOf(
        localization["incident_type_car_accident"]!!,
        localization["incident_type_roadblock"]!!,
        localization["incident_type_weather_conditions"]!!,
        localization["incident_type_traffic_jam"]!!,
        localization["incident_type_road_works"]!!,
        localization["incident_type_police_activity"]!!,
        localization["incident_type_broken_down_vehicle"]!!,
        localization["incident_type_flooding"]!!,
        localization["incident_type_fire_near_road"]!!,
        localization["incident_type_obstacle_on_road"]!!,
        localization["incident_type_other"]!!
    )

    var selectedIncidentType by remember { mutableStateOf<IncidentType?>(null) }
    var isIncidentTypeDropdownExpanded by remember { mutableStateOf(false) }

    var incidentDescription by remember { mutableStateOf("") }

    val incidentPhotos = remember { mutableStateListOf<Uri>() }

    var selectedAddress by remember { mutableStateOf("") }

    val locationRequestState by locationHandler.locationRequestState.observeAsState()

    val lastUpdateTime = locationHandler.lastUpdateTime.observeAsState().value!!

    val userLocation = locationHandler.userLocation.observeAsState().value

    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    var isUserLocationTaken by remember { mutableStateOf<Boolean?>(null) }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationHandler.handlePermissionResult(
            permissions = permissions,
            onSuccess = { location ->
                latitude = location.latitude
                longitude = location.longitude

                locationHandler.getAddressFromCoordinates(
                    context,
                    localization,
                    latitude!!,
                    longitude!!
                ) { address ->
                    if (address != null) {
                        isUserLocationTaken = true

                        val parts = address.split(",")
                            .map { it.trim() }

                        val street = parts.getOrNull(0) ?: localization["unknown_street"]!!

                        val buildingNumber = parts.getOrNull(1) ?: localization["unknown_number"]!!

                        selectedAddress = "$street, $buildingNumber"

                        locationHandler.setLocationRequestState(LocationRequestState.Success)
                    } else {
                        Toast.makeText(context, localization["get_address_fail"]!!, Toast.LENGTH_LONG).show()
                        locationHandler.setLocationRequestState(LocationRequestState.NoLocation)
                    }
                }
            }
        )
    }

    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uriList ->
        val remaining = 3 - incidentPhotos.size
        incidentPhotos.addAll(uriList.take(remaining))
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && incidentPhotos.size < 3) {
            incidentPhotos.add(cameraImageUri.value!!)
        }
    }

    var showDialog by remember { mutableStateOf(false) }

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
                                            IncidentType.ROAD_WORKS -> localization["incident_type_road_works"]!!
                                            IncidentType.POLICE_ACTIVITY -> localization["incident_type_police_activity"]!!
                                            IncidentType.BROKEN_DOWN_VEHICLE -> localization["incident_type_broken_down_vehicle"]!!
                                            IncidentType.FLOODING -> localization["incident_type_flooding"]!!
                                            IncidentType.FIRE_NEAR_ROAD -> localization["incident_type_fire_near_road"]!!
                                            IncidentType.OBSTACLE_ON_ROAD -> localization["incident_type_obstacle_on_road"]!!
                                            IncidentType.SOS -> localization["incident_type_sos"]!!
                                        },
                                        color = if (selectedIncidentType != null) theme["text"]!! else theme["placeholder"]!!,
                                        fontSize = 22.sp,
                                        fontFamily = RubikFont
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "",
                                        tint = theme["icon"]!!
                                    )
                                }
                                DropdownMenu(
                                    modifier = Modifier.background(theme["primary"]!!),
                                    expanded = isIncidentTypeDropdownExpanded,
                                    onDismissRequest = { isIncidentTypeDropdownExpanded = false }
                                ) {
                                    incidentTypes.forEach { type ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val iconRes = when (type) {
                                                        localization["incident_type_car_accident"]!! -> R.drawable.car_accident
                                                        localization["incident_type_roadblock"]!! -> R.drawable.roadblock
                                                        localization["incident_type_weather_conditions"]!! -> R.drawable.weather_warning
                                                        localization["incident_type_traffic_jam"]!! -> R.drawable.traffic_jam
                                                        localization["incident_type_other"]!! -> R.drawable.warning
                                                        localization["incident_type_road_works"]!! -> R.drawable.road_works
                                                        localization["incident_type_police_activity"]!! -> R.drawable.police_activity
                                                        localization["incident_type_broken_down_vehicle"]!! -> R.drawable.broken_down_vehicle
                                                        localization["incident_type_flooding"]!! -> R.drawable.flooding
                                                        localization["incident_type_fire_near_road"]!! -> R.drawable.fire_near_road
                                                        localization["incident_type_obstacle_on_road"]!! -> R.drawable.obstacle_on_road
                                                        else -> R.drawable.warning
                                                    }

                                                    Icon(
                                                        painter = painterResource(id = iconRes),
                                                        contentDescription = null,
                                                        tint = Color.Unspecified,
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .padding(end = 8.dp)
                                                    )

                                                    Text(
                                                        text = type,
                                                        color = Color.White,
                                                        fontSize = 20.sp,
                                                        fontFamily = RubikFont
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedIncidentType = when(type) {
                                                    localization["incident_type_car_accident"]!! -> IncidentType.CAR_ACCIDENT
                                                    localization["incident_type_roadblock"]!! -> IncidentType.ROADBLOCK
                                                    localization["incident_type_weather_conditions"]!! -> IncidentType.WEATHER_CONDITIONS
                                                    localization["incident_type_traffic_jam"]!! -> IncidentType.TRAFFIC_JAM
                                                    localization["incident_type_other"]!! -> IncidentType.OTHER
                                                    localization["incident_type_road_works"]!! -> IncidentType.ROAD_WORKS
                                                    localization["incident_type_police_activity"]!! -> IncidentType.POLICE_ACTIVITY
                                                    localization["incident_type_broken_down_vehicle"]!! -> IncidentType.BROKEN_DOWN_VEHICLE
                                                    localization["incident_type_flooding"]!! -> IncidentType.FLOODING
                                                    localization["incident_type_fire_near_road"]!! -> IncidentType.FIRE_NEAR_ROAD
                                                    localization["incident_type_obstacle_on_road"]!! -> IncidentType.OBSTACLE_ON_ROAD
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
                            },
                            placeholder = localization["description_placeholder"]!!,
                            singleLine = false,
                            theme = theme
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = localization["location"]!!,
                            fontSize = 24.sp,
                            fontFamily = RubikFont,
                            color = theme["text"]!!
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (locationRequestState == LocationRequestState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(40.dp),
                                    color = theme["primary"]!!
                                )
                            } else if (isUserLocationTaken != true) {
                                TextButton(
                                    onClick = {
                                        val currTime = System.currentTimeMillis()
                                        if (currTime - lastUpdateTime > 5 * 60 * 1000 || userLocation == null) {
                                            locationLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        } else {
                                            locationHandler.setLastUpdateTime(System.currentTimeMillis())
                                            latitude = userLocation.latitude
                                            longitude = userLocation.longitude

                                            locationHandler.getAddressFromCoordinates(
                                                context, localization,
                                                latitude!!, longitude!!
                                            ) { address ->
                                                if (address != null) {
                                                    val parts = address.split(",").map { it.trim() }
                                                    val street = parts.getOrNull(0) ?: localization["unknown_street"]!!
                                                    val buildingNumber = parts.getOrNull(1) ?: localization["unknown_number"]!!
                                                    selectedAddress = "$street, $buildingNumber"
                                                    isUserLocationTaken = true
                                                } else {
                                                    Toast.makeText(context, localization["get_address_fail"]!!, Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                    },
                                    enabled = true
                                ) {
                                    Text(
                                        text = localization["location_button"]!!,
                                        fontSize = 20.sp,
                                        color = theme["primary"]!!,
                                        fontFamily = RubikFont
                                    )
                                }
                            }
                        }
                    }
                    if (isUserLocationTaken == false) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = localization["location_empty"]!!,
                            color = theme["error"]!!,
                            fontSize = 12.sp,
                            fontFamily = RubikFont,
                            fontWeight = FontWeight.Normal
                        )
                    }
                    if (locationRequestState == LocationRequestState.NoLocation) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = localization["location_not_available"]!!,
                            color = theme["error"]!!,
                            fontSize = 12.sp,
                            fontFamily = RubikFont,
                            fontWeight = FontWeight.Normal
                        )
                    }
                    if (locationRequestState == LocationRequestState.NoPermission) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = localization["location_permission_denied"]!!,
                            color = theme["error"]!!,
                            fontSize = 12.sp,
                            fontFamily = RubikFont,
                            fontWeight = FontWeight.Normal
                        )
                    }
                    if (isUserLocationTaken == true) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = selectedAddress,
                            fontSize = 22.sp,
                            fontFamily = RubikFont,
                            color = theme["text"]!!
                        )
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
                                    uri = uri,
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
                                                incidentPhotos.removeAt(index)
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
                    PhotoPickerDialog(
                        showDialog = showDialog,
                        onDismiss = { showDialog = false },
                        onPickFromGallery = {
                            galleryLauncher.launch("image/*")
                        },
                        onTakePhoto = {
                            val photoFile = File(context.cacheDir, "${UUID.randomUUID()}.jpg")
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                photoFile
                            )
                            cameraImageUri.value = uri
                            cameraLauncher.launch(uri)
                        },
                        localization = localization,
                        theme = theme
                    )
                    if (incidentPhotos.size < 3) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            TextButton(
                                enabled = incidentCreationState.value != IncidentCreationState.Loading,
                                onClick = {
                                    showDialog = true
                                }
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
                            isUserLocationTaken = userLocation != null
                            if (!isUserLocationTaken!! || selectedAddress.isEmpty()) {
                                Toast.makeText(context, localization["location_error"]!!, Toast.LENGTH_LONG).show()
                                isUserLocationTaken = false
                                return@Button
                            }
                            incidentsManager.addNewIncident(
                                authViewModel = authViewModel,
                                commentManager = commentManager,
                                context = context,
                                photoUris = incidentPhotos,
                                type = selectedIncidentType!!,
                                description = incidentDescription,
                                address = selectedAddress,
                                latitude = userLocation!!.latitude.toString(),
                                longitude = userLocation.longitude.toString(),
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

data class GeocodingResponse(
    @Json(name = "results") val results: List<Result>
)

data class Result(
    @Json(name = "formatted_address") val formattedAddress: String,
    @Json(name = "address_components") val addressComponents: List<AddressComponent>
)