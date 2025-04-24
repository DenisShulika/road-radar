package com.denisshulika.road_radar

import android.Manifest
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.denisshulika.road_radar.pages.GeocodingResponse
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Locale

class LocationHandler(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>()

    private val _locationRequestState = MutableLiveData<LocationRequestState>(LocationRequestState.Idle)
    val locationRequestState: LiveData<LocationRequestState> = _locationRequestState

    private val _userLocation = MutableLiveData<Location?>(null)
    val userLocation: LiveData<Location?> get() = _userLocation

    private val _lastUpdateTime = MutableLiveData(0L)
    val lastUpdateTime: LiveData<Long> get() = _lastUpdateTime

    private val _locationTimeout = MutableLiveData(false)

    private var timerJob: Job? = null

    private fun resetTimeout() {
        _locationTimeout.postValue(false)
        timerJob?.cancel()
    }

    private fun startTimeout() {
        resetTimeout()
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            delay(10000)
            if (_userLocation.value == null) {
                _locationTimeout.postValue(true)
                _locationRequestState.postValue(LocationRequestState.NoLocation)
            }
        }
    }

    fun setLocationRequestState(state: LocationRequestState){
        _locationRequestState.value = state
    }

    fun setLastUpdateTime(time: Long){
        _lastUpdateTime.value = time
    }

    private fun requestLocation(onLocationReceived: (Location?) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onLocationReceived(null)
            return
        }

        val locationInterval = 1000L
        val locationFastestInterval = 100L
        val locationMaxWaitTime = 10000L
        val locationDuration = 10000L
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, locationInterval)
            .setWaitForAccurateLocation(false)
            .setDurationMillis(locationDuration)
            .setMinUpdateIntervalMillis(locationFastestInterval)
            .setMaxUpdateDelayMillis(locationMaxWaitTime)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                location?.let {
                    onLocationReceived(location)
                    fusedLocationClient.removeLocationUpdates(this)
                } ?: run {
                    onLocationReceived(null)
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun handlePermissionResult(
        permissions: Map<String, Boolean>,
        onSuccess: (Location) -> Unit
    ) {
        _locationRequestState.postValue(LocationRequestState.Loading)
        _userLocation.postValue(null)

        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (!fineGranted && !coarseGranted) {
            _locationRequestState.postValue(LocationRequestState.NoPermission)
            return
        }

        startTimeout()

        requestLocation { location ->
            resetTimeout()

            if (location == null) {
                _locationRequestState.postValue(LocationRequestState.NoLocation)
            } else {
                _lastUpdateTime.postValue(System.currentTimeMillis())
                _userLocation.postValue(location)
                _locationRequestState.postValue(LocationRequestState.Success)
                onSuccess(location)
            }
        }
    }

    @Suppress("DEPRECATION")
    fun getAddressFromCoordinates(
        context: Context,
        localization: Map<String, String>,
        latitude: Double,
        longitude: Double,
        callback: (String?) -> Unit
    ) {
        val geocoder = Geocoder(context, Locale.getDefault())

        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses?.isNotEmpty() == true) {
                val address = addresses[0].getAddressLine(0) ?: localization["unknown_location"]!!
                callback(address)
                return
            }
        } catch (e: Exception) {
            Toast.makeText(context, "${e.message}", Toast.LENGTH_LONG).show()
        }

        val apiKey = BuildConfig.MAPS_API_KEY
        val url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=$latitude,$longitude&key=$apiKey"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                val body = response.body?.string()
                if (body != null) {
                    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                    val jsonAdapter = moshi.adapter(GeocodingResponse::class.java)
                    val result = jsonAdapter.fromJson(body)

                    val fullAddress = result?.results?.firstOrNull()?.formattedAddress
                    callback(fullAddress)
                } else {
                    callback(null)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "${e.message}", Toast.LENGTH_LONG).show()
                callback(null)
            }
        }.start()
    }

    fun showLocationPermissionDialog(
        context: Context,
        localization: Map<String, String>
    ) {
        val dialog = AlertDialog.Builder(context)
            .setTitle(localization["permission_title"]!!)
            .setMessage(localization["permission_message"]!!)
            .setPositiveButton(localization["permission_positive_button"]!!) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", context.packageName, null)
                intent.data = uri
                context.startActivity(intent)
            }
            .setNegativeButton(localization["permission_negative_button"]!!) { dialog, _ -> dialog.dismiss() }
            .create()

        dialog.show()
    }
}

sealed class LocationRequestState {
    data object Idle : LocationRequestState()
    data object Loading : LocationRequestState()
    data object Success : LocationRequestState()
    data object NoLocation : LocationRequestState()
    data object NoPermission : LocationRequestState()
    data class Error(val message: String) : LocationRequestState()
}