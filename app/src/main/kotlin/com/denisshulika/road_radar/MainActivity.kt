package com.denisshulika.road_radar

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.denisshulika.road_radar.local.SettingsLocalStorage
import com.denisshulika.road_radar.model.ThemeState
import com.denisshulika.road_radar.util.readValueFromJsonFile
import kotlinx.coroutines.launch

@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT.also { requestedOrientation = it }
        enableEdgeToEdge()

        val authViewModel: AuthViewModel by viewModels()
        val incidentsManager: IncidentsManager by viewModels()
        val settingsViewModel: SettingsViewModel by viewModels()
        val locationHandler: LocationHandler by viewModels()
        val commentManager: CommentManager by viewModels()

        val settingsStorage = SettingsLocalStorage(this, settingsViewModel)

        incidentsManager.deleteOldIncidents(this)

        var isSystemInDarkTheme = false
        setContent {
            isSystemInDarkTheme = isSystemInDarkTheme()
            Surface(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF219FD9)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(25.dp)),
                        painter = painterResource(R.drawable.logo_icon),
                        contentDescription = ""
                    )
                }
            }
        }

        lifecycleScope.launch {
            settingsStorage.initializeSettings(this@MainActivity, isSystemInDarkTheme)

            setContent {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val theme = settingsViewModel.getTheme()
                    val language = settingsViewModel.getLanguage()

                    if (theme == ThemeState.SYSTEM && isSystemInDarkTheme()) {
                        settingsViewModel.setTheme(ThemeState.DARK)
                    } else if (theme == ThemeState.SYSTEM && !isSystemInDarkTheme()) {
                        settingsViewModel.setTheme(ThemeState.LIGHT)
                    }

                    settingsViewModel.setLocalisation(readValueFromJsonFile(language.value, this@MainActivity)!!)

                    RoadRadarNavigation(
                        authViewModel = authViewModel,
                        incidentsManager = incidentsManager,
                        settingsViewModel = settingsViewModel,
                        locationHandler = locationHandler,
                        commentManager = commentManager
                    )
                }
            }
        }
    }
}