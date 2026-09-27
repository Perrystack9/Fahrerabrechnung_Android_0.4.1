package de.fahrerabrechnung.core.card

/**
 * ISO7816 command APDU representation.
 * Sensitive card payloads must not be logged.
 */
data class ApduCommand(
    val cla: Byte,
    val ins: Byte,
    val p1: Byte,
    val p2: Byte,
    val data: ByteArray = byteArrayOf(),
    val expectedLength: Int? = null
) {
    fun encode(): ByteArray {
        val header = byteArrayOf(cla, ins, p1, p2)
        val body = if (data.isNotEmpty()) {
            byteArrayOf(data.size.toByte()) + data
        } else {
            byteArrayOf()
        }
        val le = expectedLength?.let { byteArrayOf(it.toByte()) } ?: byteArrayOf()
        return header + body + le
    }
}
