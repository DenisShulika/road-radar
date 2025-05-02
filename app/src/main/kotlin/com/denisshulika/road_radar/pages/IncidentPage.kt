package com.denisshulika.road_radar.pages

import android.icu.text.SimpleDateFormat
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.denisshulika.road_radar.IncidentsManager
import com.denisshulika.road_radar.R
import com.denisshulika.road_radar.SettingsViewModel
import com.denisshulika.road_radar.model.IncidentType
import com.denisshulika.road_radar.model.ThemeState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentPage(
    navController: NavController,
    settingsViewModel: SettingsViewModel,
    incidentsManager: IncidentsManager
) {
    val context = LocalContext.current

    val incidentInfo by incidentsManager.selectedDocumentInfo.observeAsState()

    val localization = settingsViewModel.localization.observeAsState().value!!
    val theme = settingsViewModel.themeColors.observeAsState().value!!

    val systemUiController = rememberSystemUiController()

    systemUiController.setStatusBarColor(
        color = theme["top_bar_background"]!!,
        darkIcons = settingsViewModel.getTheme() != ThemeState.DARK || !isSystemInDarkTheme()
    )
    systemUiController.setNavigationBarColor(
        color = theme["background"]!!,
        darkIcons = settingsViewModel.getTheme() != ThemeState.DARK || !isSystemInDarkTheme()
    )

    val dateFormat = SimpleDateFormat(
        "EEEE, dd MMMM".trimIndent(),
        Locale(localization["date_format_language"]!!, localization["date_format_country"]!!)
    )
    val timeFormat = SimpleDateFormat(
        "'at' HH:mm:ss".trimIndent(),
        Locale(localization["date_format_language"]!!, localization["date_format_country"]!!)
    )

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedImageUrl by remember { mutableStateOf("") }

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
                            text = localization["incident_info_title"]!!,
                            textAlign = TextAlign.Center,
                            fontFamily = RubikFont,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
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
                incidentInfo?.let { info ->
                    val creationDate = info.creationDate.toDate()
                    val formatedDate = dateFormat.format(creationDate)
                    val formatedTime = timeFormat.format(creationDate)

                    val type = when (info.type) {
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
                    }

                    if (showBottomSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showBottomSheet = false },
                            sheetState = bottomSheetState,
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                                .navigationBarsPadding(),
                            dragHandle = {},
                            shape = RectangleShape,
                            containerColor = theme["background"]!!
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                ZoomableImage(selectedImageUrl, theme)

                                IconButton(
                                    onClick = {
                                        showBottomSheet = false
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .wrapContentSize()
                                        .padding(12.dp)
                                        .background(
                                            color = Color.Black.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(50)
                                        ),
                                ) {
                                    Icon(
                                        modifier = Modifier.size(24.dp),
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "",
                                        tint = theme["icon"]!!
                                    )
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${localization["incident_type_subtext"]!!} ${
                                    when (info.type) {
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
                                    }
                                }",
                                fontSize = 20.sp,
                                fontFamily = RubikFont,
                                color = theme["text"]!!
                            )
                            val iconRes = when (type) {
                                IncidentType.CAR_ACCIDENT.value -> R.drawable.car_accident
                                IncidentType.ROADBLOCK.value -> R.drawable.roadblock
                                IncidentType.WEATHER_CONDITIONS.value -> R.drawable.weather_warning
                                IncidentType.TRAFFIC_JAM.value -> R.drawable.traffic_jam
                                IncidentType.ROAD_WORKS.value -> R.drawable.road_works
                                IncidentType.POLICE_ACTIVITY.value -> R.drawable.police_activity
                                IncidentType.BROKEN_DOWN_VEHICLE.value -> R.drawable.broken_down_vehicle
                                IncidentType.FLOODING.value -> R.drawable.flooding
                                IncidentType.FIRE_NEAR_ROAD.value -> R.drawable.fire_near_road
                                IncidentType.OBSTACLE_ON_ROAD.value -> R.drawable.obstacle_on_road
                                IncidentType.OTHER.value -> R.drawable.warning
                                else -> R.drawable.warning
                            }

                            Spacer(modifier = Modifier.size(8.dp))

                            Icon(
                                painter = painterResource(id = iconRes),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified
                            )
                        }
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = theme["accent"]!!
                        )
                        Text(
                            text = "${localization["incident_date_subtext"]!!} $formatedDate $formatedTime",
                            fontSize = 20.sp,
                            fontFamily = RubikFont,
                            color = theme["text"]!!
                        )
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = theme["accent"]!!
                        )
                        Text(
                            text = "${localization["incident_address_subtext"]!!} ${info.address}",
                            fontSize = 20.sp,
                            fontFamily = RubikFont,
                            color = theme["text"]!!
                        )
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = theme["accent"]!!
                        )
                        if(info.description.isNotEmpty()){
                            Text(
                                text = "${localization["incident_description_subtext"]!!} ${info.description}",
                                fontSize = 20.sp,
                                fontFamily = RubikFont,
                                color = theme["text"]!!
                            )
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = theme["accent"]!!
                            )
                        }
                        if (info.photos.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(info.photos.size) { index ->
                                    val imageUrl = info.photos[index]
                                    val painter = rememberAsyncImagePainter(imageUrl)
                                    val painterState = painter.state
                                    Box(
                                        modifier = Modifier
                                            .size(180.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Gray.copy(alpha = 0.2f))
                                    ) {
                                        Image(
                                            painter = painter,
                                            contentDescription = "",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .matchParentSize()
                                                .clickable {
                                                    selectedImageUrl = imageUrl
                                                    showBottomSheet = true
                                                },
                                            contentScale = ContentScale.Crop
                                        )
                                        if (painterState is AsyncImagePainter.State.Loading) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier
                                                        .size(48.dp),
                                                    color = theme["icon"]!!
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = theme["accent"]!!
                            )
                        }
                        Text(
                            text = "${localization["incident_created_by_subtext"]!!} ${info.createdBy}",
                            fontSize = 20.sp,
                            fontFamily = RubikFont,
                            color = theme["text"]!!
                        )
                        Spacer(modifier = Modifier)
                    }
                }
            }
        }
    }
}
