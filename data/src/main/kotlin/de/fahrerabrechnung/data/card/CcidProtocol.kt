package de.fahrerabrechnung.data.card

/** USB CCID 1.1 section 4 bulk messages. Length is little endian, header is ten bytes. */
internal object CcidProtocol {
    const val HEADER_SIZE = 10
    const val MAX_PAYLOAD = 65544
    const val POWER_ON = 0x62
    const val POWER_OFF = 0x63
    const val SLOT_STATUS = 0x65
    const val XFR_BLOCK = 0x6F
    const val DATA_BLOCK = 0x80
    const val STATUS = 0x81

    data class Response(val type: Int, val slot: Int, val sequence: Int, val status: Int,
                        val error: Int, val chain: Int, val data: ByteArray) {
        val cardPresent: Boolean get() = (status and 3) != 2
        val timeExtension: Boolean get() = (status ushr 6) == 2
    }

    fun command(type: Int, slot: Int, sequence: Int, payload: ByteArray = byteArrayOf()): ByteArray {
        require(type in listOf(POWER_ON, POWER_OFF, SLOT_STATUS, XFR_BLOCK))
        require(slot in 0..255 && sequence in 0..255 && payload.size <= MAX_PAYLOAD)
        val n = payload.size
        return byteArrayOf(type.toByte(), n.toByte(), (n ushr 8).toByte(),
            (n ushr 16).toByte(), (n ushr 24).toByte(), slot.toByte(), sequence.toByte(),
            0, 0, 0) + payload
    }

    fun expectedLength(header: ByteArray): Int {
        if (header.size < HEADER_SIZE) throw ReaderException(ReaderFailure.MALFORMED_RESPONSE)
        val n = (header[1].toLong() and 255) or ((header[2].toLong() and 255) shl 8) or
            ((header[3].toLong() and 255) shl 16) or ((header[4].toLong() and 255) shl 24)
        if (n > MAX_PAYLOAD) throw ReaderException(ReaderFailure.MALFORMED_RESPONSE)
        return HEADER_SIZE + n.toInt()
    }

    fun response(bytes: ByteArray, type: Int, slot: Int, sequence: Int): Response {
        val total = expectedLength(bytes)
        if (bytes.size != total || (bytes[0].toInt() and 255) != type ||
            (bytes[5].toInt() and 255) != slot || (bytes[6].toInt() and 255) != sequence)
            throw ReaderException(ReaderFailure.MALFORMED_RESPONSE)
        val status = bytes[7].toInt() and 255
        val error = bytes[8].toInt() and 255
        if ((status and 3) == 2) throw ReaderException(ReaderFailure.CARD_ABSENT)
        if ((status ushr 6) == 1) throw ReaderException(
            when (error) {
                0xFE -> ReaderFailure.TIMEOUT // ICC_MUTE (CCID 1.1 table 6.2-2)
                else -> ReaderFailure.IO
            })
        if ((status ushr 6) == 3) throw ReaderException(ReaderFailure.MALFORMED_RESPONSE)
        return Response(type, slot, sequence, status, error, bytes[9].toInt() and 255,
            bytes.copyOfRange(HEADER_SIZE, total))
    }
}
