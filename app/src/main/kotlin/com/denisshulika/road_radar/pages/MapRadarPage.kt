package com.denisshulika.road_radar.pages

import android.Manifest
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.VectorDrawable
import android.icu.text.SimpleDateFormat
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.denisshulika.road_radar.AuthState
import com.denisshulika.road_radar.AuthViewModel
import com.denisshulika.road_radar.Incident
import com.denisshulika.road_radar.IncidentsManager
import com.denisshulika.road_radar.LoadingDocumentsState
import com.denisshulika.road_radar.LocationHandler
import com.denisshulika.road_radar.LocationRequestState
import com.denisshulika.road_radar.R
import com.denisshulika.road_radar.Routes
import com.denisshulika.road_radar.SettingsViewModel
import com.denisshulika.road_radar.model.CustomDrawerState
import com.denisshulika.road_radar.model.NavigationItem
import com.denisshulika.road_radar.model.ThemeState
import com.denisshulika.road_radar.model.isOpened
import com.denisshulika.road_radar.model.opposite
import com.denisshulika.road_radar.ui.components.CustomDrawer
import com.denisshulika.road_radar.util.coloredShadow
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapRadarPage(
    navController: NavController,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    incidentsManager: IncidentsManager,
    locationHandler: LocationHandler
) {
    val context = LocalContext.current

    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        authViewModel.checkAuthStatus()
        when (authState.value) {
            is AuthState.Unauthenticated -> {
                incidentsManager.stopListeningIncidents()
                navController.navigate(Routes.LOGIN)
            }
            else -> Unit
        }
    }

    val localization = settingsViewModel.localization.observeAsState().value!!
    val theme = settingsViewModel.themeColors.observeAsState().value!!

    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    val loadingDocumentsState by incidentsManager.loadingDocumentsState.observeAsState()
    val incidents = incidentsManager.incidents.observeAsState(emptyList())

    val locationRequestState by locationHandler.locationRequestState.observeAsState()

    val radius = settingsViewModel.radius.observeAsState().value!!

    val userLocation = locationHandler.userLocation.observeAsState().value

    val lastUpdateTime = locationHandler.lastUpdateTime.observeAsState().value!!

    var incidentLoadingTrigger by remember { mutableStateOf(false) }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationHandler.handlePermissionResult(
            permissions = permissions,
            onSuccess = { location ->
                latitude = location.latitude
                longitude = location.longitude

                incidentsManager.stopListeningIncidents()
                incidentsManager.startListeningIncidents(
                    location.latitude,
                    location.longitude,
                    radius.toDouble(),
                    localization
                )
            }
        )
    }

    LaunchedEffect(incidentLoadingTrigger) {
        val currTime = System.currentTimeMillis()
        if (currTime - lastUpdateTime > 5 * 60 * 1000 || userLocation == null) {
            locationLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            latitude = userLocation.latitude
            longitude = userLocation.longitude

            incidentsManager.stopListeningIncidents()
            incidentsManager.startListeningIncidents(
                latitude!!,
                longitude!!,
                radius.toDouble(),
                localization
            )
        }
    }

    val dateFormat = SimpleDateFormat(
        "EEEE, dd MMMM".trimIndent(),
        Locale(localization["date_format_language"]!!, localization["date_format_country"]!!)
    )
    val timeFormat = SimpleDateFormat(
        "'at' HH:mm:ss".trimIndent(),
        Locale(localization["date_format_language"]!!, localization["date_format_country"]!!)
    )

    var drawerState by remember { mutableStateOf(CustomDrawerState.Closed) }
    var selectedNavigationItem by remember { mutableStateOf(NavigationItem.MapRadar) }

    val configuration = LocalConfiguration.current
    val destiny = LocalDensity.current.density

    val screenWidth = remember {
        derivedStateOf { (configuration.screenWidthDp * destiny).roundToInt() }
    }
    val offsetValue by remember { derivedStateOf { (screenWidth.value / 4.5).dp } }
    val animatedOffset by animateDpAsState(
        targetValue = if (drawerState.isOpened()) offsetValue else 0.dp,
        label = "Animated Offset"
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (drawerState.isOpened()) 0.9f else 1f,
        label = "Animated Scale"
    )

    BackHandler(enabled = drawerState.isOpened()) {
        drawerState = CustomDrawerState.Closed
    }

    val systemUiController = rememberSystemUiController()

    systemUiController.setStatusBarColor(
        color = if (drawerState == CustomDrawerState.Closed) theme["top_bar_background"]!! else theme["drawer_background"]!!,
        darkIcons = settingsViewModel.getTheme() != ThemeState.DARK || !isSystemInDarkTheme()
    )
    systemUiController.setNavigationBarColor(
        color = if (drawerState == CustomDrawerState.Closed) theme["background"]!! else theme["drawer_background"]!!,
        darkIcons = settingsViewModel.getTheme() != ThemeState.DARK || !isSystemInDarkTheme()
    )

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 11f)
    }

    LaunchedEffect(userLocation) {
        if (userLocation != null) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(latitude!!, longitude!!),
                11f
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(theme["drawer_background"]!!)
            .then(
                if (drawerState.isOpened()) {
                    Modifier.pointerInput(Unit) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            if (drawerState == CustomDrawerState.Opened && dragAmount < 0) {
                                drawerState = CustomDrawerState.Closed
                            }
                        }
                    }
                } else {
                    Modifier
                }
            )
    ) {
        CustomDrawer(
            selectedNavigationItem = selectedNavigationItem,
            onNavigationItemClick = {
                if (it != NavigationItem.Incidents) {
                    incidentsManager.stopListeningIncidents()
                }
                selectedNavigationItem = it
            },
            onCloseClick = { drawerState = CustomDrawerState.Closed },
            authViewModel = authViewModel,
            settingsViewModel = settingsViewModel,
            navController = navController,
            incidentsManager = incidentsManager
        )

        Scaffold(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToPx(), 0) }
                .scale(scale = animatedScale)
                .coloredShadow(
                    color = theme["shadow"]!!,
                    alpha = 0.1f,
                    shadowRadius = 30.dp
                ),
            topBar = {
                if (locationRequestState != LocationRequestState.Success && loadingDocumentsState != LoadingDocumentsState.Success) {
                    CenterAlignedTopAppBar(
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = theme["top_bar_background"]!!,
                            titleContentColor = theme["text"]!!,
                            navigationIconContentColor = theme["icon"]!!,
                            actionIconContentColor = theme["icon"]!!
                        ),
                        title = {
                            Text(
                                text = selectedNavigationItem.getTitle(localization),
                                textAlign = TextAlign.Center,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    drawerState = drawerState.opposite()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = ""
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    locationHandler.setLastUpdateTime(0L)
                                    incidentLoadingTrigger = !incidentLoadingTrigger
                                },
                                enabled = loadingDocumentsState != LoadingDocumentsState.Loading && locationRequestState != LocationRequestState.Loading
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.refresh),
                                    contentDescription = ""
                                )
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(theme["background"]!!)
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                when (locationRequestState) {
                    LocationRequestState.Success -> {
                        when (loadingDocumentsState) {
                            LoadingDocumentsState.Success -> {
                                GoogleMap(
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    cameraPositionState = cameraPositionState,
                                    mapColorScheme = if (settingsViewModel.getTheme() == ThemeState.DARK) ComposeMapColorScheme.DARK else ComposeMapColorScheme.LIGHT
                                ) {
                                    incidents.value.forEach { incident ->
                                        val markerLatLng = LatLng(
                                            incident.latitude.toDouble(),
                                            incident.longitude.toDouble()
                                        )
                                        val markerState =
                                            rememberMarkerState(position = markerLatLng)

                                        val incidentType = incident.type
                                        val type = when (incidentType) {
                                            "CAR_ACCIDENT" -> localization["incident_type_car_accident"]!!
                                            "ROADBLOCK" -> localization["incident_type_roadblock"]!!
                                            "WEATHER_CONDITIONS" -> localization["incident_type_weather_conditions"]!!
                                            "TRAFFIC_JAM" -> localization["incident_type_traffic_jam"]!!
                                            "OTHER" -> localization["incident_type_other"]!!
                                            else -> localization["incident_type_other"]!!
                                        }

                                        val iconRes = when (incidentType) {
                                            "CAR_ACCIDENT" -> R.drawable.car_accident
                                            "ROADBLOCK" -> R.drawable.roadblock
                                            "WEATHER_CONDITIONS" -> R.drawable.weather_warning
                                            "TRAFFIC_JAM" -> R.drawable.traffic_jam
                                            "OTHER" -> R.drawable.warning
                                            else -> R.drawable.warning
                                        }

                                        val vectorDrawable = AppCompatResources.getDrawable(
                                            context,
                                            iconRes
                                        ) as VectorDrawable
                                        val bitmap = Bitmap.createBitmap(
                                            vectorDrawable.intrinsicWidth,
                                            vectorDrawable.intrinsicHeight,
                                            Bitmap.Config.ARGB_8888
                                        )
                                        val canvas = Canvas(bitmap)
                                        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
                                        vectorDrawable.draw(canvas)

                                        val description = incident.description
                                        val address = incident.address
                                        val creationTime =
                                            incident.creationDate.seconds
                                        val creationDate =
                                            incident.creationDate.toDate()
                                        val lifetimeTimestamp =
                                            incident.lifetime.seconds
                                        val formatedDate = dateFormat.format(creationTime)
                                        val formatedTime = timeFormat.format(creationTime)
                                        val id = incident.id
                                        Marker(
                                            state = markerState,
                                            contentDescription = type,
                                            title = type,
                                            snippet = "$formatedDate $formatedTime",
                                            icon = BitmapDescriptorFactory.fromBitmap(bitmap),
                                            onInfoWindowClick = {
                                                val currentTime = System.currentTimeMillis()
                                                if (lifetimeTimestamp * 1000 < currentTime) {
                                                    Toast.makeText(
                                                        context,
                                                        localization["incident_has_been_ended"]!!,
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    locationHandler.setLastUpdateTime(0L)
                                                    incidentLoadingTrigger = !incidentLoadingTrigger
                                                    navController.navigate(Routes.MAP_RADAR)
                                                }

                                                val createdBy = incident.createdBy
                                                val photos = incident.photos

                                                val incidentInfo = Incident(
                                                    id = id,
                                                    type = type,
                                                    address = address,
                                                    description = description,
                                                    createdBy = createdBy,
                                                    photos = photos,
                                                    creationDate = Timestamp(creationDate)
                                                )
                                                incidentsManager.setSelectedDocumentInfo(
                                                    incidentInfo
                                                )
                                                incidentsManager.stopListeningIncidents()
                                                navController.navigate(Routes.INCIDENT)
                                            },

                                            )
                                    }
                                }
                                FloatingActionButton(
                                    onClick = {
                                        drawerState = drawerState.opposite()
                                    },
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(16.dp),
                                    containerColor = theme["primary"]!!,
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Icon(
                                        modifier = Modifier.size(32.dp),
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "",
                                        tint = theme["icon"]!!
                                    )
                                }
                                FloatingActionButton(
                                    onClick = {
                                        if (loadingDocumentsState != LoadingDocumentsState.Loading && locationRequestState != LocationRequestState.Loading) {
                                            locationHandler.setLastUpdateTime(0L)
                                            incidentLoadingTrigger = !incidentLoadingTrigger
                                        }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(16.dp),
                                    containerColor = theme["primary"]!!,
                                    shape = RoundedCornerShape(50),
                                ) {
                                    Icon(
                                        modifier = Modifier.size(32.dp),
                                        imageVector = ImageVector.vectorResource(R.drawable.refresh),
                                        contentDescription = "",
                                        tint = theme["icon"]!!
                                    )
                                }
                            }

                            LoadingDocumentsState.Loading -> {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ){
                                    Text(
                                        text = localization["loading_incidents"]!!,
                                        fontSize = 20.sp,
                                        fontFamily = RubikFont,
                                        color = theme["text"]!!,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.size(12.dp))
                                    LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth(0.5f),
                                        color = theme["primary"]!!,
                                        trackColor = theme["placeholder"]!!
                                    )
                                }
                            }

                            is LoadingDocumentsState.Error -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp)
                                ) {
                                    Text(
                                        text = localization["loading_documents_state_error"]!!,
                                        fontSize = 20.sp,
                                        fontFamily = RubikFont,
                                        color = theme["error"]!!
                                    )
                                    Text(
                                        text = (loadingDocumentsState as LoadingDocumentsState.Error).message,
                                        fontSize = 20.sp,
                                        fontFamily = RubikFont,
                                        color = theme["error"]!!
                                    )
                                }
                            }

                            LoadingDocumentsState.Idle -> Unit
                            null -> Unit
                        }
                    }

                    LocationRequestState.Loading -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = localization["location_request_state_loading"]!!,
                                fontSize = 20.sp,
                                fontFamily = RubikFont,
                                color = theme["text"]!!,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.size(12.dp))
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(0.5f),
                                color = theme["primary"]!!,
                                trackColor = theme["placeholder"]!!
                            )
                        }
                    }

                    LocationRequestState.NoLocation -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = localization["location_request_state_no_location"]!!,
                                fontSize = 20.sp,
                                fontFamily = RubikFont,
                                color = theme["error"]!!,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    LocationRequestState.NoPermission -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = localization["location_permission_needed"]!!,
                                fontSize = 18.sp,
                                fontFamily = RubikFont,
                                color = theme["text"]!!
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            TextButton(
                                onClick = {
                                    locationHandler.showLocationPermissionDialog(
                                        context,
                                        localization
                                    )
                                }
                            ) {
                                Text(
                                    text = localization["grant_permission"]!!,
                                    fontSize = 20.sp,
                                    color = theme["primary"]!!,
                                    fontFamily = RubikFont
                                )
                            }
                        }
                    }

                    is LocationRequestState.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                        ) {
                            Text(
                                text = localization["location_request_state_error"]!!,
                                fontSize = 20.sp,
                                fontFamily = RubikFont,
                                color = theme["error"]!!
                            )
                            Text(
                                text = (locationRequestState as LocationRequestState.Error).message,
                                fontSize = 20.sp,
                                fontFamily = RubikFont,
                                color = theme["error"]!!
                            )
                        }
                    }

                    LocationRequestState.Idle -> Unit
                    null -> Unit
                }
                if (drawerState.isOpened()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(enabled = drawerState == CustomDrawerState.Opened) {
                                drawerState = CustomDrawerState.Closed
                            }
                    )
                }
            }
        }
    }
}