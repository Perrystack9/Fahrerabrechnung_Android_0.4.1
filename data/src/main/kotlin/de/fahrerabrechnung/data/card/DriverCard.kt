package de.fahrerabrechnung.data.card

import java.nio.charset.Charset
import java.time.Instant

enum class CardGeneration { GEN1, GEN2, GEN2V2, UNKNOWN, UNSUPPORTED }
enum class CardRecognition { DRIVER, OTHER_CARD, UNKNOWN_CARD, INCONSISTENT }
data class DriverIdentity(val surname: String, val givenNames: String, val birthDate: Instant,
                          val language: String)
data class CardIdentity(val number: String, val issuingState: Int, val issuingAuthority: String,
                        val issued: Instant, val validFrom: Instant, val expires: Instant)
data class DriverCard(val generation: CardGeneration, val card: CardIdentity, val holder: DriverIdentity)
data class CardReadResult(val recognition: CardRecognition, val generation: CardGeneration,
                          val card: DriverCard? = null)

data class CardFile(val id: Int, val name: String, val generation: CardGeneration,
                    val required: Boolean, val expectedLength: Int, val parser: String)

/** EU 2016/799 Annex IC Appendix 2, section 4.2. */
object DriverCardFiles {
    val gen1Aid = byteArrayOf(0xFF.toByte(), 0x54, 0x41, 0x43, 0x48, 0x4F)
    val gen2Aid = byteArrayOf(0xFF.toByte(), 0x53, 0x4D, 0x52, 0x44, 0x54)
    const val MF = 0x3F00
    const val APPLICATION = 0x0501
    const val IDENTIFICATION = 0x0520
    val files = listOf(
        CardFile(APPLICATION, "Application_Identification", CardGeneration.GEN1, true, 10, "ApplicationIdG1"),
        CardFile(IDENTIFICATION, "Identification", CardGeneration.GEN1, true, 143, "DriverIdentity"),
        CardFile(APPLICATION, "Application_Identification", CardGeneration.GEN2, true, 15, "ApplicationIdG2"),
        CardFile(IDENTIFICATION, "Identification", CardGeneration.GEN2, true, 143, "DriverIdentity")
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

/** No storage side effects: only a fully read and validated card is returned. */
class DriverCardReader(private val transport: CardReaderTransport) {
    suspend fun read(): CardReadResult {
        try {
            transport.open()
            AtrDecoder.parse(transport.powerOn())
            val session = ApduSession(transport)
            val g2 = readApplication(session, DriverCardFiles.gen2Aid, 15)
            val g1 = if (g2 == null) readApplication(session, DriverCardFiles.gen1Aid, 10) else null
            val (recognized, generation) = DriverCardDecoder.generation(g1, g2)
            if (recognized != CardRecognition.DRIVER) return CardReadResult(recognized, generation)
            val identity = readFile(session, DriverCardFiles.IDENTIFICATION, 143)
            return try {
                CardReadResult(recognized, generation, DriverCardDecoder.identity(identity, generation))
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
        val data = ArrayList<Byte>()
        while (data.size < length) {
            val response = session.send(CardCommands.readBinary(data.size, minOf(256, length - data.size)))
            if (!response.success || response.data.isEmpty() || response.data.size > length - data.size)
                throw ReaderException(ReaderFailure.MALFORMED_RESPONSE)
            data.addAll(response.data.toList())
        }
        return data.toByteArray()
    }
}
