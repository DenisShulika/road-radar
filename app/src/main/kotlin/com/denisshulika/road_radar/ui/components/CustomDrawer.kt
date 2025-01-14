package com.denisshulika.road_radar.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.denisshulika.road_radar.AuthViewModel
import com.denisshulika.road_radar.IncidentManager
import com.denisshulika.road_radar.R
import com.denisshulika.road_radar.Routes
import com.denisshulika.road_radar.SettingsViewModel
import com.denisshulika.road_radar.local.UserLocalStorage
import com.denisshulika.road_radar.model.NavigationItem
import com.denisshulika.road_radar.pages.RubikFont
import com.google.android.libraries.places.api.model.kotlin.localTime

@Composable
fun CustomDrawer (
    selectedNavigationItem : NavigationItem,
    onNavigationItemClick : (NavigationItem) -> Unit,
    onCloseClick : () -> Unit,
    navController: NavController,
    authViewModel : AuthViewModel,
    settingsViewModel: SettingsViewModel,
    incidentManager: IncidentManager
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val localization = settingsViewModel.localization.observeAsState().value!!
    val theme = settingsViewModel.themeColors.observeAsState().value!!

    val userName = remember { mutableStateOf("") }
    val userPhotoUrl = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        userPhotoUrl.value = UserLocalStorage(context).getUserPhotoUrl()
        userName.value = UserLocalStorage(context).getUserName() ?: localization["user_name_fail"] ?: "Failed to get a name"
    }
    Column(
        modifier = Modifier
            .padding(
                bottom = 24.dp,
                start = 12.dp,
                end = 12.dp,
                top = 24.dp
            )
            .fillMaxHeight()
            .fillMaxWidth(fraction = 0.6f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            IconButton(
                onClick = onCloseClick
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "",
                    tint = theme["icon"]!!
                )
            }
        }
        Spacer(modifier = Modifier.size(24.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (userPhotoUrl.value != null) {
                    Image(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        painter = rememberAsyncImagePainter(userPhotoUrl.value),
                        contentDescription = ""
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme["background"]!!),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.image_load_fail),
                            contentDescription = "",
                            tint = theme["icon"]!!
                        )
                    }
                }
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = userName.value,
                    fontSize = 20.sp,
                    color = theme["text"]!!,
                    fontFamily = RubikFont,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(modifier = Modifier.size(40.dp))
        NavigationItem.entries.toTypedArray().take(3).forEach { navigationItem ->
            NavigationDrawerItem(
                settingsViewModel = settingsViewModel,
                navigationItem = navigationItem,
                selected = navigationItem == selectedNavigationItem,
                onClick = {
                    when (navigationItem) {
                        NavigationItem.Incidents -> {
                            if(selectedNavigationItem != NavigationItem.Incidents) {
                                onNavigationItemClick(NavigationItem.Incidents)
                                navController.navigate(Routes.INCIDENTS)
                            }
                        }
                        NavigationItem.MapRadar -> {
                            if(selectedNavigationItem != NavigationItem.MapRadar) {
                                onNavigationItemClick(NavigationItem.MapRadar)
                                navController.navigate(Routes.MAP_RADAR)
                            }
                        }
                        NavigationItem.Profile -> {
                            if(selectedNavigationItem != NavigationItem.Profile) {
                                onNavigationItemClick(NavigationItem.Profile)
                                navController.navigate(Routes.PROFILE)
                            }
                        }
                        else -> {}
                    }
                },
                theme = theme
            )
            Spacer(modifier = Modifier.size(8.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
        NavigationItem.entries.toTypedArray().takeLast(3).forEach { navigationItem ->
            NavigationDrawerItem(
                settingsViewModel =settingsViewModel,
                navigationItem = navigationItem,
                selected = navigationItem == selectedNavigationItem,
                onClick = {
                    when (navigationItem) {
                        NavigationItem.Settings -> {
                            if(selectedNavigationItem != NavigationItem.Settings) {
                                onNavigationItemClick(NavigationItem.Settings)
                                navController.navigate(Routes.SETTINGS)
                            }
                        }
                        NavigationItem.About -> {
                            if(selectedNavigationItem != NavigationItem.About) {
                                onNavigationItemClick(NavigationItem.About)
                                navController.navigate(Routes.ABOUT)
                            }
                        }
                        NavigationItem.Signout -> {
                            authViewModel.signout(context, coroutineScope, incidentManager)
                        }
                        else -> {}
                    }
                },
                theme = theme
            )
            Spacer(modifier = Modifier.size(8.dp))
        }
    }
}