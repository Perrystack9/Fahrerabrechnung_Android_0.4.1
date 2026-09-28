package de.fahrerabrechnung.feature.onboarding

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import java.net.HttpURLConnection
import java.net.URL
import java.io.File
import java.io.IOException
import java.util.LinkedHashMap
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sinh
import kotlin.math.tan

/** A small, foreground-only home-point picker. It never reads device location itself. */
internal class HomeMapView(context: Context) : View(context) {
    var onPointSelected: ((Double, Double) -> Unit)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tileSize = 256
    private val executor = Executors.newFixedThreadPool(3) { task ->
        Thread(task, "home-map-tile").apply { isDaemon = true }
    }
    private val cache = object : LinkedHashMap<String, Bitmap>(64, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Bitmap>): Boolean = size > 64
    }
    private val loading = mutableSetOf<String>()
    private val failedKeys = mutableSetOf<String>()
    private var failed = false
    private var failureReason: String? = null
    private var hasVisibleTiles = false
    private var zoom = 6
    private var centerLat = 48.137
    private var centerLon = 11.575
    private var selected: Pair<Double, Double>? = null
    private var downX = 0f
    private var downY = 0f
    private var downCenterX = 0.0
    private var downCenterY = 0.0
    private var draggingPin = false
    private var moved = false
    private var released = false

    fun setInitialPoint(latitude: Double?, longitude: Double?) {
        if (latitude != null && longitude != null) setPoint(latitude, longitude)
    }

    fun setPoint(latitude: Double, longitude: Double) {
        if (!latitude.isFinite() || !longitude.isFinite() || latitude !in -90.0..90.0 || longitude !in -180.0..180.0) return
        centerLat = latitude
        centerLon = longitude
        selected = latitude to longitude
        zoom = 16
        invalidate()
    }

    fun changeZoom(delta: Int) {
        zoom = (zoom + delta).coerceIn(2, 18)
        invalidate()
    }

    fun retryTiles() {
        failedKeys.clear()
        failed = false
        failureReason = null
        invalidate()
    }

    fun release() {
        released = true
        executor.shutdownNow()
        cache.clear()
        loading.clear()
        failedKeys.clear()
    }

    private fun world(latitude: Double, longitude: Double): Pair<Double, Double> {
        val scale = tileSize * 2.0.pow(zoom)
        val lat = latitude.coerceIn(-85.0511, 85.0511) * PI / 180
        return ((longitude + 180) / 360 * scale) to
            ((1 - ln(tan(lat) + 1 / cos(lat)) / PI) / 2 * scale)
    }

    private fun geo(x: Double, y: Double): Pair<Double, Double> {
        val scale = tileSize * 2.0.pow(zoom)
        val latitude = atan(sinh(PI * (1 - 2 * y / scale))) * 180 / PI
        val longitude = x / scale * 360 - 180
        return latitude to (((longitude + 180) % 360 + 360) % 360 - 180)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.rgb(232, 240, 242))
        val (cx, cy) = world(centerLat, centerLon)
        val left = cx - width / 2.0
        val top = cy - height / 2.0
        val n = 1 shl zoom
        var visible = 0
        for (x in floor(left / tileSize).toInt()..floor((left + width) / tileSize).toInt()) {
            for (y in floor(top / tileSize).toInt()..floor((top + height) / tileSize).toInt()) {
                if (y !in 0 until n) continue
                val wrappedX = ((x % n) + n) % n
                val key = "$zoom/$wrappedX/$y"
                val tileLeft = (x * tileSize - left).toFloat()
                val tileTop = (y * tileSize - top).toFloat()
                val bounds = RectF(tileLeft, tileTop, tileLeft + tileSize, tileTop + tileSize)
                val bitmap = cache[key]
                if (bitmap != null) {
                    paint.color = Color.WHITE
                    canvas.drawBitmap(bitmap, null, bounds, paint)
                    visible++
                } else {
                    paint.color = Color.rgb(218, 230, 234)
                    canvas.drawRect(bounds, paint)
                    paint.color = Color.rgb(193, 209, 215)
                    paint.style = Paint.Style.STROKE
                    canvas.drawRect(bounds, paint)
                    paint.style = Paint.Style.FILL
                    requestTile(key, zoom, wrappedX, y)
                }
            }
        }
        hasVisibleTiles = visible > 0
        selected?.let { (lat, lon) ->
            val (px, py) = world(lat, lon)
            val x = (width / 2 + px - cx).toFloat()
            val y = (height / 2 + py - cy).toFloat()
            val radius = 12f * resources.displayMetrics.density
            val pin = Path().apply {
                moveTo(x, y)
                lineTo(x - radius * 0.8f, y - radius * 1.6f)
                quadTo(x - radius * 1.3f, y - radius * 2.8f, x, y - radius * 2.8f)
                quadTo(x + radius * 1.3f, y - radius * 2.8f, x + radius * 0.8f, y - radius * 1.6f)
                close()
            }
            paint.color = Color.WHITE
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f * resources.displayMetrics.density
            canvas.drawPath(pin, paint)
            paint.style = Paint.Style.FILL
            paint.color = Color.rgb(211, 48, 48)
            canvas.drawPath(pin, paint)
        }
        if (visible == 0) {
            paint.color = Color.rgb(15, 45, 74)
            paint.textSize = 14f * resources.displayMetrics.scaledDensity
            val message = if (failed) "Karte nicht erreichbar: ${failureReason ?: "Netzwerkfehler"}"
                else "Karte wird geladen …"
            canvas.drawText(message, 12f * resources.displayMetrics.density, height - 12f * resources.displayMetrics.density, paint)
        }
        paint.color = Color.rgb(15, 45, 74)
        paint.textSize = 10f * resources.displayMetrics.scaledDensity
        canvas.drawText("© OpenStreetMap", 8f * resources.displayMetrics.density, 15f * resources.displayMetrics.density, paint)
    }

    private fun requestTile(key: String, level: Int, x: Int, y: Int) {
        if (released || key in failedKeys || !loading.add(key)) return
        executor.execute {
            var bitmap: Bitmap? = null
            var reason: String? = null
            try {
                val directory = File(context.cacheDir, "home-map-tiles")
                val file = File(directory, "$level-$x-$y.png")
                val sevenDays = 7L * 24 * 60 * 60 * 1000
                if (file.isFile && System.currentTimeMillis() - file.lastModified() < sevenDays) {
                    bitmap = BitmapFactory.decodeFile(file.absolutePath)
                }
                if (bitmap == null) {
                    val connection = URL("https://tile.openstreetmap.org/$level/$x/$y.png").openConnection() as HttpURLConnection
                    connection.setRequestProperty("User-Agent", "FahrerTagebuch/0.4.1 (Android home-point picker)")
                    connection.connectTimeout = 6000
                    connection.readTimeout = 6000
                    try {
                        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                            throw IOException("HTTP ${connection.responseCode}")
                        }
                        val bytes = connection.inputStream.use { it.readBytes() }
                        bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        if (bitmap != null && (directory.isDirectory || directory.mkdirs())) {
                            file.writeBytes(bytes)
                        }
                    } finally {
                        connection.disconnect()
                    }
                }
            } catch (e: Exception) {
                reason = when (e) {
                    is IOException -> e.message?.takeIf { it.startsWith("HTTP ") } ?: "Netzwerk oder DNS"
                    else -> "Netzwerk oder DNS"
                }
            }
            val loaded = bitmap
            val error = reason
            post {
                if (!released) {
                    loading.remove(key)
                    if (loaded != null) cache[key] = loaded
                    else {
                        failedKeys.add(key)
                        failed = true
                        failureReason = error ?: "Bilddaten ungültig"
                    }
                    invalidate()
                }
            }
        }
    }

    private fun select(screenX: Float, screenY: Float) {
        if (!hasVisibleTiles) return
        val (cx, cy) = world(centerLat, centerLon)
        val point = geo(cx + screenX - width / 2.0, cy + screenY - height / 2.0)
        selected = point
        onPointSelected?.invoke(point.first, point.second)
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                downX = event.x
                downY = event.y
                val (cx, cy) = world(centerLat, centerLon)
                downCenterX = cx
                downCenterY = cy
                draggingPin = selected?.let { (lat, lon) ->
                    val (px, py) = world(lat, lon)
                    val pinX = width / 2 + px - cx
                    val pinY = height / 2 + py - cy
                    kotlin.math.abs(event.x - pinX) < 35 * resources.displayMetrics.density &&
                        event.y in (pinY - 45 * resources.displayMetrics.density)..(pinY + 15 * resources.displayMetrics.density)
                } ?: false
                moved = false
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (kotlin.math.abs(event.x - downX) + kotlin.math.abs(event.y - downY) > 6 * resources.displayMetrics.density) moved = true
                if (moved) {
                    if (draggingPin) select(event.x, event.y)
                    else {
                        val center = geo(downCenterX - (event.x - downX), downCenterY - (event.y - downY))
                        centerLat = center.first
                        centerLon = center.second
                        invalidate()
                    }
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (!moved) select(event.x, event.y)
                parent?.requestDisallowInterceptTouchEvent(false)
                performClick()
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
