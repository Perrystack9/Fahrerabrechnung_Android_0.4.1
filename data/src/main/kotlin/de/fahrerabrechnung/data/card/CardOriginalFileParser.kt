package de.fahrerabrechnung.data.card

/** Original identification EFs in the already selected driver-card application. */
enum class CardOriginalFileKind(val fileId: Int) {
    APPLICATION_IDENTIFICATION(0x0501), APPLICATION_IDENTIFICATION_V2(0x0525), IDENTIFICATION(0x0520);

    fun supports(generation: CardGeneration): Boolean = when (generation) {
        CardGeneration.GEN1, CardGeneration.GEN2 -> this != APPLICATION_IDENTIFICATION_V2
        CardGeneration.GEN2V2 -> true
        else -> false
    }
    internal val storageKind: String get() = "ORIGINAL_$name"
}

/** The complete original bytes are retained; decoded values never replace them. */
class CardOriginalFile(val kind: CardOriginalFileKind, val generation: CardGeneration, bytes: ByteArray) {
    init {
        val lengths = when (kind) {
            CardOriginalFileKind.APPLICATION_IDENTIFICATION -> if (generation == CardGeneration.GEN1) setOf(10) else setOf(15, 17)
            CardOriginalFileKind.APPLICATION_IDENTIFICATION_V2 -> setOf(10)
            CardOriginalFileKind.IDENTIFICATION -> setOf(143)
        }
        if (!kind.supports(generation) || bytes.size !in lengths)
            throw ParseError(0, "OriginalIdentification", "unsupported generation, kind or complete length")
    }
    private val payload = bytes.copyOf()
    val bytes: ByteArray get() = payload.copyOf()
}

data class CardOriginalFileRead(val file: CardOriginalFile? = null, val readFailed: Boolean = false)

sealed interface CardOriginalFileRecord

data class CardApplicationIdentificationRecord(val cardTypeRaw: Int, val structureVersionBytes: List<Int>,
    val eventsPerTypeRaw: Int, val faultsPerTypeRaw: Int, val activityBytesRaw: Int,
    val vehicleRecordsRaw: Int, val placeRecordsRaw: Int, val gnssRecordsRaw: Int?,
    val specificConditionRecordsRaw: Int?, val vehicleUnitRecordsRaw: Int?) : CardOriginalFileRecord

data class CardApplicationIdentificationV2Record(val followingDataLengthRaw: Int, val borderRecordsRaw: Int,
    val loadUnloadRecordsRaw: Int, val loadTypeRecordsRaw: Int, val vehicleUnitConfigurationBytesRaw: Int) : CardOriginalFileRecord

data class CardIdentificationRecord(val card: DriverCard, val authorityCodePageRaw: Int,
    val surnameCodePageRaw: Int, val givenNamesCodePageRaw: Int) : CardOriginalFileRecord

/** EU2016/799 Appendix1 2.61/62, Appendix2 TCS149/153; EU2018/502 and EU2021/1228 2.61a.
 * Capacity fields remain their recorded unsigned values, including unusable/unknown values.
 * This decoder does not expand capacity bounds of any activity/event reader. */
object CardOriginalFileParser {
    fun parse(file: CardOriginalFile): CardOriginalFileRecord {
        val bytes = file.bytes
        fun u8(offset: Int) = bytes[offset].toInt() and 255
        fun u16(offset: Int) = (u8(offset) shl 8) or u8(offset + 1)
        return when (file.kind) {
            CardOriginalFileKind.APPLICATION_IDENTIFICATION -> {
                val actual = if (file.generation == CardGeneration.GEN1) DriverCardDecoder.generation(bytes, null)
                    else DriverCardDecoder.generation(null, bytes)
                if (actual.first != CardRecognition.DRIVER || actual.second != file.generation)
                    throw ParseError(0, "Application_Identification", "inconsistent driver-card generation")
                CardApplicationIdentificationRecord(u8(0), listOf(u8(1), u8(2)), u8(3), u8(4), u16(5), u16(7),
                    if (file.generation == CardGeneration.GEN1) u8(9) else u16(9),
                    if (bytes.size >= 15) u16(11) else null, if (bytes.size >= 15) u16(13) else null,
                    if (bytes.size == 17) u16(15) else null)
            }
            CardOriginalFileKind.APPLICATION_IDENTIFICATION_V2 -> {
                if (u16(0) != bytes.size - 2)
                    throw ParseError(0, "Application_Identification_V2", "inconsistent following-data length")
                CardApplicationIdentificationV2Record(u16(0), u16(2), u16(4), u16(6), u16(8))
            }
            CardOriginalFileKind.IDENTIFICATION -> CardIdentificationRecord(
                DriverCardDecoder.identity(bytes, file.generation), u8(17), u8(65), u8(101))
        }
    }

    internal fun validateIdentity(file: CardOriginalFile, card: DriverCard) {
        val record = parse(file)
        require(file.generation == card.generation)
        if (record is CardIdentificationRecord) require(record.card == card)
    }
}

/** A one-byte boundary probe while this EF is still selected. No reread or card-writing command. */
internal object CardOriginalFileReader {
    suspend fun retain(session: ApduSession, kind: CardOriginalFileKind, generation: CardGeneration,
        bytes: ByteArray?): CardOriginalFileRead {
        if (bytes == null) return CardOriginalFileRead(readFailed = true)
        return try {
            val response = session.send(CardCommands.readBinary(bytes.size, 1), correctLength = false)
            if (response.data.isNotEmpty() || response.statusWord !in setOf(0x6700, 0x6B00, 0x6282))
                throw ReaderException(ReaderFailure.MALFORMED_RESPONSE)
            CardOriginalFileRead(CardOriginalFile(kind, generation, bytes))
        } catch (_: ReaderException) { CardOriginalFileRead(readFailed = true) }
    }
}
