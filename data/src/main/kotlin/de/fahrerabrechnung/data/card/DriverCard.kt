package de.fahrerabrechnung.data.card

import java.nio.charset.Charset
import java.time.Instant

enum class CardGeneration { GEN1, GEN2, GEN2V2, UNKNOWN, UNSUPPORTED }
enum class CardCapability { BASIC_IDENTITY, G2_FILES, V2_FILES }
val CardGeneration.capabilities: Set<CardCapability> get() = when (this) {
    CardGeneration.GEN1 -> setOf(CardCapability.BASIC_IDENTITY)
    CardGeneration.GEN2 -> setOf(CardCapability.BASIC_IDENTITY, CardCapability.G2_FILES)
    CardGeneration.GEN2V2 -> CardCapability.entries.toSet()
    else -> emptySet()
}
enum class CardRecognition { DRIVER, OTHER_CARD, UNKNOWN_CARD, INCONSISTENT }
data class DriverIdentity(val surname: String, val givenNames: String, val birthDate: Instant,
                          val language: String)
data class CardIdentity(val number: String, val issuingState: Int, val issuingAuthority: String,
                        val issued: Instant, val validFrom: Instant, val expires: Instant)
data class DriverCard(val generation: CardGeneration, val card: CardIdentity, val holder: DriverIdentity)
/** The activity ring is raw card data; day records are decoded in a later stage. */
class CardActivityFile(val oldestDayOffset: Int, val newestDayOffset: Int, bytes: ByteArray) {
    val dailyRecords: ByteArray = bytes.copyOf()
}
data class CardReadResult(val recognition: CardRecognition, val generation: CardGeneration,
                          val card: DriverCard? = null, val activity: CardActivityFile? = null)

data class CardFile(val id: Int, val name: String, val generation: CardGeneration,
                    val required: Boolean, val expectedLength: Int, val parser: String)

/** EU 2016/799 Annex IC Appendix 2, section 4.2. */
object DriverCardFiles {
    val gen1Aid = byteArrayOf(0xFF.toByte(), 0x54, 0x41, 0x43, 0x48, 0x4F)
    val gen2Aid = byteArrayOf(0xFF.toByte(), 0x53, 0x4D, 0x52, 0x44, 0x54)
    const val MF = 0x3F00
    const val APPLICATION = 0x0501
    const val IDENTIFICATION = 0x0520
    const val DRIVER_ACTIVITY = 0x0504
    const val APPLICATION_V2 = 0x0525
    val files = listOf(
        CardFile(APPLICATION, "Application_Identification", CardGeneration.GEN1, true, 10, "ApplicationIdG1"),
        CardFile(IDENTIFICATION, "Identification", CardGeneration.GEN1, true, 143, "DriverIdentity"),
        CardFile(APPLICATION, "Application_Identification", CardGeneration.GEN2, true, 15, "ApplicationIdG2"),
        CardFile(IDENTIFICATION, "Identification", CardGeneration.GEN2, true, 143, "DriverIdentity"),
        CardFile(APPLICATION_V2, "Application_Identification_V2", CardGeneration.GEN2V2, true, 10, "ApplicationIdV2")
    )
}

object DriverCardDecoder {
    fun generation(g1: ByteArray?, g2: ByteArray?): Pair<CardRecognition, CardGeneration> {
        if (g2 != null) {
            if (g2.size != 15) return CardRecognition.INCONSISTENT to CardGeneration.UNKNOWN
            if (g2[0].toInt() and 255 != 1) return CardRecognition.OTHER_CARD to CardGeneration.UNSUPPORTED
            return when (g2[1].toInt() and 255 to (g2[2].toInt() and 255)) {
                1 to 0 -> CardRecognition.DRIVER to CardGeneration.GEN2
                1 to 1 -> CardRecognition.DRIVER to CardGeneration.GEN2V2
                else -> CardRecognition.UNKNOWN_CARD to CardGeneration.UNKNOWN
            }
        }
        if (g1 != null) {
            if (g1.size != 10) return CardRecognition.INCONSISTENT to CardGeneration.UNKNOWN
            if (g1[0].toInt() and 255 != 1) return CardRecognition.OTHER_CARD to CardGeneration.UNSUPPORTED
            return CardRecognition.DRIVER to CardGeneration.GEN1
        }
        return CardRecognition.UNKNOWN_CARD to CardGeneration.UNKNOWN
    }

    /** Appendix 2: activityStructureLength is the two-byte field after card type,
     * structure version, event count and fault count in EF Application_Identification. */
    fun activityLength(application: ByteArray, generation: CardGeneration): Int {
        val expected = if (generation == CardGeneration.GEN1) 10 else 15
        if (generation !in setOf(CardGeneration.GEN1, CardGeneration.GEN2, CardGeneration.GEN2V2) ||
            application.size != expected) throw ParseError(0, "Application_Identification", "invalid length")
        val length = (application[5].toInt() and 255) shl 8 or (application[6].toInt() and 255)
        // READ BINARY uses a 15-bit offset; do not silently truncate larger card files.
        if (length !in 12..32764) throw ParseError(5, "activityStructureLength", "unsupported length")
        return length
    }

    /** EF Driver_Activity_Data has two two-byte offsets into its cyclic data area. */
    fun activity(bytes: ByteArray, expectedRingLength: Int): CardActivityFile {
        if (bytes.size != expectedRingLength + 4) throw ParseError(0, "Driver_Activity_Data", "invalid length")
        val r = BinaryReader(bytes)
        val oldest = r.u16be("activityPointerOldestDayRecord")
        val newest = r.u16be("activityPointerNewestRecord")
        if (oldest >= expectedRingLength || newest >= expectedRingLength)
            throw ParseError(0, "Driver_Activity_Data", "pointer outside activity ring")
        return CardActivityFile(oldest, newest, r.slice(expectedRingLength, "activityDailyRecords"))
    }

    /** EF Identification: 65-byte CardIdentification followed by 78-byte holder record. */
    fun identity(bytes: ByteArray, generation: CardGeneration): DriverCard {
        if (generation !in setOf(CardGeneration.GEN1, CardGeneration.GEN2, CardGeneration.GEN2V2))
            throw ParseError(0, "generation", "unsupported")
        if (bytes.size != 143) throw ParseError(0, "Identification", "expected 143 bytes")
        val r = BinaryReader(bytes)
        val state = r.u8("issuingState")
        val number = r.text(16, Charsets.ISO_8859_1, "cardNumber")
        // Names carry a code page byte followed by 35 encoded characters.
        val authority = codedName(r, "issuingAuthority")
        val issued = r.epochSeconds("issueDate")
        val start = r.epochSeconds("validFrom")
        val expiry = r.epochSeconds("expiry")
        val surname = codedName(r, "surname")
        val given = codedName(r, "givenNames")
        val birth = r.epochSeconds("birthDate")
        val language = r.text(2, Charsets.US_ASCII, "language")
        if (r.remaining != 0 || number.isBlank() || surname.isBlank() || expiry <= start)
            throw ParseError(r.offset, "Identification", "invalid identity or validity")
        return DriverCard(generation, CardIdentity(number, state, authority, issued, start, expiry),
            DriverIdentity(surname, given, birth, language))
    }

    private fun codedName(r: BinaryReader, field: String): String {
        val codePage = r.u8("$field.codePage")
        val charset = when (codePage) {
            0, 1 -> Charsets.ISO_8859_1
            2, 3, 5, 7, 9, 13, 15, 16 -> Charset.forName("ISO-8859-$codePage")
            80 -> Charset.forName("KOI8-R")
            85 -> Charset.forName("KOI8-U")
            else -> throw ParseError(r.offset - 1, field, "unsupported code page")
        }
        return r.text(35, charset, field)
    }
}

/** No storage side effects: identity and complete raw activity file are returned together. */
class DriverCardReader(private val transport: CardReaderTransport) {
    suspend fun read(): CardReadResult {
        try {
            when (transport.open()) {
                ReaderState.Open, ReaderState.CardPresent -> Unit
                ReaderState.Absent -> throw ReaderException(ReaderFailure.NO_READER)
                ReaderState.PermissionRequired, ReaderState.PermissionDenied -> throw ReaderException(ReaderFailure.PERMISSION)
                ReaderState.Empty -> throw ReaderException(ReaderFailure.CARD_ABSENT)
                ReaderState.Disconnected -> throw ReaderException(ReaderFailure.DISCONNECTED)
            }
            AtrDecoder.parse(transport.powerOn())
            val session = ApduSession(transport)
            val g2 = readApplication(session, DriverCardFiles.gen2Aid, 15)
            val g1 = if (g2 == null) readApplication(session, DriverCardFiles.gen1Aid, 10) else null
            val (recognized, generation) = DriverCardDecoder.generation(g1, g2)
            if (recognized != CardRecognition.DRIVER) return CardReadResult(recognized, generation)
            if (generation == CardGeneration.GEN2V2) readFile(session, DriverCardFiles.APPLICATION_V2, 10)
            return try {
                val application = g2 ?: g1!!
                val activityLength = DriverCardDecoder.activityLength(application, generation)
                val identity = readFile(session, DriverCardFiles.IDENTIFICATION, 143)
                val activity = readFile(session, DriverCardFiles.DRIVER_ACTIVITY, activityLength + 4)
                CardReadResult(recognized, generation, DriverCardDecoder.identity(identity, generation),
                    DriverCardDecoder.activity(activity, activityLength))
            } catch (_: ParseError) {
                CardReadResult(CardRecognition.INCONSISTENT, CardGeneration.UNKNOWN)
            }
        } finally {
            runCatching { transport.powerOff() }
            transport.close()
        }
    }

    private suspend fun readApplication(session: ApduSession, aid: ByteArray, appSize: Int): ByteArray? {
        val selected = session.send(CardCommands.selectAid(aid))
        if (selected.statusWord == 0x6A82) return null
        if (!selected.success) throw ReaderException(ReaderFailure.IO)
        return readFile(session, DriverCardFiles.APPLICATION, appSize)
    }

    private suspend fun readFile(session: ApduSession, id: Int, length: Int): ByteArray {
        if (!session.send(CardCommands.selectFile(id)).success) throw ReaderException(ReaderFailure.IO)
        if (length !in 1..32768) throw ReaderException(ReaderFailure.MALFORMED_RESPONSE)
        val data = ByteArray(length)
        var offset = 0
        while (offset < length) {
            val response = session.send(CardCommands.readBinary(offset, minOf(256, length - offset)))
            if (!response.success || response.data.isEmpty() || response.data.size > length - offset)
                throw ReaderException(ReaderFailure.MALFORMED_RESPONSE)
            response.data.copyInto(data, offset)
            offset += response.data.size
        }
        return data
    }
}
