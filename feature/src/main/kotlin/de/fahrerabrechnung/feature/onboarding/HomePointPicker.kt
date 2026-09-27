package de.fahrerabrechnung.feature.onboarding

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.Locale

@Composable
internal fun HomePointPicker(latitude: String, longitude: String, onPointChange: (Double, Double) -> Unit) {
    val context = LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    val requestLocation: () -> Unit = {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            error = "GPS ist ausgeschaltet. Du kannst die Nadel auf der Karte selbst setzen."
        } else {
            try {
                if (Build.VERSION.SDK_INT >= 30) {
                    manager.getCurrentLocation(LocationManager.GPS_PROVIDER, null, ContextCompat.getMainExecutor(context)) { location ->
                        if (location == null) error = "Kein Standort gefunden. Bitte versuche es erneut oder setze die Nadel selbst."
                        else {
                            onPointChange(location.latitude, location.longitude)
                            webView?.evaluateJavascript("setPoint(${location.latitude},${location.longitude})", null)
                            error = null
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    manager.requestSingleUpdate(LocationManager.GPS_PROVIDER, object : android.location.LocationListener {
                        override fun onLocationChanged(location: android.location.Location) {
                            onPointChange(location.latitude, location.longitude)
                            webView?.evaluateJavascript("setPoint(${location.latitude},${location.longitude})", null)
                            error = null
                        }
                    }, Looper.getMainLooper())
                }
            } catch (_: SecurityException) {
                error = "Standortberechtigung fehlt."
            }
        }
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { allowed ->
        if (allowed) requestLocation() else error = "Ohne Berechtigung kannst du die Nadel manuell setzen."
    }
    val bridge = remember(onPointChange) {
        HomePointBridge { lat, lon -> onPointChange(lat, lon) }
    }
    Column {
        AndroidView(
            factory = { viewContext ->
                WebView(viewContext).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = false
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    settings.setSupportMultipleWindows(false)
                    isHorizontalScrollBarEnabled = false
                    isVerticalScrollBarEnabled = false
                    settings.userAgentString = settings.userAgentString + " FahrerTagebuch/0.4.1 (home point picker)"
                    addJavascriptInterface(bridge, "HomePoint")
                    loadDataWithBaseURL("https://leafletjs.com/", mapHtml(latitude, longitude), "text/html", "UTF-8", null)
                    webView = this
                }
            },
            modifier = Modifier.fillMaxWidth().height(220.dp),
        )
        DisposableEffect(Unit) {
            onDispose {
                webView?.removeJavascriptInterface("HomePoint")
                webView?.destroy()
                webView = null
            }
        }
        OutlinedButton(onClick = {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) requestLocation()
            else launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }) { Text("Aktuellen Standort verwenden") }
        Text("Nadel antippen und verschieben oder auf die Karte tippen. Karte: OpenStreetMap.")
        if (latitude.isNotBlank() && longitude.isNotBlank()) {
            Text("Heimatpunkt: ${latitude.take(10)}, ${longitude.take(10)} · Radius 500 m")
        }
        error?.let { Text(it) }
    }
}

private class HomePointBridge(val update: (Double, Double) -> Unit) {
    @JavascriptInterface
    fun setPoint(latitude: Double, longitude: Double) {
        if (latitude.isFinite() && longitude.isFinite() && latitude in -90.0..90.0 && longitude in -180.0..180.0) {
            android.os.Handler(Looper.getMainLooper()).post { update(latitude, longitude) }
        }
    }
}

private fun mapHtml(latitude: String, longitude: String): String {
    val lat = latitude.toDoubleOrNull()?.takeIf { it.isFinite() && it in -90.0..90.0 } ?: 48.137
    val lon = longitude.toDoubleOrNull()?.takeIf { it.isFinite() && it in -180.0..180.0 } ?: 11.575
    val hasPoint = latitude.toDoubleOrNull() != null && longitude.toDoubleOrNull() != null
    return """
        <!doctype html><html><head><meta name="viewport" content="width=device-width, initial-scale=1">
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css">
        <style>html,body,#map{width:100%;height:100%;margin:0}</style></head><body><div id="map"></div>
        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
        <script>
          var map=L.map('map').setView([${String.format(Locale.US, "%.7f", lat)},${String.format(Locale.US, "%.7f", lon)}],${if (hasPoint) 16 else 6});
          L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png',
            {maxZoom:19,attribution:'&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'}).addTo(map);
          var marker=null;
          function setPoint(lat,lon) {
            if (!marker) {
              marker=L.marker([lat,lon],{draggable:true}).addTo(map);
              marker.on('dragend',function(e){var p=e.target.getLatLng();HomePoint.setPoint(p.lat,p.lng)});
            } else marker.setLatLng([lat,lon]);
            map.setView([lat,lon],16);
          }
          map.on('click',function(e){setPoint(e.latlng.lat,e.latlng.lng);HomePoint.setPoint(e.latlng.lat,e.latlng.lng)});
          ${if (hasPoint) "setPoint($lat,$lon);" else ""}
        </script></body></html>
    """.trimIndent()
}
