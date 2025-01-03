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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.denisshulika.road_radar.pages.RubikFont
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.PlaceTypes
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
    placeholder: String
) {
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
                            }
                            .addOnFailureListener {
                                suggestions = emptyList()
                            }
                    }
                } else {
                    suggestions = emptyList()
                }
            },
            placeholder = placeholder
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
                            .padding(8.dp)
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun AutocompleteTextFieldForDistrict(
    value: String,
    placesClient: PlacesClient,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    onPlaceSelected: (String) -> Unit,
    placeholder: String,
    region: String
) {
    var suggestions by remember { mutableStateOf(listOf<String>()) }
    var debounceJob by remember { mutableStateOf<Job?>(null) }

    val sevastopolDistricts = listOf(
        "Балаклавський район",
        "Гагарінський район",
        "Ленінський район",
        "Нахімовський район"
    )

    Column(modifier = modifier) {
        StyledBasicTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
                debounceJob?.cancel()
                if (newValue.trim().length > 2) {
                    debounceJob = CoroutineScope(Dispatchers.Main).launch {
                        delay(300)

                        if (region.contains("Севастополь", ignoreCase = true) && !newValue.startsWith("рай", ignoreCase = true)) {
                            suggestions = sevastopolDistricts.filter { it.contains(newValue, ignoreCase = true) }
                        } else {
                            val token = AutocompleteSessionToken.newInstance()
                            val request = FindAutocompletePredictionsRequest.builder()
                                .setQuery("$region $newValue")
                                .setCountries(listOf("UA"))
                                .setSessionToken(token)
                                .build()

                            placesClient.findAutocompletePredictions(request)
                                .addOnSuccessListener { response ->
                                    suggestions = response.autocompletePredictions
                                        .map { it.getPrimaryText(null).toString() }
                                        .filter { suggestion -> suggestion.endsWith("район", ignoreCase = true) }
                                }
                                .addOnFailureListener {
                                    suggestions = emptyList()
                                }
                        }
                    }
                } else {
                    suggestions = emptyList()
                }
            },
            placeholder = placeholder
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
                        style = MaterialTheme.typography.bodySmall
                    )
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = Color(0xFFADADAD)
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
    district: String,
    context: Context
) {
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
                            .setQuery("$district $newValue")
                            .setCountries(listOf("UA"))
                            .setSessionToken(token)
                            .setTypesFilter(listOf(PlaceTypes.ADDRESS))
                            .build()

                        placesClient.findAutocompletePredictions(request)
                            .addOnSuccessListener { response ->
                                suggestions = response.autocompletePredictions
                                    .map { it.getFullText(null).toString() }
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
            singleLine = false
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
                            .padding(8.dp)
                    )
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = Color(0xFFADADAD)
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