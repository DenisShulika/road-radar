package com.denisshulika.road_radar.pages

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.provider.OpenableColumns
import android.provider.Settings
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.denisshulika.road_radar.AuthViewModel
import com.denisshulika.road_radar.BuildConfig
import com.denisshulika.road_radar.IncidentCreationState
import com.denisshulika.road_radar.IncidentManager
import com.denisshulika.road_radar.Routes
import com.denisshulika.road_radar.SettingsViewModel
import com.denisshulika.road_radar.model.IncidentType
import com.denisshulika.road_radar.model.ThemeState
import com.denisshulika.road_radar.ui.components.StyledBasicTextField
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.model.AddressComponent
import com.google.android.libraries.places.api.net.PlacesClient
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Locale

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

    var incidentPhotos by remember { mutableStateOf<List<Uri?>>(emptyList()) }

    var selectedRegion by remember { mutableStateOf("") }

    var selectedAddress by remember { mutableStateOf("") }

    var userGPS by remember { mutableStateOf<GPS?>(null) }
    var isUserLocationTaken by remember { mutableStateOf<Boolean?>(null) }

    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

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

    val locationPermissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationPermissionGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)
        val coarseLocationPermissionGranted = permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)

        if (fineLocationPermissionGranted || coarseLocationPermissionGranted) {
            getCurrentLocation(context, localization) { gps ->
                userGPS = gps
                latitude = gps?.latitude
                longitude = gps?.longitude
            }
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(context, localization["permission_needed"]!!, Toast.LENGTH_LONG).show()
            } else {
                showLocationPermissionDialog(context, localization)
            }
        }
    }

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
                            },
                            placeholder = localization["description_placeholder"]!!,
                            singleLine = false,
                            theme = theme
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = localization["location"]!!,
                            fontSize = 24.sp,
                            fontFamily = RubikFont,
                            color = theme["text"]!!
                        )
                        TextButton(
                            onClick = {
                                when {
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED -> {
                                        getCurrentLocation(context, localization) { gps ->
                                            userGPS = gps
                                            latitude = gps?.latitude
                                            longitude = gps?.longitude
                                            if (userGPS != null) {
                                                isUserLocationTaken = true
                                                getAddressFromCoordinates(context, localization, userGPS!!.latitude, userGPS!!.longitude) { address ->
                                                    if (address != null) {
                                                        val parts = address.split(",")
                                                            .map { it.trim() }

                                                        val street = parts.getOrNull(0) ?: localization["unknown_street"]!!
                                                        val region = parts.find { it.contains("область") || it.contains("місто") }
                                                            ?: localization["unknown_region"]!!

                                                        val buildingNumber = parts.getOrNull(1) ?: localization["unknown_number"]!!

                                                        selectedAddress = "$street, $buildingNumber"
                                                        selectedRegion = region
                                                    } else {
                                                        Toast.makeText(context, localization["get_address_fail"]!!, Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    else -> {
                                        locationPermissionRequest.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                }
                            }
                        ) {
                            Text(
                                text = localization["location_button"]!!,
                                fontSize = 20.sp,
                                color = theme["primary"]!!,
                                fontFamily = RubikFont
                            )
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
                    Spacer(modifier = Modifier.height(32.dp))
                    if(selectedRegion.isNotEmpty()){
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = localization["region_title"]!!,
                                fontSize = 24.sp,
                                fontFamily = RubikFont,
                                color = theme["text"]!!
                            )
                            Text(
                                text = selectedRegion,
                                fontSize = 22.sp,
                                fontFamily = RubikFont,
                                color = theme["text"]!!
                            )
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                    if(selectedAddress.isNotEmpty()) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = localization["address_title"]!!,
                                fontSize = 24.sp,
                                fontFamily = RubikFont,
                                color = theme["text"]!!
                            )
                            Text(
                                text = selectedAddress,
                                fontSize = 22.sp,
                                fontFamily = RubikFont,
                                color = theme["text"]!!
                            )
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
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
                            isUserLocationTaken = userGPS != null
                            if (isUserLocationTaken == false || selectedAddress.isEmpty()) {
                                Toast.makeText(context, localization["location_error"]!!, Toast.LENGTH_LONG).show() //TODO()
                                isUserLocationTaken = false
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
                                latitude = userGPS!!.latitude.toString(),
                                longitude = userGPS!!.longitude.toString(),
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

fun getCurrentLocation(context: Context, localization: Map<String, String>, onLocationReceived: (GPS?) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        onLocationReceived(null)
        return
    }

    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
        location?.let {
            onLocationReceived(GPS(it.longitude, it.latitude))
        } ?: run {
            Toast.makeText(context, localization["get_location_fail"]!!, Toast.LENGTH_LONG).show()
            onLocationReceived(null)
        }
    }
}

fun showLocationPermissionDialog(
    context: Context,
    localization: Map<String, String>
) {
    val dialog = AlertDialog.Builder(context)
        .setTitle(localization["permission_title"]!!)
        .setMessage(localization["permission_message"]!!)
        .setPositiveButton(localization["permission_positive_button"]!!) { _, _ ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            context.startActivity(intent)
        }
        .setNegativeButton(localization["permission_negative_button"]!!) { dialog, _ -> dialog.dismiss() }
        .create()

    dialog.show()
}

data class GPS(
    val longitude: Double,
    val latitude: Double
)

@Suppress("DEPRECATION")
fun getAddressFromCoordinates(
    context: Context,
    localization: Map<String, String>,
    latitude: Double,
    longitude: Double,
    callback: (String?) -> Unit
) {
    val geocoder = Geocoder(context, Locale.getDefault())

    try {
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (addresses?.isNotEmpty() == true) {
            val address = addresses[0].getAddressLine(0) ?: localization["unknown_location"]!!
            callback(address)
            return
        }
    } catch (e: Exception) {
        Toast.makeText(context, "${e.message}", Toast.LENGTH_LONG).show()
    }

    val apiKey = BuildConfig.MAPS_API_KEY
    val url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=$latitude,$longitude&key=$apiKey"

    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()

    Thread {
        try {
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (body != null) {
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                val jsonAdapter = moshi.adapter(GeocodingResponse::class.java)
                val result = jsonAdapter.fromJson(body)

                val fullAddress = result?.results?.firstOrNull()?.formattedAddress
                callback(fullAddress)
            } else {
                callback(null)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "${e.message}", Toast.LENGTH_LONG).show()
            callback(null)
        }
    }.start()
}

data class GeocodingResponse(
    @Json(name = "results") val results: List<Result>
)

data class Result(
    @Json(name = "formatted_address") val formattedAddress: String,
    @Json(name = "address_components") val addressComponents: List<AddressComponent>
)
