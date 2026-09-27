package de.fahrerabrechnung.feature.onboarding

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import java.util.Locale

@SuppressLint("JavascriptInterface") // Lint sees remember<T>; HomePointBridge.setPoint is annotated and validates both coordinates.
@Composable
internal fun HomePointPicker(latitude: String, longitude: String, onPointChange: (Double, Double) -> Unit) {
    val context = LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }
    var webView by remember { mutableStateOf<WebView?>(null) }
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
                            webView?.evaluateJavascript("setPoint(${location.latitude},${location.longitude})", null)
                            error = null
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    manager.requestSingleUpdate(LocationManager.GPS_PROVIDER, object : android.location.LocationListener {
                        override fun onLocationChanged(location: android.location.Location) {
                            pending = location.latitude to location.longitude
                            showMap = true
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
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true) requestLocation() else error = "Ohne Berechtigung kannst du die Nadel manuell setzen."
    }
    val bridge = remember {
        HomePointBridge { lat, lon -> pending = lat to lon }
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
                        loadDataWithBaseURL(
                            "https://leafletjs.com/",
                            mapHtml(pending?.first?.toString() ?: latitude, pending?.second?.toString() ?: longitude),
                            "text/html", "UTF-8", null,
                        )
                        webView = this
                    }
                },
                modifier = Modifier.fillMaxWidth().height(220.dp).testTag("onboarding_map"),
            )
            Text("Nadel verschieben oder auf die Karte tippen. Karte: OpenStreetMap.")
        }
        DisposableEffect(Unit) {
            onDispose {
                webView?.removeJavascriptInterface("HomePoint")
                webView?.destroy()
                webView = null
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
        <style>html,body,#map{width:100%;height:100%;margin:0}#map{position:relative;background:#e8f0f2}
        #status{position:absolute;top:44%;left:5%;width:90%;text-align:center;color:#17334b;font:16px sans-serif;z-index:1000}</style>
        </head><body><div id="map"><div id="status">Karte wird geladen …</div></div>
        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" onerror="loadBackup()"></script>
        <script>function loadBackup(){var s=document.createElement("script");s.src="https://cdn.jsdelivr.net/npm/leaflet@1.9.4/dist/leaflet.js";s.onload=initMap;s.onerror=function(){document.getElementById("status").textContent="Karten-Skript nicht erreichbar. Prüfe die Internetverbindung oder nutze GPS."};document.body.appendChild(s)}</script>
        <script>
          var statusEl=document.getElementById('status');
          function initMap(){
          if (!window.L) {
            statusEl.textContent='Karte nicht verfügbar. Prüfe die Internetverbindung oder nutze GPS.';
          } else {
          var map=L.map('map').setView([${String.format(Locale.US, "%.7f", lat)},${String.format(Locale.US, "%.7f", lon)}],${if (hasPoint) 16 else 6});
          L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png',
            {maxZoom:19,attribution:'&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'}).addTo(map);
          var tileCount=0, failedTiles=0;
          map.on('tileload',function(){tileCount++;statusEl.style.display='none'});
          map.on('tileerror',function(){failedTiles++;statusEl.style.display='block';statusEl.textContent='Kartenkacheln nicht erreichbar. Prüfe die Internetverbindung oder nutze GPS.'});
          setTimeout(function(){if(!tileCount&&!failedTiles){statusEl.style.display='block';statusEl.textContent='Kartenkacheln laden nicht. Prüfe die Internetverbindung oder nutze GPS.'}},8000);
          map.whenReady(function(){setTimeout(function(){map.invalidateSize();},100)});
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
          }
          }
          if(window.L)initMap();
        </script></body></html>
    """.trimIndent()
}
