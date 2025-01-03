package com.denisshulika.road_radar

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.google.android.libraries.places.api.Places
import java.util.Locale

@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT.also { requestedOrientation = it }
        enableEdgeToEdge()

        val authViewModel : AuthViewModel by viewModels()

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.PLACES_API_KEY, Locale("uk"))
        }

        val placesClient = Places.createClient(this)

        setContent {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                RoadRadarNavigation(
                    modifier = Modifier,
                    authViewModel = authViewModel,
                    placesClient = placesClient
                )
            }
        }
    }
}
