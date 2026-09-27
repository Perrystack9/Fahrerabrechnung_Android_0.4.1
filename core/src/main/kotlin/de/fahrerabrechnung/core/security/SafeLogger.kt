package de.fahrerabrechnung.core.security

import android.util.Log

/**
 * Central logging boundary. Never pass names, complete card IDs/VINs, card/tachograph position coordinates,
 * APDUs or driver-card binary payloads to this API.
 */
object SafeLogger {
    fun info(tag: String, message: String) = Log.i(tag, message)
    fun warn(tag: String, message: String, throwable: Throwable? = null) = Log.w(tag, message, throwable)
    fun error(tag: String, message: String, throwable: Throwable? = null) = Log.e(tag, message, throwable)
}
