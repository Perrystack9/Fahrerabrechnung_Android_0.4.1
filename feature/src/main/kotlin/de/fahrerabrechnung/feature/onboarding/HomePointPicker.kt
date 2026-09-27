package de.fahrerabrechnung.feature.onboarding

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

@Composable
internal fun HomePointPicker(latitude: String, longitude: String, onPointChange: (Double, Double) -> Unit) {
    val context = LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }
    var mapView by remember { mutableStateOf<HomeMapView?>(null) }
    var showMap by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    val currentOnPointChange by rememberUpdatedState(onPointChange)
    val requestLocation: () -> Unit = requestLocation@{
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            error = "Standortberechtigung fehlt."
            return@requestLocation
        }
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            error = "GPS ist ausgeschaltet. Du kannst die Nadel auf der Karte selbst setzen."
        } else {
            try {
                if (Build.VERSION.SDK_INT >= 30) {
                    manager.getCurrentLocation(LocationManager.GPS_PROVIDER, null, ContextCompat.getMainExecutor(context)) { location ->
                        if (location == null) error = "Kein Standort gefunden. Bitte versuche es erneut oder setze die Nadel selbst."
                        else {
                            pending = location.latitude to location.longitude
                            showMap = true
                            mapView?.setPoint(location.latitude, location.longitude)
                            error = null
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    manager.requestSingleUpdate(LocationManager.GPS_PROVIDER, object : android.location.LocationListener {
                        override fun onLocationChanged(location: android.location.Location) {
                            pending = location.latitude to location.longitude
                            showMap = true
                            mapView?.setPoint(location.latitude, location.longitude)
                            error = null
                        }
                    }, Looper.getMainLooper())
                }
            } catch (_: SecurityException) {
                error = "Standortberechtigung fehlt."
            }
        }
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true) requestLocation() else error = "Ohne Berechtigung kannst du die Nadel manuell setzen."
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) requestLocation()
                else launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            },
            modifier = Modifier.fillMaxWidth().testTag("onboarding_use_gps"),
        ) { Text("Aktuellen Standort per GPS verwenden") }
        if (!showMap) {
            OutlinedButton(onClick = { showMap = true }, modifier = Modifier.fillMaxWidth().testTag("onboarding_open_map")) {
                Text("Oder Punkt auf Karte wählen")
            }
        } else {
            AndroidView(
                factory = { viewContext ->
                    HomeMapView(viewContext).apply {
                        onPointSelected = { lat, lon -> pending = lat to lon }
                        val initial = pending ?: (latitude.toDoubleOrNull()?.let { lat ->
                            longitude.toDoubleOrNull()?.let { lon -> lat to lon }
                        })
                        setInitialPoint(initial?.first, initial?.second)
                        mapView = this
                    }
                },
                modifier = Modifier.fillMaxWidth().height(220.dp).testTag("onboarding_map"),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { mapView?.changeZoom(1) }) { Text("+") }
                OutlinedButton(onClick = { mapView?.changeZoom(-1) }) { Text("−") }
                OutlinedButton(onClick = { mapView?.retryTiles() }) { Text("Karte neu laden") }
            }
            Text("Nadel verschieben oder auf die Karte tippen. Karte: OpenStreetMap.")
        }
        DisposableEffect(showMap) {
            onDispose {
                mapView?.release()
                mapView = null
            }
        }
        Button(onClick = { pending?.let { currentOnPointChange(it.first, it.second) } }, enabled = pending != null, modifier = Modifier.fillMaxWidth().testTag("onboarding_confirm_home")) {
            Text("Heimatpunkt übernehmen")
        }
        pending?.let { Text("Ausgewählt: ${it.first}, ${it.second}") }
        if (latitude.isNotBlank() && longitude.isNotBlank()) {
            Text("Heimatpunkt: ${latitude.take(10)}, ${longitude.take(10)} · Radius 500 m")
        }
        error?.let { Text(it) }
    }
}

