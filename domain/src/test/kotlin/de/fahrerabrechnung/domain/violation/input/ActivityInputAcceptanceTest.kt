package de.fahrerabrechnung.domain.violation.input

import de.fahrerabrechnung.domain.model.ActivityType
import org.junit.Assert.*
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.util.concurrent.CancellationException

/** M0 MI vectors use independently specified minute totals, not the UI's midnight extension. */
class ActivityInputAcceptanceTest {
    private val day = Instant.parse("2026-09-07T00:00:00Z")
    private val rest = ActivityType.PAUSE_RUHE
    private val work = ActivityType.SONSTIGE_ARBEIT
    private val driving = ActivityType.LENKZEIT
    private fun at(minute: Long) = day.plusSeconds(minute * 60)
    private fun change(minute: Int, activity: ActivityType = rest, c: Boolean = false,
        inserted: Boolean = true, slot: Boolean = false) = RecordedChange(minute, activity, slot, c, inserted)
    private fun source(id: String = "S1", generation: String = "GEN1", card: String = "C1") =
        InputSource(InputSourceKey("P1", card, id, "raw-$id", generation,
            if (generation == "GEN1") "GEN1" else "GEN2", "B1", "run-$id"),
            listOf(FileEvidence(InputFamily.ACTIVITIES, 0x0504, FileAvailability.PRESENT, "raw-$id", "digest-$id")), emptyList())
    private fun record(vararg changes: RecordedChange, index: Int = 0, date: Instant = day) =
        RecordedDay(index, index * 20, date, "day-$index-${changes.contentHashCode()}", "0001", 0, changes.toList())
    private fun activity(vararg records: RecordedDay, input: InputSource = source()) = ActivitySource(input, records.toList())
    private fun build(vararg sources: ActivitySource, context: InputContext = InputContext.empty("P1"),
        points: List<InputPoint> = emptyList()): ViolationInputSnapshot = InputSnapshotBuilder.build(
            InputSources("P1", sources.map { it.source.key.cardId }.distinct(), sources.toList(), points,
                sources.map { it.source }, emptyList(), emptyMap()), context)
    private fun minutes(snapshot: ViolationInputSnapshot, type: ActivityType, start: Instant = day,
        end: Instant = at(2880)) = snapshot.intervals.filter { it.meaning.activity == type }.sumOf {
            val a = maxOf(start, it.range.start); val b = minOf(end, it.range.end)
            if (a < b) Duration.between(a, b).toMinutes() else 0
        }

    @Test fun mi01_all32SlotCardCrewActivityCombinationsHaveCardDependentMeaning() {
        for (slot in listOf(false, true)) for (c in listOf(false, true))
            for (inserted in listOf(false, true)) for (type in listOf(rest, ActivityType.BEREITSCHAFT, work, driving)) {
                val result = ActivityIntervals.meaning(change(360, type, c, inserted, slot))
                when {
                    !inserted && (!c || type == driving) -> {
                        assertEquals(InputQuality.UNKNOWN, result.quality)
                        assertEquals(ActivityType.UNBEKANNT, result.activity)
                        assertEquals(CrewEvidence.NOT_RECORDED, result.crew)
                    }
                    !inserted -> {
                        assertEquals(type, result.activity); assertTrue(result.manualTachograph)
                        assertEquals(CrewEvidence.NOT_RECORDED, result.crew)
                        assertEquals(SlotEvidence.NOT_RELEVANT, result.slot)
                    }
                    else -> {
                        assertEquals(type, result.activity); assertFalse(result.manualTachograph)
                        assertEquals(if (c) CrewEvidence.CREW else CrewEvidence.SOLO, result.crew)
                        assertEquals(if (slot) SlotEvidence.CO_DRIVER else SlotEvidence.DRIVER, result.slot)
                    }
                }
            }
    }

    @Test fun mi02_withdrawalRetainsAllFourRawActivitiesButFollowing120MinutesAreUnknown() {
        for (raw in listOf(rest, ActivityType.BEREITSCHAFT, work, driving)) {
            val snapshot = build(activity(record(change(0, work), change(480, raw, inserted = false), change(600, work))))
            assertEquals(480, minutes(snapshot, work))
            assertEquals(120, minutes(snapshot, ActivityType.UNBEKANNT))
            assertEquals(raw, snapshot.observations[1].rawChanges.single().activity)
            assertEquals(InputQuality.UNKNOWN, snapshot.intervals.last().meaning.quality)
        }
    }

    @Test fun mi03_newKnownManualEntryFillsOldUnknownWithoutMutatingOrDroppingItsSource() {
        val old = activity(record(change(0, work), change(480, inserted = false), change(600, work)))
        val later = activity(record(change(0, work), change(480, c = true, inserted = false), change(600, work)), input = source("S2"))
        val before = build(old)
        val after = build(old, later)
        assertEquals(120, minutes(before, ActivityType.UNBEKANNT))
        assertEquals(120, minutes(after, rest))
        assertEquals(0, minutes(after, ActivityType.UNBEKANNT))
        assertTrue(after.intervals.last().meaning.manualTachograph)
        assertEquals("Manueller Nachtrag am EG-Kontrollgerät", MANUAL_TACHOGRAPH_LABEL)
        assertEquals(setOf("S1", "S2"), after.intervals.last().support.flatMap { it.begin }.map { it.source.sessionId }.toSet())
        assertFalse(old.days.single().changes[1].rawC)
        assertNotEquals(before.revision, after.revision)
        assertFalse(after.issues.any { it.code == InputIssueCode.SOURCE_CONFLICT })
    }

    @Test fun mi04_manualDrivingCombinationIsAnomalyNotDrivingOrCrew() {
        val snapshot = build(activity(record(change(0), change(360, driving, c = true, inserted = false), change(480, work))))
        val interval = snapshot.intervals.last()
        assertEquals(120, Duration.between(interval.range.start, interval.range.end).toMinutes())
        assertEquals(InputIssueCode.MANUAL_DRIVING_ANOMALY, interval.meaning.reason)
        assertEquals(CrewEvidence.NOT_RECORDED, interval.meaning.crew)
        assertFalse(interval.meaning.manualTachograph)
        assertEquals(driving, snapshot.observations[1].rawChanges.single().activity)
        assertEquals(0, minutes(snapshot, driving))
    }

    @Test fun mi05_sameMinuteOrderCannotChooseWinnerOrInventDuration() {
        val known = change(480, work)
        val removed = change(480, work, inserted = false)
        val first = build(activity(record(change(0), known, removed, change(600, work))))
        val reversed = build(activity(record(change(0), removed, known, change(600, work))))
        for (snapshot in listOf(first, reversed)) {
            assertEquals(120, minutes(snapshot, ActivityType.UNBEKANNT))
            assertEquals(0, minutes(snapshot, work))
            assertEquals(listOf(1, 2), snapshot.observations[1].references.map { it.changeIndex })
            assertEquals(InputIssueCode.AMBIGUOUS_MINUTE, snapshot.intervals.last().meaning.reason)
            assertTrue(snapshot.intervals.all { it.range.start < it.range.end })
        }
        assertEquals(first.dataRevision, reversed.dataRevision)
        assertNotEquals(first.observations[1].rawChanges, reversed.observations[1].rawChanges)
    }

    @Test fun mi06_identicalSameMinuteChangesPreserveBothIndicesAndExactly120WorkMinutes() {
        val snapshot = build(activity(record(change(0), change(480, work), change(480, work), change(600))))
        assertEquals(120, minutes(snapshot, work))
        assertEquals(listOf(1, 2), snapshot.intervals.last().support.single().begin.map { it.changeIndex })
        assertFalse(snapshot.issues.any { it.code == InputIssueCode.AMBIGUOUS_MINUTE })
    }

    @Test fun mi07_nonMidnightRecordIsNotRoundedToInventedDayAndValidOtherDaySurvives() {
        val snapshot = build(activity(record(change(0), change(600, work), date = day.plusSeconds(1)),
            record(change(0), change(60, work), index = 1, date = at(1440))))
        assertEquals(60, minutes(snapshot, rest))
        assertEquals(at(1440), snapshot.intervals.first().range.start)
        assertTrue(snapshot.issues.any { it.code == InputIssueCode.NON_MIDNIGHT_DATE })
        val coverage = InputCoverage.forRule(snapshot, InputRange(at(1440), at(1500)),
            RuleInputRequirement("test", setOf(InputDependency.ACTIVITY)))
        assertTrue(coverage.factualPrerequisitesCovered)
        assertTrue(coverage.sourceLimitations.any { it.code == InputIssueCode.NON_MIDNIGHT_DATE })
    }

    @Test fun mi08_reversedSourceDatesAreNotRepairedBySorting() {
        val snapshot = build(activity(record(change(0), change(600), date = at(1440)),
            record(change(0), change(600), index = 1)))
        assertTrue(snapshot.intervals.isEmpty())
        assertTrue(snapshot.issues.any { it.code == InputIssueCode.REVERSED_OR_DUPLICATE_DAY })
    }

    @Test fun mi09_newestPointIsOpenDespiteArbitraryImportOrMetadataClocks() {
        val first = activity(record(change(0), change(600, work)))
        val laterMetadata = first.copy(source = first.source.copy(metadataDigests = mapOf("0507" to "present", "savedAt" to "999999999")))
        val snapshot = build(laterMetadata)
        assertEquals(at(600), snapshot.latestClosedEnd)
        assertEquals(at(600), snapshot.latestObservation)
        assertEquals(0, minutes(snapshot, work))
        assertTrue(snapshot.issues.any { it.code == InputIssueCode.OPEN_END && it.at == at(600) })
        assertFalse(InputCoverage.forRule(snapshot, InputRange(at(600), at(601)),
            RuleInputRequirement("open", setOf(InputDependency.ACTIVITY))).factualPrerequisitesCovered)
    }

    @Test fun mi10_fullConsecutiveDayChainHas660RestMinutesAndBothMidnightAnchors() {
        val snapshot = build(activity(record(change(0, work), change(1320, c = true, inserted = false)),
            record(change(0, c = true, inserted = false), change(540, work), index = 1, date = at(1440))))
        assertEquals(660, minutes(snapshot, rest))
        val bridge = snapshot.intervals.single { it.range.start == at(1320) }
        assertEquals(at(1440), bridge.range.end)
        assertEquals(EndEvidenceKind.NEXT_UTC_DAY, bridge.support.single().kind)
        assertEquals(0, bridge.support.single().begin.single().recordIndex)
        assertEquals(1, bridge.support.single().end.single().recordIndex)
        assertEquals(at(1980), snapshot.latestClosedEnd)
        assertTrue(InputCoverage.forRule(snapshot, InputRange(at(1320), at(1980)),
            RuleInputRequirement("rest-facts", setOf(InputDependency.ACTIVITY))).factualPrerequisitesCovered)
    }

    @Test fun mi11_missingDayPartialOrAmbiguousHeadAndOtherSourceDoNotCloseHistoricalTail() {
        val first = record(change(0, work), change(1320))
        val variants = listOf(
            arrayOf(activity(first)),
            arrayOf(activity(first, record(change(0), change(540, work), index = 1, date = at(2880)))),
            arrayOf(activity(first, record(change(1), change(540, work), index = 1, date = at(1440)))),
            arrayOf(activity(first, record(change(0), change(0, work), change(540, work), index = 1, date = at(1440)))),
            arrayOf(activity(first), activity(record(change(0), change(540, work), date = at(1440)), input = source("S2"))),
        )
        for (variant in variants) {
            val snapshot = build(*variant)
            val tail = snapshot.intervals.filter { it.range.start >= at(1320) && it.range.end <= at(1440) }
            assertTrue(tail.none { it.meaning.quality == InputQuality.KNOWN })
            assertTrue(snapshot.issues.any { it.code == InputIssueCode.OPEN_END && it.at == at(1320) })
        }
    }

    @Test fun mi12_unknownCanHaveProvedMidnightEndWithoutBecomingRest() {
        val snapshot = build(activity(record(change(0, work), change(1320, inserted = false)),
            record(change(0, inserted = false), change(540, work), index = 1, date = at(1440))))
        assertEquals(660, minutes(snapshot, ActivityType.UNBEKANNT))
        assertEquals(0, minutes(snapshot, rest))
        assertEquals(EndEvidenceKind.NEXT_UTC_DAY, snapshot.intervals[1].support.single().kind)
    }

    @Test fun mi13_realZeroChangeIsObservedButNotAnEntireRestDayAndEmptyRemainsEmpty() {
        val observed = build(activity(record(change(0))))
        val emptySource = source().let { it.copy(files = it.files.map { file -> file.copy(availability = FileAvailability.EMPTY, contentDigest = "empty") }) }
        val empty = build(activity(input = emptySource))
        assertEquals(1, observed.observations.size); assertTrue(observed.intervals.isEmpty())
        assertTrue(empty.observations.isEmpty()); assertTrue(empty.intervals.isEmpty())
        assertNotEquals(observed.revision, empty.revision)
        assertNotEquals(observed.dataRevision, empty.dataRevision)
    }

    @Test fun mi14_bothApplicationsAndCompatibleReplayCountActualMinutesOnceAndRetainAllOrigins() {
        val g1 = activity(record(change(0), change(480, work), change(600)))
        val g2 = activity(record(change(0), change(480, work), change(600)), input = source("S2", "GEN2V2"))
        val first = build(g1)
        val merged = build(g2, g1)
        assertEquals(120, minutes(merged, work))
        assertEquals(first.dataRevision, merged.dataRevision)
        assertNotEquals(first.provenanceRevision, merged.provenanceRevision)
        assertEquals(build(g1, g2).revision, merged.revision)
        assertEquals(merged.revision, build(g1, g1, g2).revision)
        assertEquals(setOf("GEN1", "GEN2V2"), merged.intervals.last().support.flatMap { it.begin }.map { it.source.generation }.toSet())
        assertEquals("GEN2", merged.sources.single { it.key.generation == "GEN2V2" }.key.application)
        // Identical raw condition codes can have different meanings in the two technical formats.
        fun withConditions(input: ActivitySource) = input.copy(source = input.source.copy(files = input.source.files +
            FileEvidence(InputFamily.SPECIAL_CONDITIONS, 0x0522, FileAvailability.PRESENT, "conditions", "digest")))
        val firstFormat = withConditions(g1); val secondFormat = withConditions(g2)
        fun marker(input: ActivitySource) = listOf(InputPoint(input.source.key, InputFamily.SPECIAL_CONDITIONS,
            0, at(60), mapOf("typeRaw" to "3")))
        assertNotEquals(build(firstFormat, points = marker(firstFormat)).dataRevision,
            build(secondFormat, points = marker(secondFormat)).dataRevision)
    }

    @Test fun mi20_crewOnlyInRecordedInsertedIntervalAndNeverInheritedIntoManualOrGap() {
        val snapshot = build(activity(record(change(0), change(360, driving, c = true),
            change(480, c = true, inserted = false), change(600, inserted = false), change(660, work))))
        assertEquals(listOf(CrewEvidence.CREW, CrewEvidence.NOT_RECORDED, CrewEvidence.NOT_RECORDED),
            snapshot.intervals.drop(1).map { it.meaning.crew })
        assertTrue(snapshot.intervals[2].meaning.manualTachograph)
        assertEquals(InputQuality.UNKNOWN, snapshot.intervals[3].meaning.quality)
        assertTrue(snapshot.contextNeeds.none { it.key.name.contains("CREW") })
        val ambiguousCrew = activity(record(change(0), change(360, work, c = true),
            change(360, work, inserted = false), change(600)))
        val manual = activity(record(change(0), change(360, work, c = true, inserted = false), change(600)), input = source("S2"))
        val resolvedActivity = build(ambiguousCrew, manual).intervals.last()
        assertEquals(work, resolvedActivity.meaning.activity)
        assertEquals(CrewEvidence.NOT_RECORDED, resolvedActivity.meaning.crew)
    }

    @Test fun mi24_conflictingKnownSourcesHaveNoGenerationOrNewestWinner() {
        val old = activity(record(change(0), change(480, work), change(600)))
        val new = activity(record(change(0), change(480, driving), change(540, work), change(600)), input = source("S2", "GEN2"))
        val snapshot = build(old, new)
        assertEquals(60, minutes(snapshot, work))
        assertEquals(60, minutes(snapshot, ActivityType.UNBEKANNT))
        assertEquals(InputQuality.CONFLICT, snapshot.intervals.single { it.range.start == at(480) }.meaning.quality)
        assertEquals(snapshot.revision, build(new, old).revision)
        assertTrue(snapshot.issues.any { it.code == InputIssueCode.SOURCE_CONFLICT })
        val ambiguousKnown = activity(record(change(0), change(480, work), change(480, driving), change(600)))
        val claimedWinner = activity(record(change(0), change(480, work), change(600)), input = source("S2", "GEN2V2"))
        val retained = build(ambiguousKnown, claimedWinner)
        assertEquals(InputQuality.CONFLICT, retained.intervals.last().meaning.quality)
        assertEquals(120, minutes(retained, ActivityType.UNBEKANNT))
        assertEquals(0, minutes(retained, work))
    }

    @Test fun compatibleOpenStartBorrowsConcreteEndWithBothSourcesExplicit() {
        val old = activity(record(change(0), change(480, work)))
        val new = activity(record(change(0), change(480, work), change(600)), input = source("S2"))
        val last = build(old, new).intervals.last()
        val borrowed = last.support.single { it.kind == EndEvidenceKind.COMPATIBLE_SOURCE }
        assertEquals(setOf("S1", "S2"), borrowed.begin.map { it.source.sessionId }.toSet())
        assertEquals(setOf("S2"), borrowed.end.map { it.source.sessionId }.toSet())
        assertEquals(2, borrowed.end.single().changeIndex)
    }

    @Test fun aConflictingUnendedPointCannotDisappearBehindAnOlderClosedInterval() {
        val closed = activity(record(change(0), change(480, work), change(600)))
        val open = activity(record(change(0), change(540, driving)), input = source("S2"))
        val snapshot = build(closed, open)
        val conflict = snapshot.intervals.single { it.range.start == at(540) }
        assertEquals(InputQuality.CONFLICT, conflict.meaning.quality)
        assertEquals(at(600), conflict.range.end)
        assertEquals(0, minutes(snapshot, driving))
        assertTrue(conflict.support.any { it.kind == EndEvidenceKind.OPEN_POINT_CONFLICT && it.begin.single().source.sessionId == "S2" })
        assertTrue(snapshot.issues.any { it.code == InputIssueCode.OPEN_END && it.source?.sessionId == "S2" })
        val ambiguousOpen = activity(record(change(0), change(540, work), change(540, driving)), input = source("S2"))
        val retained = build(closed, ambiguousOpen)
        assertEquals(InputQuality.CONFLICT, retained.intervals.single { it.range.start == at(540) }.meaning.quality)
    }

    @Test fun incompatibleCrewOrSlotDoesNotInventAConflictInTheRecordedActivity() {
        val solo = activity(record(change(0, work), change(120)))
        val crew = activity(record(change(0, work, c = true, slot = true), change(120)), input = source("S2"))
        val first = build(solo, crew).intervals.first()
        assertEquals(work, first.meaning.activity)
        assertEquals(InputQuality.KNOWN, first.meaning.quality)
        assertEquals(CrewEvidence.CONFLICT, first.meaning.crew)
        assertEquals(SlotEvidence.CONFLICT, first.meaning.slot)
    }

    @Test fun daylightSavingChangesNeverUseLocalClockSubtraction() {
        val date = Instant.parse("2026-10-25T00:00:00Z")
        val snapshot = build(activity(record(change(0), change(180, work), date = date)))
        assertEquals(180, minutes(snapshot, rest, date, date.plusSeconds(10800)))
        assertEquals(10800, Duration.between(snapshot.intervals.single().range.start, snapshot.intervals.single().range.end).seconds)
    }

    @Test fun snapshotsCopyAndFreezeMutableCallerCollections() {
        val changes = mutableListOf(change(0), change(120, work))
        val input = activity(record(*changes.toTypedArray()))
        val sources = mutableListOf(input.source)
        val raw = InputSources("P1", mutableListOf("C1"), mutableListOf(input), mutableListOf(), sources,
            mutableListOf(), mutableMapOf())
        val snapshot = InputSnapshotBuilder.build(raw, InputContext.empty("P1"))
        sources.clear(); changes.clear()
        assertEquals(1, snapshot.sources.size)
        assertThrows(UnsupportedOperationException::class.java) { (snapshot.intervals as MutableList).clear() }
        assertThrows(UnsupportedOperationException::class.java) { (snapshot.intervals[0].support[0].begin as MutableList).clear() }
    }

    @Test fun cancellationIsPropagatedWithoutPartialSnapshot() {
        val input = activity(record(change(0), change(120, work)))
        assertThrows(CancellationException::class.java) { InputSnapshotBuilder.build(
            InputSources("P1", listOf("C1"), listOf(input), emptyList(), listOf(input.source), emptyList(), emptyMap()),
            InputContext.empty("P1"), checkCancellation = { throw CancellationException("synthetic") }) }
    }
}
