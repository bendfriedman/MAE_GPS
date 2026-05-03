package com.example.gpsreceiver

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class GpsData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val speed: Float = 0.0f,
    val hasData: Boolean = false
)

class GpsViewModel : ViewModel() {

    private val _gpsData = MutableStateFlow(GpsData())
    val gpsData: StateFlow<GpsData> = _gpsData

    fun updateLocation(latitude: Double, longitude: Double, altitude: Double, speed: Float) {
        _gpsData.value = GpsData(
            latitude = latitude,
            longitude = longitude,
            altitude = altitude,
            speed = speed,
            hasData = true
        )
    }
}
