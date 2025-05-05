package com.denisshulika.road_radar.pages


import android.Manifest
import android.icu.text.SimpleDateFormat
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.denisshulika.road_radar.AuthState
import com.denisshulika.road_radar.AuthViewModel
import com.denisshulika.road_radar.CommentManager
import com.denisshulika.road_radar.Incident
import com.denisshulika.road_radar.IncidentCreationState
import com.denisshulika.road_radar.IncidentsManager
import com.denisshulika.road_radar.LoadingDocumentsState
import com.denisshulika.road_radar.LocationHandler
import com.denisshulika.road_radar.LocationRequestState
import com.denisshulika.road_radar.R
import com.denisshulika.road_radar.Routes
import com.denisshulika.road_radar.SettingsViewModel
import com.denisshulika.road_radar.SortOrder
import com.denisshulika.road_radar.model.CustomDrawerState
import com.denisshulika.road_radar.model.IncidentType
import com.denisshulika.road_radar.model.NavigationItem
import com.denisshulika.road_radar.model.ThemeState
import com.denisshulika.road_radar.model.isOpened
import com.denisshulika.road_radar.model.opposite
import com.denisshulika.road_radar.ui.components.CustomDrawer
import com.denisshulika.road_radar.util.coloredShadow
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentsPage(
    navController: NavController,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    incidentsManager: IncidentsManager,
    commentManager: CommentManager,
    locationHandler: LocationHandler
) {
    val localization = settingsViewModel.localization.observeAsState().value!!
    val theme = settingsViewModel.themeColors.observeAsState().value!!

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val destiny = LocalDensity.current.density

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

    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    val dateFormat = SimpleDateFormat(
        "EEEE, dd MMMM".trimIndent(),
        Locale(localization["date_format_language"]!!, localization["date_format_country"]!!)
    )
    val timeFormat = SimpleDateFormat(
        "'${localization["at"]!!}' HH:mm:ss".trimIndent(),
        Locale(localization["date_format_language"]!!, localization["date_format_country"]!!)
    )

    val loadingDocumentsState by incidentsManager.loadingDocumentsState.observeAsState()
    val incidents = incidentsManager.incidents.observeAsState(emptyList()).value

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

    DisposableEffect(Unit) {
        onDispose {
            incidentsManager.stopListeningIncidents()
        }
    }

    var drawerState by remember { mutableStateOf(CustomDrawerState.Closed) }
    var selectedNavigationItem by remember { mutableStateOf(NavigationItem.Incidents) }

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


    var showDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val selectedTypes = incidentsManager.incidentTypeFilters.observeAsState().value!!
    val incidentTypes = IncidentType.entries

    val sortOrder = incidentsManager.sortOrder.observeAsState().value!!

    var expandedSortOrder by remember { mutableStateOf(false) }

    var isFiltersChanged by remember { mutableStateOf(false) }

    LaunchedEffect(showDialog) {
        if (!showDialog && isFiltersChanged) {
            incidentsManager.stopListeningIncidents()
            incidentsManager.startListeningIncidents(
                latitude!!,
                longitude!!,
                radius.toDouble(),
                localization
            )
            isFiltersChanged = false
        }
    }

    val incidentCreationState = incidentsManager.incidentCreationState.observeAsState()

    LaunchedEffect(incidentCreationState.value) {
        when(incidentCreationState.value) {
            is IncidentCreationState.Error -> {
                Toast.makeText(context, (incidentCreationState.value as IncidentCreationState.Error).message, Toast.LENGTH_LONG).show()
                incidentsManager.setIncidentCreationState(IncidentCreationState.Idle)
            }
            else -> Unit
        }
    }

    var showSosDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(theme["drawer_background"]!!)
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    drawerState =
                        if (dragAmount > 0) CustomDrawerState.Opened else CustomDrawerState.Closed
                }
            }
    ) {
        CustomDrawer(
            selectedNavigationItem = selectedNavigationItem,
            onNavigationItemClick = {
                if (it != NavigationItem.MapRadar) {
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
                )
                .clickable(enabled = drawerState == CustomDrawerState.Opened) {
                    drawerState = CustomDrawerState.Closed
                },
            topBar = {
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
                        if (loadingDocumentsState == LoadingDocumentsState.Success && locationRequestState == LocationRequestState.Success) {
                            IconButton(
                                onClick = {
                                    showDialog = true
                                }
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.filter),
                                    contentDescription = ""
                                )
                            }
                        } else {
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
                    }
                )
            }
        ) { innerPadding ->
            if (showDialog) {
                ModalBottomSheet(
                    sheetState = sheetState,
                    onDismissRequest = {
                        showDialog = false
                        if (isFiltersChanged) {
                            incidentsManager.stopListeningIncidents()
                            incidentsManager.startListeningIncidents(
                                latitude!!,
                                longitude!!,
                                radius.toDouble(),
                                localization
                            )
                            isFiltersChanged = false
                        }
                    },
                    containerColor = theme["background"]!!,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    contentColor = theme["text"]!!,
                    tonalElevation = 8.dp,
                    scrimColor = Color.Black.copy(alpha = 0.32f),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
                        ) {
                            Text(
                                text = localization["sort_by"]!!,
                                fontSize = 20.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Bold,
                                color = theme["text"]!!
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable (
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        expandedSortOrder = !expandedSortOrder
                                    }
                                    .padding(vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${localization["sort_order"]!!}: ${localization[sortOrder.name]!!}",
                                        fontSize = 18.sp,
                                        fontFamily = RubikFont,
                                        color = theme["text"]!!
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = theme["icon"]!!
                                    )
                                }
                            }

                            Box {
                                DropdownMenu(
                                    modifier = Modifier.background(theme["primary"]!!),
                                    expanded = expandedSortOrder,
                                    onDismissRequest = {
                                        expandedSortOrder = false
                                    }
                                ) {
                                    SortOrder.entries.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(
                                                text = localization[option.name]!!,
                                                color = Color.White,
                                                fontSize = 20.sp,
                                                fontFamily = RubikFont
                                            ) },
                                            onClick = {
                                                isFiltersChanged = true
                                                incidentsManager.setSortOrder(option)
                                                expandedSortOrder = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = localization["incident_filter_select"]!!,
                                fontSize = 20.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Bold,
                                color = theme["text"]!!
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            incidentTypes.forEach { type ->
                                val iconRes = when (type) {
                                    IncidentType.CAR_ACCIDENT -> R.drawable.car_accident
                                    IncidentType.ROADBLOCK -> R.drawable.roadblock
                                    IncidentType.WEATHER_CONDITIONS -> R.drawable.weather_warning
                                    IncidentType.TRAFFIC_JAM -> R.drawable.traffic_jam
                                    IncidentType.ROAD_WORKS -> R.drawable.road_works
                                    IncidentType.POLICE_ACTIVITY -> R.drawable.police_activity
                                    IncidentType.BROKEN_DOWN_VEHICLE -> R.drawable.broken_down_vehicle
                                    IncidentType.FLOODING -> R.drawable.flooding
                                    IncidentType.FIRE_NEAR_ROAD -> R.drawable.fire_near_road
                                    IncidentType.OBSTACLE_ON_ROAD -> R.drawable.obstacle_on_road
                                    IncidentType.OTHER -> R.drawable.warning
                                    IncidentType.SOS -> R.drawable.sos
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) {
                                            isFiltersChanged = true
                                            if (selectedTypes.contains(type)) {
                                                incidentsManager.removeIncidentTypeFilter(type)
                                            } else {
                                                incidentsManager.addIncidentTypeFilter(type)
                                            }
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedTypes.contains(type),
                                        onCheckedChange = {
                                            isFiltersChanged = true
                                            if (it) {
                                                incidentsManager.addIncidentTypeFilter(type)
                                            } else {
                                                incidentsManager.removeIncidentTypeFilter(type)
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = theme["primary"]!!,
                                            uncheckedColor = theme["icon"]!!
                                        )
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Icon(
                                        painter = painterResource(id = iconRes),
                                        contentDescription = null,
                                        tint = Color.Unspecified,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .padding(end = 8.dp)
                                    )

                                    Text(
                                        text = when (type) {
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
                                        fontSize = 18.sp,
                                        fontFamily = RubikFont,
                                        color = theme["text"]!!
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    showDialog = false
                                    if (isFiltersChanged) {
                                        incidentsManager.stopListeningIncidents()
                                        incidentsManager.startListeningIncidents(
                                            latitude!!,
                                            longitude!!,
                                            radius.toDouble(),
                                            localization
                                        )
                                        isFiltersChanged = false
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = theme["primary"]!!,
                                    contentColor = theme["text"]!!
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = localization["apply_filters"]!!,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(theme["background"]!!)
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 20.dp, end = 20.dp)
                ) {
                    when (locationRequestState) {
                        LocationRequestState.Success -> {
                            when (loadingDocumentsState) {
                                LoadingDocumentsState.Success -> {
                                    if (incidents.isEmpty()) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.SpaceBetween,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Column(
                                                    verticalArrangement = Arrangement.Center,
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = localization["no_incidents_in_radius"]!!,
                                                        fontSize = 20.sp,
                                                        fontFamily = RubikFont,
                                                        textAlign = TextAlign.Center,
                                                        color = theme["text"]!!
                                                    )
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                    Text(
                                                        text = localization["smile"]!!,
                                                        fontSize = 24.sp,
                                                        fontFamily = RubikFont,
                                                        color = theme["primary"]!!
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        LazyColumn(
                                            verticalArrangement = Arrangement.spacedBy(20.dp)
                                        ) {
                                            item { Spacer(modifier = Modifier) }
                                            items(incidents) { incident ->
                                                val type = when (incident.type) {
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
                                                }
                                                val description = incident.description
                                                val address = incident.address
                                                val creationDate = incident.creationDate.toDate()
                                                val formatedDate = dateFormat.format(creationDate)
                                                val formatedTime = timeFormat.format(creationDate)
                                                val id = incident.id
                                                val usersLiked = incident.usersLiked.toMutableList()
                                                val lifetime = incident.lifetime

                                                val userID = authViewModel.getCurrentUser()!!.uid

                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 8.dp)
                                                        .shadow(
                                                            elevation = 4.dp,
                                                            shape = RoundedCornerShape(12.dp),
                                                            ambientColor = if (incident.type == IncidentType.SOS) theme["error"]!!.copy(0.7f) else Color.Black,
                                                            spotColor = if (incident.type == IncidentType.SOS) theme["error"]!!.copy(0.7f) else Color.Black
                                                        ),
                                                    colors = CardDefaults.cardColors(containerColor = theme["drawer_background"]!!)
                                                ) {
                                                    Column {
                                                        Box(
                                                            modifier = Modifier
                                                                .clickable {
                                                                    val currentTime = System.currentTimeMillis()
                                                                    if (lifetime.seconds * 1000 < currentTime) {
                                                                        Toast.makeText(context, localization["incident_has_been_ended"]!!, Toast.LENGTH_LONG).show()
                                                                        incidentLoadingTrigger = !incidentLoadingTrigger
                                                                        return@clickable
                                                                    }

                                                                    val createdBy = incident.createdBy
                                                                    val photos = incident.photos

                                                                    val incidentInfo = Incident(
                                                                        id = id,
                                                                        type = incident.type,
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
                                                                }
                                                                .fillMaxWidth()
                                                        ) {
                                                            Column(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(16.dp),
                                                                verticalArrangement = Arrangement.spacedBy(20.dp)
                                                            ) {
                                                                Row(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth(),
                                                                    verticalAlignment = Alignment.Top
                                                                ) {
                                                                    Icon(
                                                                        imageVector = ImageVector.vectorResource(R.drawable.car),
                                                                        contentDescription = "",
                                                                        tint = theme["icon"]!!,
                                                                        modifier = Modifier.size(24.dp)
                                                                    )
                                                                    Spacer(modifier = Modifier.size(24.dp))

                                                                    Text(
                                                                        text = type,
                                                                        fontSize = 20.sp,
                                                                        fontFamily = RubikFont,
                                                                        color = if(incident.type == IncidentType.SOS) theme["error"]!! else theme["text"]!!
                                                                    )

                                                                    val iconRes = when (incident.type) {
                                                                        IncidentType.CAR_ACCIDENT -> R.drawable.car_accident
                                                                        IncidentType.ROADBLOCK -> R.drawable.roadblock
                                                                        IncidentType.WEATHER_CONDITIONS -> R.drawable.weather_warning
                                                                        IncidentType.TRAFFIC_JAM -> R.drawable.traffic_jam
                                                                        IncidentType.ROAD_WORKS -> R.drawable.road_works
                                                                        IncidentType.POLICE_ACTIVITY -> R.drawable.police_activity
                                                                        IncidentType.BROKEN_DOWN_VEHICLE -> R.drawable.broken_down_vehicle
                                                                        IncidentType.FLOODING -> R.drawable.flooding
                                                                        IncidentType.FIRE_NEAR_ROAD -> R.drawable.fire_near_road
                                                                        IncidentType.OBSTACLE_ON_ROAD -> R.drawable.obstacle_on_road
                                                                        IncidentType.OTHER -> R.drawable.warning
                                                                        IncidentType.SOS -> R.drawable.sos
                                                                    }

                                                                    Spacer(modifier = Modifier.size(8.dp))

                                                                    Icon(
                                                                        painter = painterResource(id = iconRes),
                                                                        contentDescription = null,
                                                                        modifier = Modifier.size(24.dp),
                                                                        tint = Color.Unspecified
                                                                    )
                                                                }
                                                                Row(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth(),
                                                                    verticalAlignment = Alignment.Top
                                                                ) {
                                                                    Icon(
                                                                        imageVector = ImageVector.vectorResource(
                                                                            R.drawable.time
                                                                        ),
                                                                        contentDescription = "",
                                                                        tint = theme["icon"]!!
                                                                    )
                                                                    Spacer(modifier = Modifier.size(24.dp))
                                                                    Text(
                                                                        text = "$formatedDate\n$formatedTime",
                                                                        fontSize = 20.sp,
                                                                        fontFamily = RubikFont,
                                                                        color = theme["text"]!!
                                                                    )
                                                                }
                                                                Row(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth(),
                                                                    verticalAlignment = Alignment.Top
                                                                ) {
                                                                    Icon(
                                                                        imageVector = ImageVector.vectorResource(
                                                                            R.drawable.location
                                                                        ),
                                                                        contentDescription = "",
                                                                        tint = theme["icon"]!!
                                                                    )
                                                                    Spacer(modifier = Modifier.size(24.dp))
                                                                    Text(
                                                                        text = address,
                                                                        fontSize = 20.sp,
                                                                        fontFamily = RubikFont,
                                                                        color = theme["text"]!!
                                                                    )
                                                                }
                                                                if (description.isNotEmpty()) {
                                                                    Row(
                                                                        modifier = Modifier
                                                                            .fillMaxWidth(),
                                                                        verticalAlignment = Alignment.Top
                                                                    ) {
                                                                        Icon(
                                                                            imageVector = ImageVector.vectorResource(
                                                                                R.drawable.message
                                                                            ),
                                                                            contentDescription = "",
                                                                            tint = theme["icon"]!!
                                                                        )
                                                                        Spacer(modifier = Modifier.size(24.dp))
                                                                        Text(
                                                                            text = if (description.length > 50) description.take(
                                                                                50
                                                                            ) + "..." else description,
                                                                            fontSize = 20.sp,
                                                                            fontFamily = RubikFont,
                                                                            color = theme["text"]!!
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }


                                                        if(userID !in usersLiked) {
                                                            HorizontalDivider(
                                                                modifier = Modifier.padding(horizontal = 12.dp),
                                                                thickness = 1.dp,
                                                                color = theme["icon"]!!.copy(alpha = 0.2f)
                                                            )

                                                            Box(
                                                                modifier = Modifier
                                                                    .clickable {
                                                                        incidentsManager.addUserLike(id, userID)
                                                                        incidentsManager.updateUserThanksGivenCount(userID, id)
                                                                    }
                                                                    .fillMaxWidth()
                                                                    .padding(16.dp)
                                                            ) {
                                                                Row(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth(),
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.ThumbUp,
                                                                        contentDescription = "",
                                                                        tint = theme["icon"]!!
                                                                    )
                                                                    Spacer(modifier = Modifier.size(24.dp))
                                                                    Text(
                                                                        text = localization["helped_me"]!!,
                                                                        fontSize = 18.sp,
                                                                        fontFamily = RubikFont,
                                                                        fontWeight = FontWeight.SemiBold,
                                                                        color = theme["primary"]!!,
                                                                        modifier = Modifier.animateContentSize()
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        HorizontalDivider(
                                                            modifier = Modifier.padding(horizontal = 12.dp),
                                                            thickness = 1.dp,
                                                            color = theme["icon"]!!.copy(alpha = 0.2f)
                                                        )

                                                        Box(
                                                            modifier = Modifier
                                                                .clickable {
                                                                    val currentTime = System.currentTimeMillis()
                                                                    val creationTime = creationDate.time
                                                                    val timeDifference = currentTime - creationTime
                                                                    if (timeDifference > 3 * 60 * 60 * 1000) {
                                                                        Toast.makeText(context, localization["incident_has_been_ended"]!!, Toast.LENGTH_LONG).show()
                                                                        incidentLoadingTrigger =
                                                                            !incidentLoadingTrigger
                                                                        return@clickable
                                                                    }

                                                                    val createdBy = incident.createdBy
                                                                    val photos = incident.photos

                                                                    val incidentInfo = Incident(
                                                                        id = id,
                                                                        type = IncidentType.fromValue(type),
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
                                                                    commentManager.clearCommentsAndAuthors()
                                                                    navController.navigate(Routes.COMMENTS)
                                                                }
                                                                .fillMaxWidth()
                                                        ) {
                                                            Column(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(16.dp)
                                                            ) {
                                                                Row(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth(),
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                                ) {
                                                                    Row {
                                                                        Icon(
                                                                            imageVector = ImageVector.vectorResource(
                                                                                R.drawable.comments
                                                                            ),
                                                                            contentDescription = "",
                                                                            tint = theme["icon"]!!
                                                                        )
                                                                        Spacer(modifier = Modifier.size(24.dp))
                                                                        Text(
                                                                            text = "${
                                                                                incident.commentCount
                                                                            } ${when {
                                                                                incident.commentCount == 0 -> localization["comments_zero"]!!
                                                                                incident.commentCount % 100 in 11..14 -> localization["comments_many"]!!
                                                                                incident.commentCount % 10 == 1 -> localization["comments_one"]!!
                                                                                incident.commentCount % 10 in 2..4 -> localization["comments_few"]!!
                                                                                else -> localization["comments_many"]!!
                                                                            }}",
                                                                            fontSize = 18.sp,
                                                                            fontFamily = RubikFont,
                                                                            color = theme["text"]!!
                                                                        )
                                                                    }
                                                                    Icon(
                                                                        imageVector = ImageVector.vectorResource(
                                                                            R.drawable.arrow_right
                                                                        ),
                                                                        contentDescription = "",
                                                                        tint = theme["icon"]!!
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            item { Spacer(modifier = Modifier) }
                                        }
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
                }
                FloatingActionButton(
                    onClick = {
                        incidentsManager.stopListeningIncidents()
                        navController.navigate(Routes.ADD_NEW_INCIDENT)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = theme["primary"]!!,
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        imageVector = Icons.Filled.Add,
                        contentDescription = "",
                        tint = theme["icon"]!!
                    )
                }
                if (locationRequestState == LocationRequestState.Success) {
                    FloatingActionButton(
                        onClick = { showSosDialog = true },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        containerColor = theme["error"]!!,
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sos,
                            contentDescription = "",
                            tint = theme["icon"]!!,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                if (showSosDialog) {
                    AlertDialog(
                        onDismissRequest = { showSosDialog = false },
                        title = {
                            Text(
                                text = localization["sos_dialog_title"]!!,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Medium,
                                color = theme["text"]!!
                            )
                        },
                        text = {
                            var pressDuration by remember { mutableLongStateOf(0L) }
                            val maxPressDuration = 5000L
                            var isHolding by remember { mutableStateOf(false) }
                            var timerStarted by remember { mutableStateOf(false) }
                            var startTime by remember { mutableLongStateOf(0L) }

                            LaunchedEffect(isHolding) {
                                if (isHolding) {
                                    if (!timerStarted) {
                                        timerStarted = true
                                        pressDuration = 0
                                    }

                                    while (pressDuration < maxPressDuration) {
                                        pressDuration = System.currentTimeMillis() - startTime
                                        delay(100)
                                    }

                                    locationHandler.getAddressFromCoordinates(
                                        context, localization,
                                        latitude!!, longitude!!
                                    ) { address ->
                                        if (address != null) {
                                            val parts = address.split(",").map { it.trim() }
                                            val street = parts.getOrNull(0) ?: localization["unknown_street"]!!
                                            val buildingNumber = parts.getOrNull(1) ?: localization["unknown_number"]!!

                                            incidentsManager.addNewIncident(
                                                authViewModel = authViewModel,
                                                commentManager = commentManager,
                                                context = context,
                                                photoUris = emptyList(),
                                                type = IncidentType.SOS,
                                                description = "",
                                                address = "$street, $buildingNumber",
                                                latitude = userLocation!!.latitude.toString(),
                                                longitude = userLocation.longitude.toString(),
                                                localization = localization
                                            )

                                            showSosDialog = false
                                        } else {
                                            Toast.makeText(context, localization["get_address_fail"]!!, Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            }

                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = localization["sos_dialog_description"]!!,
                                    fontFamily = RubikFont,
                                    fontWeight = FontWeight.Normal,
                                    color = theme["placeholder"]!!
                                )
                                if (isHolding) {
                                    LinearProgressIndicator(
                                        progress = { (pressDuration.toFloat() / maxPressDuration) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(16.dp)
                                            .padding(bottom = 8.dp),
                                        color = theme["primary"]!!,
                                        trackColor = theme["placeholder"]!!,
                                        gapSize = 2.dp,
                                        strokeCap = StrokeCap.Round
                                    )
                                } else {
                                    TextButton(
                                        onClick = {
                                            isHolding = true
                                            startTime = System.currentTimeMillis()
                                        },
                                        shape = RoundedCornerShape(25.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = theme["primary"]!!)
                                    ) {
                                        Text(
                                            text = localization["sos_dialog_confirm_button"]!!,
                                            fontFamily = RubikFont,
                                            fontWeight = FontWeight.Medium,
                                            color = theme["text"]!!
                                        )
                                    }
                                }
                            }
                        },
                        confirmButton = {

                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showSosDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = theme["primary"]!!)
                            ) {
                                Text(
                                    text = localization["sos_dialog_cancel_button"]!!,
                                    fontFamily = RubikFont,
                                    fontWeight = FontWeight.Medium,
                                    color = theme["text"]!!
                                )
                            }
                        },
                        containerColor = theme["background"]!!
                    )
                }
            }
        }
    }
}
