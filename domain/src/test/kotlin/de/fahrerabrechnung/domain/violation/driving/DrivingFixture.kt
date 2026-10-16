package de.fahrerabrechnung.domain.violation.driving

import de.fahrerabrechnung.domain.model.ActivityType
import de.fahrerabrechnung.domain.violation.input.*
import java.time.*

/** Synthetic recorded chronology fed through the existing M1 semantic/interval builder. */
internal class DrivingFixture(val start: Instant = Instant.parse("2026-09-07T00:00:00Z"), anchored: Boolean = true,
    private val passengers: Boolean = false, private val occasional: Boolean = false) {
    private data class Point(val at: Instant, val type: ActivityType, val crew: Boolean, val inserted: Boolean, val slot: Boolean)
    private val points = mutableListOf<Point>()
    var cursor: Instant = start
        private set
    init { if (anchored) points += Point(start.minusSeconds(660 * 60), ActivityType.PAUSE_RUHE, false, true, false) }
    fun add(type: ActivityType, minutes: Int, crew: Boolean = false, inserted: Boolean = true, slot: Boolean = false): DrivingFixture {
        require(minutes >= 0)
        if (minutes > 0) { points += Point(cursor, type, crew, inserted, slot); cursor = cursor.plusSeconds(minutes * 60L) }
        return this
    }
    fun d(minutes: Int, crew: Boolean = false) = add(ActivityType.LENKZEIT, minutes, crew)
    fun r(minutes: Int, manual: Boolean = false) = add(ActivityType.PAUSE_RUHE, minutes, manual, !manual)
    fun w(minutes: Int) = add(ActivityType.SONSTIGE_ARBEIT, minutes)
    fun a(minutes: Int, crew: Boolean = false, inserted: Boolean = true) = add(ActivityType.BEREITSCHAFT, minutes, crew, inserted, true)
    fun u(minutes: Int) = add(ActivityType.PAUSE_RUHE, minutes, false, false)
    fun dl(minutes: Int): DrivingFixture {
        var remaining = minutes
        while (remaining > 270) { d(270); r(45); remaining -= 270 }
        return d(remaining)
    }
    fun day(minutes: Int): DrivingFixture {
        if (minutes == 0) return r(1440)
        val breaks = (minutes - 1) / 270
        w(780 - minutes - breaks * 45); dl(minutes); r(660)
        return this
    }
    fun week(vararg minutes: Int): DrivingFixture { require(minutes.size == 7); minutes.forEach(::day); return this }
    fun snapshot(extraIdenticalSource: Boolean = false, withEnd: Boolean = true,
        contextAdditions: List<ContextAssertion> = emptyList(), pointsExtra: List<InputPoint> = emptyList(),
        conflictingCrewSource: Boolean = false, reviewed: List<ReviewedScopeAdjustment> = emptyList(),
        contextTransform: (InputContext) -> InputContext = { it }): ViolationInputSnapshot {
        val all = points.toMutableList()
        if (withEnd) all += Point(cursor, ActivityType.SONSTIGE_ARBEIT, false, true, false)
        require(all.isNotEmpty())
        val firstDate = all.first().at.atOffset(ZoneOffset.UTC).toLocalDate()
        val lastDate = all.last().at.atOffset(ZoneOffset.UTC).toLocalDate()
        val recorded = mutableListOf<RecordedDay>()
        var date = firstDate
        while (date <= lastDate) {
            val midnight = date.atStartOfDay().toInstant(ZoneOffset.UTC)
            val next = midnight.plusSeconds(86400)
            val entries = all.filter { it.at >= midnight && it.at < next }.toMutableList()
            if (all.first().at < midnight && entries.none { it.at == midnight }) {
                all.lastOrNull { it.at < midnight }?.let { entries.add(0, it.copy(at = midnight)) }
            }
            if (entries.firstOrNull()?.at?.let { it > midnight } == true) {
                // The card format requires a 00:00 marker even before the specified test history.
                // Explicitly UNKNOWN, never a fabricated rest before the first evidenced anchor.
                entries.add(0, Point(midnight, ActivityType.PAUSE_RUHE, false, false, false))
            }
            if (entries.isNotEmpty()) recorded += RecordedDay(recorded.size, recorded.size * 20, midnight,
                "synthetic-$date", "0001", 0, entries.map {
                    RecordedChange(Duration.between(midnight, it.at).toMinutes().toInt(), it.type, it.slot, it.crew, it.inserted)
                })
            date = date.plusDays(1)
        }
        fun source(id: String, generation: String): InputSource = InputSource(InputSourceKey("P1", "C1", id, "raw-$id", generation,
            if (generation == "GEN1") "GEN1" else "GEN2", "B1", "run-$id"),
            listOf(FileEvidence(InputFamily.ACTIVITIES, 0x0504, FileAvailability.PRESENT, "raw-$id", "digest-$id"),
                FileEvidence(InputFamily.SPECIAL_CONDITIONS, 0x0522, FileAvailability.EMPTY, "conditions-$id", "empty")) +
                if (pointsExtra.any { it.family == InputFamily.LOAD_TYPE && it.source.sessionId == id })
                    listOf(FileEvidence(InputFamily.LOAD_TYPE, 0x0530, FileAvailability.PRESENT, "load-$id", "synthetic-load-$id"))
                else emptyList(), emptyList())
        val sources = mutableListOf(source("S1", "GEN1"))
        if (extraIdenticalSource || conflictingCrewSource) sources += source("S2", "GEN2V2")
        val subject = ContextSubject(ContextSubjectKind.PROFILE, "P1")
        val from = all.first().at.minusSeconds(86400)
        fun assertion(key: ContextKey, value: ContextValue) = ContextAssertion(key, value, subject, from, null,
            ContextOrigin.USER_CONTEXT, "synthetic-context", from)
        val context = InputContext("P1", "context-1", listOf(
            assertion(ContextKey.ROAD_CARRIAGE, ContextValue.Flag(true)),
            assertion(ContextKey.TRANSPORT_KIND, ContextValue.Text(if (passengers) "PASSENGERS" else "GOODS")),
            assertion(ContextKey.JOURNEY_AREA, ContextValue.Text("EU_ONLY")),
            assertion(ContextKey.MAX_MASS_KG, ContextValue.Number(40000)),
            assertion(ContextKey.PASSENGER_CAPACITY, ContextValue.Number(50)),
            assertion(ContextKey.PASSENGER_SERVICE, ContextValue.Text(if (occasional) "OCCASIONAL" else "REGULAR")),
            assertion(ContextKey.LINE_LENGTH_METRES, ContextValue.Number(50001)),
        ) + contextAdditions)
        return InputSnapshotBuilder.build(InputSources("P1", listOf("C1"), sources.mapIndexed { index, source ->
            ActivitySource(source, if (conflictingCrewSource && index == 1) recorded.map { day -> day.copy(changes = day.changes.map {
                if (it.activity == ActivityType.BEREITSCHAFT) it.copy(rawC = false, coDriverSlot = false) else it }) } else recorded) },
            pointsExtra, sources, emptyList(), emptyMap()), contextTransform(context), reviewed = reviewed)
    }
}
