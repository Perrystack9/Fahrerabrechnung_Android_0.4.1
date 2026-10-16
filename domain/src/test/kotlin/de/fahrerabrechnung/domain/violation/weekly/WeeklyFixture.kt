package de.fahrerabrechnung.domain.violation.weekly

import de.fahrerabrechnung.domain.violation.driving.DrivingFixture
import de.fahrerabrechnung.domain.violation.input.*
import java.time.Duration
import java.time.Instant

/** Only explicitly listed rests; every intervening recorded minute is synthetic other work. */
internal class WeeklyFixture(val from: Instant = Instant.parse("2026-09-07T00:00:00Z"),
    passengers: Boolean = false, occasional: Boolean = false) {
    val recorded = DrivingFixture(from, anchored = false, passengers, occasional)
    private val context = mutableListOf<ContextAssertion>()
    private val points = mutableListOf<InputPoint>()
    private var secondApplication = false
    val cursor get() = recorded.cursor
    fun until(at: String) = until(Instant.parse(at))
    fun until(at: Instant) = apply {
        require(at >= cursor)
        recorded.w(Math.toIntExact(Duration.between(cursor, at).toMinutes()))
        require(cursor == at)
    }
    fun r(at: String, minutes: Int, manual: Boolean = false) = until(at).apply { recorded.r(minutes, manual) }
    fun countries(employer: String = "DE", residence: String = "PL") = apply {
        for ((key, value) in listOf(ContextKey.EMPLOYER_COUNTRY to employer, ContextKey.RESIDENCE_COUNTRY to residence))
            assertion(key, ContextValue.Text(value), ContextSubject(ContextSubjectKind.PROFILE, "P1"), from, null)
    }
    fun restCountry(at: String, country: String) = apply {
        val time = Instant.parse(at)
        assertion(ContextKey.REST_START_COUNTRY, ContextValue.Text(country),
            ContextSubject(ContextSubjectKind.REST, "rest-${time.epochSecond}"), time, time.plusSeconds(1))
    }
    fun countryPoint(at: String, raw: Int, border: Boolean = false) = apply {
        secondApplication = secondApplication || border
        points += InputPoint(if (border) InputSourceKey("P1", "C1", "S2", "raw-S2", "GEN2V2", "GEN2", "B1", "run-S2") else
            InputSourceKey("P1", "C1", "S1", "raw-S1", "GEN1", "GEN1", "B1", "run-S1"),
            if (border) InputFamily.BORDERS else InputFamily.PLACES, points.size, Instant.parse(at),
            mapOf(if (border) "countryEnteredRaw" to raw.toString() else "countryRaw" to raw.toString()))
    }
    fun assertion(key: ContextKey, value: ContextValue, subject: ContextSubject,
        start: Instant = from, end: Instant? = null) = apply {
        context += ContextAssertion(key, value, subject, start, end, ContextOrigin.USER_CONTEXT,
            "synthetic-M4-reference", from)
    }
    fun service(start: Instant = from, end: Instant? = null, digital: Boolean? = true,
        international: Boolean = false, foreign24: Boolean? = true) = apply {
        val subject = ContextSubject(ContextSubjectKind.SERVICE, "single-bus-service")
        assertion(ContextKey.SINGLE_OCCASIONAL_SERVICE, ContextValue.Flag(true), subject, start, end)
        assertion(ContextKey.JOURNEY_KIND, ContextValue.Text(if (international) "INTERNATIONAL" else "DOMESTIC"), subject, start, end)
        if (digital != null) assertion(ContextKey.BUS_DIGITAL_TACHOGRAPH_ANNEX_IB, ContextValue.Flag(digital), subject, start, end)
        if (foreign24 != null) assertion(ContextKey.OCCASIONAL_FOREIGN_STAY_24H, ContextValue.Flag(foreign24), subject, start, end)
    }
    fun snapshot(duplicate: Boolean = false, withEnd: Boolean = true) = recorded.snapshot(
        extraIdenticalSource = duplicate || secondApplication, withEnd = withEnd, contextAdditions = context, pointsExtra = points)
}
