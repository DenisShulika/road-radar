package com.denisshulika.road_radar

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.denisshulika.road_radar.local.SettingsLocalStorage
import com.denisshulika.road_radar.model.ThemeState
import com.denisshulika.road_radar.util.readValueFromJsonFile
import com.google.android.libraries.places.api.Places
import kotlinx.coroutines.launch
import java.util.Locale

@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT.also { requestedOrientation = it }
        enableEdgeToEdge()

        val authViewModel: AuthViewModel by viewModels()
        val incidentManager: IncidentManager by viewModels()
        val settingsViewModel: SettingsViewModel by viewModels()

        val settingsStorage = SettingsLocalStorage(this, settingsViewModel)

        incidentManager.deleteOldIncidents()

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.PLACES_API_KEY, Locale("uk"))
        }

        setContent {

        }
        lifecycleScope.launch {
            settingsStorage.initializeSettings(this@MainActivity)

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
                        modifier = Modifier,
                        authViewModel = authViewModel,
                        incidentManager = incidentManager,
                        placesClient = Places.createClient(this@MainActivity),
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}