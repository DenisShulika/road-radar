package com.denisshulika.road_radar.ui.components

import android.content.Context
import android.location.Geocoder
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.denisshulika.road_radar.SettingsViewModel
import com.denisshulika.road_radar.pages.RubikFont
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale

@Composable
fun AutocompleteTextFieldForRegion(
    value: String,
    placesClient: PlacesClient,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    onPlaceSelected: (String) -> Unit,
    placeholder: String,
    settingsViewModel: SettingsViewModel
) {
    val theme = settingsViewModel.themeColors.observeAsState().value!!

    var suggestions by remember { mutableStateOf(listOf<String>()) }
    var debounceJob by remember { mutableStateOf<Job?>(null) }

    Column(modifier = modifier) {
        StyledBasicTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
                debounceJob?.cancel()
                if (newValue.trim().length > 2) {
                    debounceJob = CoroutineScope(Dispatchers.Main).launch {
                        delay(300)
                        val token = AutocompleteSessionToken.newInstance()
                        val request = FindAutocompletePredictionsRequest.builder()
                            .setQuery(newValue)
                            .setCountries(listOf("UA"))
                            .setTypesFilter(listOf("administrative_area_level_1"))
                            .setSessionToken(token)
                            .build()

                        placesClient.findAutocompletePredictions(request)
                            .addOnSuccessListener { response ->
                                suggestions = response.autocompletePredictions
                                    .map { it.getPrimaryText(null).toString() }
                                    .filter { suggestion ->
                                        suggestion.none { it in 'a'..'z' || it in 'A'..'Z' }
                                    }
                            }
                            .addOnFailureListener {
                                suggestions = emptyList()
                            }
                    }
                } else {
                    suggestions = emptyList()
                }
            },
            placeholder = placeholder,
            theme = theme
        )

        AnimatedVisibility(
            suggestions.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(suggestions) { suggestion ->
                    Text(
                        text = suggestion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onPlaceSelected(suggestion)
                                suggestions = emptyList()
                            }
                            .padding(8.dp),
                        fontFamily = RubikFont,
                        color = theme["text"]!!
                    )
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = theme["accent"]!!
                    )
                }
            }
        }
    }
}

@Composable
fun AutocompleteTextFieldForAddress(
    value: String,
    placesClient: PlacesClient,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    onPlaceSelected: (String, Double, Double) -> Unit,
    placeholder: String,
    region: String,
    context: Context,
    settingsViewModel: SettingsViewModel
) {
    val theme = settingsViewModel.themeColors.observeAsState().value!!

    var suggestions by remember { mutableStateOf(listOf<String>()) }
    var debounceJob by remember { mutableStateOf<Job?>(null) }

    Column(modifier = modifier) {
        StyledBasicTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
                debounceJob?.cancel()
                if (newValue.trim().length > 2) {
                    debounceJob = CoroutineScope(Dispatchers.Main).launch {
                        delay(300)

                        val token = AutocompleteSessionToken.newInstance()
                        val request = FindAutocompletePredictionsRequest.builder()
                            .setQuery("$region $newValue")
                            .setSessionToken(token)
                            .setCountries(listOf("UA", "RU"))
                            .setTypesFilter(listOf("address"))
                            .build()

                        placesClient.findAutocompletePredictions(request)
                            .addOnSuccessListener { response ->
                                suggestions = response.autocompletePredictions
                                    .asSequence()
                                    .map { it.getFullText(null).toString() }
                                    .map { suggestion ->
                                        suggestion.replace(Regex(", Україна(, \\d{5})?$"), "")
                                    }
                                    .filter { suggestion ->
                                        suggestion.none { it in 'a'..'z' || it in 'A'..'Z' }
                                    }
                                    .filter { suggestion ->
                                        if (region == "місто Севастополь" || region == "Автономна Республіка Крим") {
                                            !suggestion.contains(Regex("[ыЫЁё]")) &&
                                                    !suggestion.contains(Regex("\\b(улица|ул\\.|дом|пл\\.|кв\\.|город)\\b", RegexOption.IGNORE_CASE)) &&
                                                    (suggestion.endsWith("Севастополь") || suggestion.endsWith("Автономна Республіка Крим"))
                                        } else {
                                            true
                                        }
                                    }
                                    .toList()
                            }
                            .addOnFailureListener {
                                suggestions = emptyList()
                            }
                    }
                } else {
                    suggestions = emptyList()
                }
            },
            placeholder = placeholder,
            singleLine = false,
            theme = theme
        )

        AnimatedVisibility(
            suggestions.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(suggestions) { suggestion ->
                    Text(
                        text = suggestion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                getCoordinatesForStreet(context, suggestion) { latitude, longitude ->
                                    onPlaceSelected(suggestion, latitude, longitude)
                                }
                                suggestions = emptyList()
                            }
                            .padding(8.dp),
                        fontFamily = RubikFont,
                        color = theme["text"]!!
                    )
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = theme["accent"]!!
                    )
                }
            }
        }
    }
}


private fun getCoordinatesForStreet(
    context: Context,
    address: String,
    onCoordinatesReceived: (Double, Double) -> Unit
) {
    val geocoder = Geocoder(context, Locale.getDefault())
    try {
        @Suppress("DEPRECATION") val addresses = geocoder.getFromLocationName(address, 1)
        if (!addresses.isNullOrEmpty()) {
            val latitude = addresses[0].latitude
            val longitude = addresses[0].longitude
            onCoordinatesReceived(latitude, longitude)
        } else {
            onCoordinatesReceived(0.0, 0.0)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}