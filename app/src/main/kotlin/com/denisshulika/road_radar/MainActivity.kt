package com.denisshulika.road_radar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val authViewModel : AuthViewModel by viewModels()

        setContent {
            Scaffold (modifier = Modifier.fillMaxSize()) { innerPadding ->
                RoadRadarNavigation(
                    modifier = Modifier
                        .padding(innerPadding),
                    authViewModel = authViewModel
                )
            }
        }
    }
}