package de.fahrerabrechnung.domain.violation.weekly

import de.fahrerabrechnung.domain.violation.input.*
import java.time.Instant

/** Semantic lookup on already parsed M1 fields, not a second card parser or a location tracker.
 * NationNumeric: 2016/799 Appendix1 2.100–101 and JRC list, last update25.09.2026.
 * https://dtc.jrc.ec.europa.eu/dtc_nation_codes.php.html (pin in M4 legal evidence).
 * Alpha bumper codes are NOT ISO codes: e.g. F=France and historical FR=Faroe Islands.
 */
internal object RecordedRestCountry {
    private val iso = mapOf(
        0x01 to "AT", 0x02 to "AL", 0x03 to "AD", 0x04 to "AM", 0x05 to "AZ", 0x06 to "BE",
        0x07 to "BG", 0x08 to "BA", 0x09 to "BY", 0x0a to "CH", 0x0b to "CY", 0x0c to "CZ",
        0x0d to "DE", 0x0e to "DK", 0x0f to "ES", 0x10 to "EE", 0x11 to "FR", 0x12 to "FI",
        0x13 to "LI", 0x14 to "FO", 0x15 to "GB", 0x16 to "GE", 0x17 to "GR", 0x18 to "HU",
        0x19 to "HR", 0x1a to "IT", 0x1b to "IE", 0x1c to "IS", 0x1d to "KZ", 0x1e to "LU",
        0x1f to "LT", 0x20 to "LV", 0x21 to "MT", 0x22 to "MC", 0x23 to "MD", 0x24 to "MK",
        0x25 to "NO", 0x26 to "NL", 0x27 to "PT", 0x28 to "PL", 0x29 to "RO", 0x2a to "SM",
        0x2b to "RU", 0x2c to "SE", 0x2d to "SK", 0x2e to "SI", 0x2f to "TM", 0x30 to "TR",
        0x31 to "UA", 0x32 to "VA", 0x34 to "ME", 0x35 to "RS", 0x36 to "UZ", 0x37 to "TJ",
        0x38 to "KG", 0x39 to "IL",
    )
    fun country(raw: String?): String? = raw?.toIntOrNull()?.let(iso::get)
    fun canonical(context: String?): String? = when (context) { "UK" -> "GB"; "EL" -> "GR"; else -> context }
    fun points(snapshot: ViolationInputSnapshot, at: Instant): List<InputPoint> = frozen(snapshot.points.filter {
        it.timeUtc == at && it.family in setOf(InputFamily.PLACES, InputFamily.BORDERS)
    })
    fun countries(points: List<InputPoint>): Set<String> = points.mapNotNull {
        val raw = it.fields[if (it.family == InputFamily.PLACES) "countryRaw" else "countryEnteredRaw"]
        // Newer assignments have no verified individual historical activation dates here.
        // Never back-project those codes to an old recording merely from today's list.
        if (raw?.toIntOrNull()?.let { number -> number >= 0x34 } == true &&
            it.timeUtc?.let { time -> time < Instant.parse("2026-09-26T00:00:00Z") } != false) null else country(raw)
    }.toSet()
    fun evidence(point: InputPoint): String = "point:" + InputRevision.hash("m4-country-point") {
        source(point.source); text(point.family.name); int(point.recordIndex); nullable(point.timeUtc?.toString())
        point.fields.toSortedMap().forEach { (key, value) -> text(key); text(value) }
    }
}
