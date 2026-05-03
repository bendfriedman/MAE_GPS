package com.example.gpsreceiver

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.gpsreceiver.ui.theme.GPSReceiverTheme
import android.app.AlertDialog
import android.hardware.SensorEvent
import android.icu.text.DecimalFormat
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
class MainActivity : ComponentActivity() {

    var locationManager : LocationManager? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 123
    private val viewModel by viewModels<MyViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?
        registerLocationListener()
        val sensorManager: SensorManager? = getSystemService(SENSOR_SERVICE) as SensorManager?
        val sensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_PRESSURE)

        sensorManager?.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)

        setContent {
            GPSReceiverTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Values(
                        location = viewModel.strLocation,
                        pressure = viewModel.strPressure,
                        height = viewModel.strHeight,
                        speed = viewModel.strSpeed,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    fun registerLocationListener() {
        if(ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        ) {
            showPermissionRationaleDialog()
            return
        }
        locationManager?.requestLocationUpdates(
            LocationManager.GPS_PROVIDER, 0, 0f, locationListener
        )

    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Location Permission Required!!")
            .setMessage("This app needs the location permission, please accept to use location functionality")
            .setPositiveButton("Grant Permission") {dialog, _ ->
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
            }
            .setNegativeButton("Deny") {dialog, _ ->
                Toast.makeText(this, "Location permission denied!!", Toast.LENGTH_LONG).show()
            }
            .show()
    }

    val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: android.location.Location) {
            Log.d("MainActivity", "onLocationChanged")

            val longitude = location.longitude
            val decimalFormat = DecimalFormat("#.#####")
            val strLongitude = decimalFormat.format(longitude)
            val latitude = location.latitude
            val strLatitude = decimalFormat.format(latitude)
            val altitude = location.altitude
            val strAltitude = decimalFormat.format(altitude)
            val speed = location.speed
            val strSpeed = decimalFormat.format(speed)

            val txtLocation = "Longitude: $strLongitude\nLatitude: $strLatitude"
            viewModel.strLocation = txtLocation
            if (location.hasAltitude()) {
                viewModel.strHeight = "Height: $strAltitude m"
            } else {
                viewModel.strHeight = "Height: not available :("
            }
            viewModel.strSpeed = "Speed: $strSpeed m/s"

        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            // leer lassen ist ok
        }

    }

    private val sensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            var txtPressure = "Pressure: " + event?.values?.get(0).toString() + " hPa"
            viewModel.strPressure = txtPressure
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }


}

@Composable
fun Values(location: String, pressure: String, height: String, speed: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "map of the world",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
//            alpha = 0.95f
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )
        Column(
                modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "GPS Receiver",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp),
                color = MaterialTheme.colorScheme.background
            )

            GpsCard(label = "Location", value = location)
            GpsCard(label = "Altitude", value = height)
            GpsCard(label = "Speed", value = speed)
            GpsCard(label = "Air Pressure", value = pressure)
        }
    }
}

@Composable
fun GpsCard(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
class MyViewModel : ViewModel() {
    var strLocation by mutableStateOf("location")
    var strPressure by mutableStateOf("pressure")
    var strHeight by mutableStateOf("height")
    var strSpeed by mutableStateOf("speed")
}

@Preview(showBackground = true)
@Composable
fun ValuesPreview() {
    GPSReceiverTheme {
        Values("location", "pressure", "height", "speed")
    }
}