package de.fahrerabrechnung.domain.violation.rest

import de.fahrerabrechnung.domain.model.ActivityType
import de.fahrerabrechnung.domain.violation.driving.DrivingFixture
import de.fahrerabrechnung.domain.violation.input.*
import java.time.Instant

/** Prior counters are actual recorded days, never an injected claim of RED0/RED3. */
internal class RestFixture(val start: Instant = Instant.parse("2026-09-07T00:00:00Z"),
    priorReductions: Int = 0, weeklyPrefix: Boolean = true, passengers: Boolean = false, occasional: Boolean = false,
    prefix: Boolean = true) {
    private val historySeconds = (if (!prefix) 0 else if (weeklyPrefix) 2700L * 60 else 660L * 60) + priorReductions * 86400L
    val recorded = DrivingFixture(start.minusSeconds(historySeconds), anchored = false, passengers, occasional)
    private val context = mutableListOf<ContextAssertion>()
    private val points = mutableListOf<InputPoint>()
    private var secondApplication = false
    val cursor get() = recorded.cursor
    init {
        if (prefix) recorded.r(if (weeklyPrefix) 2700 else 660)
        repeat(priorReductions) { recorded.w(900).r(540) }
        check(recorded.cursor == start)
    }
    fun w(minutes: Int, crew: Boolean = false, manual: Boolean = false) = apply {
        recorded.add(ActivityType.SONSTIGE_ARBEIT, minutes, crew || manual, !manual)
    }
    fun r(minutes: Int, manual: Boolean = false) = apply { recorded.r(minutes, manual) }
    fun u(minutes: Int) = apply { recorded.u(minutes) }
    fun d(minutes: Int, crew: Boolean = false) = apply { recorded.d(minutes, crew) }
    fun dl(minutes: Int) = apply { recorded.dl(minutes) }
    fun a(minutes: Int, crew: Boolean = false) = apply { recorded.a(minutes, crew) }
    fun loadMarker(at: Instant, raw: Int) = apply {
        secondApplication = true
        points += InputPoint(InputSourceKey("P1", "C1", "S2", "raw-S2", "GEN2V2", "GEN2", "B1", "run-S2"),
            InputFamily.LOAD_TYPE, points.size, at, mapOf("entryTimeRaw" to at.epochSecond.toString(), "loadTypeRaw" to raw.toString()))
    }
    fun ferryMarker(at: Instant, end: Boolean = false, gen2: Boolean = true) = apply {
        secondApplication = secondApplication || gen2
        points += InputPoint(if (gen2) InputSourceKey("P1", "C1", "S2", "raw-S2", "GEN2V2", "GEN2", "B1", "run-S2")
            else InputSourceKey("P1", "C1", "S1", "raw-S1", "GEN1", "GEN1", "B1", "run-S1"),
            InputFamily.SPECIAL_CONDITIONS, points.size, at, mapOf("typeRaw" to if (end) "4" else "3"))
    }
    fun ferryContext(from: Instant, until: Instant? = cursor, facility: String? = "BUNK",
        accompanies: Boolean? = true, scheduledMinutes: Long? = null) = apply {
        val subject = ContextSubject(ContextSubjectKind.JOURNEY, "synthetic-ferry")
        if (facility != null) assertion(ContextKey.FERRY_SLEEPING_FACILITY, ContextValue.Text(facility), subject, from, until)
        if (accompanies != null) assertion(ContextKey.FERRY_ACCOMPANIES_VEHICLE, ContextValue.Flag(accompanies), subject, from, until)
        if (scheduledMinutes != null) assertion(ContextKey.FERRY_SCHEDULED_MINUTES, ContextValue.Number(scheduledMinutes), subject, from, until)
    }
    fun service(from: Instant, until: Instant?, id: String = "service-1", safety: Boolean? = true,
        workingConditions: Boolean? = true, workingTime: Boolean? = true) = apply {
        val subject = ContextSubject(ContextSubjectKind.SERVICE, id)
        assertion(ContextKey.SINGLE_OCCASIONAL_SERVICE, ContextValue.Flag(true), subject, from, until)
        for ((key, value) in listOf(ContextKey.ROAD_SAFETY_PRESERVED to safety,
            ContextKey.WORKING_CONDITIONS_PRESERVED to workingConditions,
            ContextKey.APPLICABLE_MAX_WORKING_TIME_RESPECTED to workingTime))
            if (value != null) assertion(key, ContextValue.Flag(value), subject, from, until)
    }
    fun assertion(key: ContextKey, value: ContextValue, subject: ContextSubject, from: Instant, until: Instant?) = apply {
        context += ContextAssertion(key, value, subject, from, until, ContextOrigin.USER_CONTEXT,
            "synthetic-M3-reference", start)
    }
    fun snapshot(withEnd: Boolean = true, duplicate: Boolean = false, conflictingCrew: Boolean = false,
        reviewed: List<ReviewedScopeAdjustment> = emptyList(), contextTransform: (InputContext) -> InputContext = { it }) =
        recorded.snapshot(extraIdenticalSource = secondApplication || duplicate, withEnd = withEnd,
            contextAdditions = context, pointsExtra = points, conflictingCrewSource = conflictingCrew, reviewed = reviewed, contextTransform = contextTransform)
}
