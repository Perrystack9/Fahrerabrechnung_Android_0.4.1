package de.fahrerabrechnung.data.card

import android.app.PendingIntent
import android.content.Context
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/** USB interface class 0x0B. Permission result is delivered to the caller's explicit receiver. */
class AndroidCcidTransport(private val manager: UsbManager) : CardReaderTransport {
    constructor(context: Context) : this(context.getSystemService(Context.USB_SERVICE) as UsbManager)

    data class DeviceInfo(val vendorId: Int, val productId: Int, val slots: Int, val permission: Boolean)
    private data class Candidate(val device: UsbDevice, val iface: UsbInterface,
                                 val input: UsbEndpoint, val output: UsbEndpoint)
    private var active: Candidate? = null
    private var connection: UsbDeviceConnection? = null
    private var sequence = 0
    private var powered = false
    private var maxMessage = CcidProtocol.MAX_PAYLOAD + CcidProtocol.HEADER_SIZE
    private val timeoutMs = 5000
    private var deniedDeviceId: Int? = null

    private data class Descriptor(val maxSlots: Int, val maxMessage: Int, val features: Long)
    private fun descriptor(raw: ByteArray): Descriptor? {
        var i = 0
        while (i + 2 <= raw.size) {
            val length = raw[i].toInt() and 255
            if (length < 2 || i + length > raw.size) return null
            if (raw[i + 1].toInt() and 255 == 0x21 && length >= 54) {
                fun le32(at: Int) = (0..3).fold(0L) { value, shift ->
                    value or ((raw[i + at + shift].toLong() and 255) shl (8 * shift))
                }
                val message = le32(44)
                if (message !in 10..(CcidProtocol.MAX_PAYLOAD + 10).toLong()) return null
                return Descriptor((raw[i + 4].toInt() and 255) + 1, message.toInt(), le32(40))
            }
            i += length
        }
        return null
    }

    private fun candidates(): List<Candidate> = manager.deviceList.values.flatMap { device ->
        (0 until device.interfaceCount).mapNotNull { i ->
            val iface = device.getInterface(i)
            if (iface.interfaceClass != 0x0B) return@mapNotNull null
            val endpoints = (0 until iface.endpointCount).map(iface::getEndpoint)
            val input = endpoints.firstOrNull { it.type == UsbConstants.USB_ENDPOINT_XFER_BULK && it.direction == UsbConstants.USB_DIR_IN }
            val output = endpoints.firstOrNull { it.type == UsbConstants.USB_ENDPOINT_XFER_BULK && it.direction == UsbConstants.USB_DIR_OUT }
            if (input == null || output == null) null else Candidate(device, iface, input, output)
        }
    }

    fun devices(): List<DeviceInfo> = candidates().map {
        DeviceInfo(it.device.vendorId, it.device.productId, 1, manager.hasPermission(it.device))
    }

    fun requestPermission(device: UsbDevice, resultIntent: PendingIntent) {
        require(candidates().any { it.device.deviceId == device.deviceId })
        deniedDeviceId = null
        manager.requestPermission(device, resultIntent)
    }

    /** Call from the receiver for UsbManager.EXTRA_PERMISSION_GRANTED. */
    fun onPermissionResult(device: UsbDevice, granted: Boolean) {
        deniedDeviceId = if (granted) null else device.deviceId
    }

    override suspend fun availability(): SourceAvailability = when {
        candidates().isEmpty() -> if (manager.deviceList.values.any { device ->
            (0 until device.interfaceCount).any { device.getInterface(it).interfaceClass == 0x0B }
        }) SourceAvailability.UNKNOWN else SourceAvailability.UNAVAILABLE
        candidates().any { manager.hasPermission(it.device) } -> SourceAvailability.AVAILABLE
        else -> SourceAvailability.PERMISSION_REQUIRED
    }

    override suspend fun open(): ReaderState = withContext(Dispatchers.IO) {
        close()
        val candidate = candidates().firstOrNull() ?: return@withContext ReaderState.Absent
        if (!manager.hasPermission(candidate.device)) return@withContext
            if (deniedDeviceId == candidate.device.deviceId) ReaderState.PermissionDenied else ReaderState.PermissionRequired
        val conn = manager.openDevice(candidate.device) ?: return@withContext ReaderState.Disconnected
        if (!conn.claimInterface(candidate.iface, true)) {
            conn.close()
            throw ReaderException(ReaderFailure.UNSUPPORTED_READER)
        }
        val properties = descriptor(conn.rawDescriptors)
        // The reader must expose short or extended APDU-level exchange; TPDU/character
        // exchange requires host-side T=0/T=1 handling and is deliberately rejected.
        if (properties == null || (properties.features and 0x00060000L) == 0L) {
            conn.releaseInterface(candidate.iface)
            conn.close()
            throw ReaderException(ReaderFailure.UNSUPPORTED_READER)
        }
        active = candidate
        connection = conn
        sequence = 0
        powered = false
        maxMessage = properties.maxMessage
        ReaderState.Open
    }

    private fun checked(): Pair<Candidate, UsbDeviceConnection> {
        val candidate = active ?: throw ReaderException(ReaderFailure.NO_READER)
        val conn = connection ?: throw ReaderException(ReaderFailure.DISCONNECTED)
        if (manager.deviceList.values.none { it.deviceId == candidate.device.deviceId }) {
            close()
            throw ReaderException(ReaderFailure.DISCONNECTED)
        }
        return candidate to conn
    }

    private fun transfer(type: Int, expected: Int, payload: ByteArray = byteArrayOf()): CcidProtocol.Response {
        val (candidate, conn) = checked()
        val seq = sequence++ and 255
        val request = CcidProtocol.command(type, 0, seq, payload)
        if (request.size > maxMessage) throw ReaderException(ReaderFailure.UNSUPPORTED_READER)
        var sent = 0
        while (sent < request.size) {
            val n = conn.bulkTransfer(candidate.output, request, sent, request.size - sent, timeoutMs)
            if (n <= 0) throw ReaderException(if (n == 0) ReaderFailure.TIMEOUT else ReaderFailure.IO)
            sent += n
        }
        var extensions = 0
        while (true) {
            val buffer = ByteArrayOutputStream()
            var target: Int? = null
            while (target == null || buffer.size() < target) {
                val chunk = ByteArray(minOf(4096, maxMessage - buffer.size()))
                if (chunk.isEmpty()) throw ReaderException(ReaderFailure.MALFORMED_RESPONSE)
                val n = conn.bulkTransfer(candidate.input, chunk, chunk.size, timeoutMs)
                if (n <= 0) throw ReaderException(if (n == 0) ReaderFailure.TIMEOUT else ReaderFailure.IO)
                buffer.write(chunk, 0, n)
                if (buffer.size() >= CcidProtocol.HEADER_SIZE && target == null) {
                    target = CcidProtocol.expectedLength(buffer.toByteArray())
                    if (target > maxMessage) throw ReaderException(ReaderFailure.MALFORMED_RESPONSE)
                }
                if (target != null && buffer.size() > target) throw ReaderException(ReaderFailure.MALFORMED_RESPONSE)
            }
            val response = CcidProtocol.response(buffer.toByteArray(), expected, 0, seq)
            if (!response.timeExtension) return response
            if (++extensions > 8) throw ReaderException(ReaderFailure.TIMEOUT)
        }
    }

    suspend fun slotStatus(): ReaderState = withContext(Dispatchers.IO) {
        try {
            transfer(CcidProtocol.SLOT_STATUS, CcidProtocol.STATUS)
            ReaderState.CardPresent
        } catch (e: ReaderException) {
            if (e.failure == ReaderFailure.CARD_ABSENT) ReaderState.Empty else throw e
        }
    }

    override suspend fun powerOn(): ByteArray = withContext(Dispatchers.IO) {
        transfer(CcidProtocol.POWER_ON, CcidProtocol.DATA_BLOCK).data.also {
            AtrDecoder.parse(it)
            powered = true
        }
    }
    override suspend fun exchange(apdu: ByteArray): ByteArray = withContext(Dispatchers.IO) {
        require(apdu.size in 4..65535)
        try {
            transfer(CcidProtocol.XFR_BLOCK, CcidProtocol.DATA_BLOCK, apdu).also {
                if (it.chain != 0) throw ReaderException(ReaderFailure.UNSUPPORTED_READER)
            }.data
        } catch (e: ReaderException) {
            if (e.failure == ReaderFailure.CARD_ABSENT && powered)
                throw ReaderException(ReaderFailure.CARD_REMOVED)
            throw e
        }
    }
    override suspend fun powerOff(): Unit = withContext(Dispatchers.IO) {
        transfer(CcidProtocol.POWER_OFF, CcidProtocol.STATUS)
        powered = false
        Unit
    }
    override fun close() {
        val candidate = active
        val conn = connection
        active = null
        connection = null
        powered = false
        if (candidate != null && conn != null) runCatching { conn.releaseInterface(candidate.iface) }
        conn?.close()
    }
}
