package com.denisshulika.road_radar.pages

import RegionLoader
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.VectorDrawable
import android.icu.text.SimpleDateFormat
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.denisshulika.road_radar.AuthState
import com.denisshulika.road_radar.AuthViewModel
import com.denisshulika.road_radar.IncidentManager
import com.denisshulika.road_radar.LoadingDocumentsState
import com.denisshulika.road_radar.R
import com.denisshulika.road_radar.Routes
import com.denisshulika.road_radar.SettingsViewModel
import com.denisshulika.road_radar.model.CustomDrawerState
import com.denisshulika.road_radar.model.IncidentInfo
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
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun MapRadarPage(
    @Suppress("UNUSED_PARAMETER") modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    incidentManager: IncidentManager
) {
    val context = LocalContext.current

    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        authViewModel.checkAuthStatus()
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate(Routes.LOGIN)
            else -> Unit
        }
    }

    val localization = settingsViewModel.localization.observeAsState().value!!
    val theme = settingsViewModel.themeColors.observeAsState().value!!

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

    val loadingState by incidentManager.loadingDocumentsState.observeAsState()
    val incidents = incidentManager.documentsList.observeAsState(emptyList())

    var incidentLoadingTrigger by remember { mutableStateOf(false) }
    val region by incidentManager.userRegion.observeAsState()

    LaunchedEffect(incidentLoadingTrigger) {
        incidentManager.setLoadingDocumentsState(LoadingDocumentsState.Loading)

        if(incidents.value.isNotEmpty()) {
            val latestDoc1 = incidents.value.first()
            val oldestDoc1 = incidents.value.last()
            val latestDoc2 = incidentManager.getLatestIncident(region!!, localization)
            val oldestDoc2 = incidentManager.getOldestIncident(region!!, localization)

            val needToUpdate = latestDoc1.id != latestDoc2!!.id || oldestDoc1.id != oldestDoc2!!.id || oldestDoc2.getTimestamp("creationDate")!!.toDate().time > 3 * 60 * 60 * 1000
            if (needToUpdate) {
                incidentManager.loadIncidentsByRegion(region!!, localization)
            } else {
                incidentManager.setLoadingDocumentsState(LoadingDocumentsState.Success)
            }
        } else {
            incidentManager.loadIncidentsByRegion(region!!, localization)
        }
    }

    val latLngByRegion = RegionLoader.loadRegionsFromJson(context)

    val regionLatLng = latLngByRegion[region ?: ""]!!
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(regionLatLng, if (region!!.contains("місто")) 9.5f else 7f)
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
                selectedNavigationItem = it
            },
            onCloseClick = { drawerState = CustomDrawerState.Closed },
            authViewModel = authViewModel,
            settingsViewModel = settingsViewModel,
            navController = navController,
            incidentManager = incidentManager
        )

        Scaffold(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToPx(), 0) }
                .scale(scale = animatedScale)
                .coloredShadow(
                    color = theme["shadow"]!!,
                    alpha = 0.1f,
                    shadowRadius = 30.dp
                )
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(theme["background"]!!)
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                when (loadingState) {
                    LoadingDocumentsState.Loading -> {
                        Row(
                            modifier = Modifier
                                .fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = localization["loading_map"]!!,
                                fontSize = 20.sp,
                                fontFamily = RubikFont,
                                color = theme["text"]!!
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            CircularProgressIndicator(
                                color = theme["primary"]!!
                            )
                        }
                    }
                    is LoadingDocumentsState.Error -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = (loadingState as LoadingDocumentsState.Error).message,
                                fontSize = 16.sp,
                                fontFamily = RubikFont,
                                color = theme["error"]!!
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = localization["loading_map_error_text"]!!,
                                fontSize = 16.sp,
                                fontFamily = RubikFont,
                                color = theme["error"]!!
                            )
                        }
                        FloatingActionButton(
                            onClick = {
                                drawerState = drawerState.opposite()
                            },
                            modifier = Modifier
                                .align(Alignment.TopStart)
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
                    }
                    LoadingDocumentsState.Success -> {
                        GoogleMap(
                            modifier = Modifier
                                .fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            mapColorScheme = if (settingsViewModel.getTheme() == ThemeState.DARK) ComposeMapColorScheme.DARK else ComposeMapColorScheme.LIGHT
                        ) {
                            incidents.value.forEach { incident ->
                                val markerLatLng = LatLng(incident.getString("latitude")!!.toDouble(), incident.getString("longitude")!!.toDouble())
                                val markerState = rememberMarkerState(position = markerLatLng)

                                val incidentType = incident.getString("type")
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

                                val vectorDrawable = AppCompatResources.getDrawable(context, iconRes) as VectorDrawable
                                val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
                                val canvas = Canvas(bitmap)
                                vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
                                vectorDrawable.draw(canvas)

                                val description = incident.getString("description")!!
                                val address = incident.getString("address")!!
                                val creationDate = incident.getTimestamp("creationDate")!!.toDate()
                                val formatedDate = dateFormat.format(creationDate)
                                val formatedTime = timeFormat.format(creationDate)
                                Marker(
                                    state = markerState,
                                    contentDescription = type,
                                    title = type,
                                    snippet = "$formatedDate $formatedTime",
                                    icon = BitmapDescriptorFactory.fromBitmap(bitmap),
                                    onInfoWindowClick = {
                                        val currentTime = System.currentTimeMillis()
                                        val creationTime = creationDate.time
                                        val timeDifference = currentTime - creationTime
                                        if (timeDifference > 3 * 60 * 60 * 1000) {
                                            Toast.makeText(context, localization["incident_has_been_ended"]!!, Toast.LENGTH_LONG).show()
                                            incidentLoadingTrigger = !incidentLoadingTrigger
                                            navController.navigate(Routes.MAP_RADAR)
                                        }

                                        val createdBy = incident.getString("createdBy")!!
                                        val photosRaw = incident.get("photos") as? List<*>
                                        val photos = photosRaw?.mapNotNull { it as? String } ?: emptyList()

                                        val incidentInfo = IncidentInfo(
                                            type = type,
                                            date = "$formatedDate $formatedTime",
                                            address = address,
                                            description = description,
                                            createdBy = createdBy,
                                            photos = photos
                                        )
                                        incidentManager.setSelectedDocumentInfo(incidentInfo)
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
                                incidentLoadingTrigger = !incidentLoadingTrigger
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp),
                            containerColor = theme["primary"]!!,
                            shape = RoundedCornerShape(50)
                        ) {
                            Icon(
                                modifier = Modifier.size(32.dp),
                                imageVector = ImageVector.vectorResource(R.drawable.refresh),
                                contentDescription = "",
                                tint = theme["icon"]!!
                            )
                        }
                    }
                    LoadingDocumentsState.Null -> Unit
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