package de.fahrerabrechnung.domain.violation.driving

import de.fahrerabrechnung.domain.model.ActivityType
import de.fahrerabrechnung.domain.violation.input.*
import java.time.*
import java.time.temporal.TemporalAdjusters

/** Pure consumer of the existing M1 snapshot. No parser, wall clock, display zone or writes. */
object DrivingCalculator {
    private const val NINE = 9 * 3600L
    private const val TEN = 10 * 3600L
    private const val BREAK_DRIVING = 270 * 60L
    private val BASE_DATE = LocalDate.of(2007, 4, 11)
    private val CONVENTION_DATE = LocalDate.of(2011, 6, 7)
    private val CREW_DATE = LocalDate.of(2020, 8, 20)
    private val BUS_DATE = LocalDate.of(2024, 5, 22)
    private val WEEK_RESTRICTIONS = listOf(CalculationRestriction.L02_WEEK_TIME_NOT_LEGALLY_CONFIRMED)
    private val EXTENSION_RESTRICTIONS = WEEK_RESTRICTIONS + CalculationRestriction.L02_EXTENSION_WEEK_NOT_LEGALLY_CONFIRMED

    private data class Atom(val interval: ActivityInterval, val drivingScope: ScopeDecision?, val breakScope: ScopeDecision?,
        val passengerService: String?, val transportKind: String?, val transfer: Boolean?, val contextConflict: Boolean) {
        val range get() = interval.range
        val known get() = interval.meaning.quality == InputQuality.KNOWN
        val driving get() = interval.meaning.activity == ActivityType.LENKZEIT
        val rest get() = interval.meaning.activity == ActivityType.PAUSE_RUHE
        val crewPassenger get() = interval.meaning.activity == ActivityType.BEREITSCHAFT &&
            interval.meaning.crew == CrewEvidence.CREW && interval.meaning.slot == SlotEvidence.CO_DRIVER
        val ambiguousCrew get() = interval.meaning.activity == ActivityType.BEREITSCHAFT &&
            (interval.meaning.crew == CrewEvidence.CONFLICT || interval.meaning.slot == SlotEvidence.CONFLICT)
    }
    private data class Day(val range: InputRange, val driving: List<DrivingSlice>, val anchor: DrivingAnchor?,
        val exact: Boolean, val restrictions: List<CalculationRestriction>) {
        val total get() = driving.sumOf { it.seconds }
        val extensionAt get() = excess(driving, NINE).firstOrNull()?.range?.start
    }
    private enum class DateState { BEFORE, AFTER, UNCERTAIN }

    /** Civil-date activation agreed nowhere by a display setting. ±18 h covers every ZoneOffset. */
    private fun dateState(at: Instant, date: LocalDate): DateState = when {
        at.atOffset(ZoneOffset.MIN).toLocalDate() >= date -> DateState.AFTER
        at.atOffset(ZoneOffset.MAX).toLocalDate() < date -> DateState.BEFORE
        else -> DateState.UNCERTAIN
    }
    private fun weekStart(at: Instant): Instant = at.atOffset(ZoneOffset.UTC).toLocalDate()
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay().toInstant(ZoneOffset.UTC)
    private fun seconds(range: InputRange) = Duration.between(range.start, range.end).seconds
    private fun ferry(point: InputPoint) = point.family == InputFamily.SPECIAL_CONDITIONS &&
        (point.fields["typeRaw"] == "3" || (point.fields["typeRaw"] == "4" && point.source.generation != "GEN1"))
    private fun slice(atom: Atom) = DrivingSlice(atom.range, atom.interval.support)
    private fun law(id: String, at: Instant) = when (id) {
        "D01", "D02" -> RuleLaw(id, "VO (EG) Nr.561/2006 Art.4(k), Art.6(1)", "561/2006:2007-04-11", listOf("Q01-B"))
        "D03" -> RuleLaw(id, "VO (EG) Nr.561/2006 Art.6(2)", "561/2006:2007-04-11", listOf("Q01-B"))
        "D04" -> RuleLaw(id, "VO (EG) Nr.561/2006 Art.6(3)", "561/2006:2007-04-11", listOf("Q01-B"))
        else -> RuleLaw(id, "VO (EG) Nr.561/2006 Art.4(d), Art.7",
            when { dateState(at, BUS_DATE) != DateState.BEFORE -> "561/2006+2024/1258"
                dateState(at, CREW_DATE) != DateState.BEFORE -> "561/2006+2020/1054"
                else -> "561/2006:2007-04-11" },
            when { dateState(at, BUS_DATE) != DateState.BEFORE -> listOf("Q01-B", "Q02", "Q03")
                dateState(at, CREW_DATE) != DateState.BEFORE -> listOf("Q01-B", "Q02")
                else -> listOf("Q01-B") })
    }

    fun calculate(snapshot: ViolationInputSnapshot, policy: DrivingPolicy = DrivingPolicy(),
        checkCancellation: () -> Unit = {}): DrivingCalculation {
        require(snapshot.intervals.zipWithNext().all { (a, b) -> a.range.end <= b.range.start })
        val issues = mutableListOf<DrivingCheckIssue>()
        val atoms = atoms(snapshot, issues, checkCancellation)
        val days = days(atoms, snapshot, issues, checkCancellation)
        val projections = mutableListOf<DrivingProjection>()
        val observed = snapshot.latestClosedEnd
        val first = atoms.firstOrNull()?.range?.start
        val weeks = mutableListOf<InputRange>()
        if (first != null && observed != null) {
            var start = weekStart(first)
            while (start < observed) {
                checkCancellation()
                weeks += InputRange(start, start.plusSeconds(7 * 86400))
                start = start.plusSeconds(7 * 86400)
            }
        }
        val extensions = weeks.map { week ->
            checkCancellation()
            val end = minOf(week.end, checkNotNull(observed))
            val covered = coverage(snapshot, InputRange(week.start, end), "D02", ScopeArticle.DRIVING_6)
            val relevant = days.filter { day -> day.driving.any { it.range.overlaps(week) } }
            WeeklyDrivingExtensions(week, frozen(days.filter { it.exact &&
                CalculationRestriction.M3_FERRY_REST_BOUNDARY_NOT_EVALUATED !in it.restrictions }
                .mapNotNull { it.extensionAt }.filter(week::contains).sorted()),
                covered.factualPrerequisitesCovered && relevant.all { it.exact } &&
                    relevant.none { CalculationRestriction.M3_FERRY_REST_BOUNDARY_NOT_EVALUATED in it.restrictions },
                frozen(EXTENSION_RESTRICTIONS))
        }
        for (day in days) {
            checkCancellation()
            val extensionAt = day.extensionAt
            val week = extensions.firstOrNull { it.week.contains(extensionAt ?: day.range.start) }
            val earlier = week?.events?.count { it < (extensionAt ?: day.range.end) } ?: 0
            // Missing history hides availability. Two positively observed earlier events still exhaust it.
            val hasSlot = earlier < 2
            val limit = if (hasSlot) TEN else NINE
            val restrictions = day.restrictions + EXTENSION_RESTRICTIONS +
                if (week?.historyComplete == true) emptyList() else listOf(CalculationRestriction.LEFT_HISTORY_MISSING)
            projections += projection(snapshot, if (limit == NINE) "D01" else "D02", day.range,
                day.driving, limit, day.exact, restrictions, day.anchor)
        }
        for (week in weeks) {
            checkCancellation()
            projections += weekProjection(snapshot, atoms, week, 56 * 3600L, "D03")
            projections += weekProjection(snapshot, atoms, InputRange(week.start.minusSeconds(7 * 86400), week.end),
                90 * 3600L, "D04")
        }
        val currentBreakBudget = breaks(snapshot, atoms, issues, projections, checkCancellation)
        val findings = projections.mapNotNull { projection ->
            val excess = excess(projection.driving, projection.normalLimitSeconds)
            if (CalculationRestriction.M3_FERRY_REST_BOUNDARY_NOT_EVALUATED in projection.restrictions) {
                issues += DrivingCheckIssue(setOf("D01", "D02", "D06"), "FERRY_DAILY_BOUNDARY_REQUIRES_M3", projection.range)
                null
            } else if (excess.isEmpty()) null else {
                val id = InputRevision.hash("m2-normal-limit-event") {
                    text(DRIVING_INTERPRETATION); text(projection.law.id); text(projection.law.version)
                    instant(projection.range.start); long(projection.normalLimitSeconds)
                    // Event identity is independent of an additional identical import or display preference.
                }
                NormalLimitFinding(id, projection.law, projection.id, projection.range, projection.countedSeconds,
                    projection.normalLimitSeconds, reached(projection.driving, projection.normalLimitSeconds), frozen(excess),
                    projection.quantityIsExact, projection.restrictions, snapshot.revision)
            }
        }
        if (snapshot.intervals.isEmpty()) issues += DrivingCheckIssue(setOf("D01", "D02", "D03", "D04", "B01"), "NO_CLOSED_ACTIVITY_INTERVAL")
        snapshot.issues.forEach { issues += DrivingCheckIssue(setOf("D05", "B05"), "INPUT_" + it.code.name, it.range) }
        issues += DrivingCheckIssue(setOf("D02", "D03", "D04"), "L02_WEEK_AND_EXTENSION_ALLOCATION_OPEN")
        val revision = InputRevision.hash("m2-calculation") {
            text(DRIVING_INTERPRETATION); text(snapshot.revision); text(policy.revision)
        }
        checkCancellation()
        return DrivingCalculation(snapshot.profileId, snapshot.revision, snapshot.dataRevision, snapshot.provenanceRevision,
            revision, policy.revision, observed, frozen(projections), frozen(findings), frozen(extensions),
            frozen(issues.distinct()), snapshot, currentBreakBudget)
    }

    private fun atoms(snapshot: ViolationInputSnapshot, issues: MutableList<DrivingCheckIssue>,
        check: () -> Unit): List<Atom> {
        val result = mutableListOf<Atom>()
        for (interval in snapshot.intervals) {
            check()
            val boundaries = sortedSetOf(interval.range.start, interval.range.end)
            snapshot.scopes.filter { it.range.overlaps(interval.range) }.forEach {
                if (it.range.start > interval.range.start) boundaries += it.range.start
                if (it.range.end < interval.range.end) boundaries += it.range.end
            }
            snapshot.context.assertions.forEach {
                if (it.from > interval.range.start && it.from < interval.range.end) boundaries += it.from
                it.until?.takeIf { end -> end > interval.range.start && end < interval.range.end }?.let(boundaries::add)
            }
            snapshot.points.filter(::ferry)
                .mapNotNull { it.timeUtc }.filter { it > interval.range.start && it < interval.range.end }.forEach(boundaries::add)
            // Clip existing recorded durations only; no new activity or end evidence is manufactured.
            for (date in listOf(BASE_DATE, CONVENTION_DATE, CREW_DATE, BUS_DATE)) {
                val utc = date.atStartOfDay().toInstant(ZoneOffset.UTC)
                for (offset in listOf(-18L, 18L)) utc.plusSeconds(offset * 3600)
                    .takeIf { it > interval.range.start && it < interval.range.end }?.let(boundaries::add)
            }
            boundaries.toList().zipWithNext().forEach { (start, end) ->
                val range = InputRange(start, end)
                if (dateState(start, BASE_DATE) == DateState.BEFORE) {
                    issues += DrivingCheckIssue(setOf("D01", "B01"), "BEFORE_561_TEMPORAL_SCOPE", range)
                } else {
                    val scope = snapshot.scopes.singleOrNull { it.range.start <= start && it.range.end >= end }
                    val transfer = snapshot.context.resolve(ContextKey.TRANSFER_REQUIRES_OTHER_WORK, range)
                    fun temporal(article: ScopeArticle): ScopeDecision? {
                        val decision = scope?.decisions?.singleOrNull { it.article == article }
                        return if (dateState(start, BASE_DATE) == DateState.UNCERTAIN) decision?.copy(
                            status = ScopeStatus.UNKNOWN, reason = "561_ENTRY_CIVIL_TIME_UNCONFIRMED") else decision
                    }
                    result += Atom(interval.copy(range = range), temporal(ScopeArticle.DRIVING_6), temporal(ScopeArticle.BREAK_7),
                        snapshot.context.resolve(ContextKey.PASSENGER_SERVICE, range).text,
                        snapshot.context.resolve(ContextKey.TRANSPORT_KIND, range).text, transfer.flag, transfer.conflict)
                }
            }
        }
        return result
    }

    private fun days(atoms: List<Atom>, snapshot: ViolationInputSnapshot, issues: MutableList<DrivingCheckIssue>,
        check: () -> Unit): List<Day> {
        val result = mutableListOf<Day>()
        var start = atoms.firstOrNull()?.range?.start ?: return result
        var end = start
        var anchor: DrivingAnchor? = null
        var exact = false
        val unlocatedFerry = snapshot.points.any { ferry(it) && it.timeUtc == null }
        val priorMarkers = snapshot.points.filter { ferry(it) && it.timeUtc?.let { at -> at < start } == true }
        val priorTime = priorMarkers.mapNotNull { it.timeUtc }.maxOrNull()
        var ferryPending = priorTime != null
        var ferryActive = priorMarkers.any { it.timeUtc == priorTime && it.fields["typeRaw"] == "3" }
        var driving = mutableListOf<DrivingSlice>()
        val restrictions = mutableSetOf<CalculationRestriction>()
        fun finish(until: Instant) {
            if (driving.isNotEmpty() && start < until) result += Day(InputRange(start, until), frozen(driving), anchor,
                exact, frozen((restrictions + if (ferryPending || unlocatedFerry) setOf(CalculationRestriction.M3_FERRY_REST_BOUNDARY_NOT_EVALUATED)
                    else emptySet()).sortedBy { it.name }))
            driving = mutableListOf()
        }
        var i = 0
        while (i < atoms.size) {
            check()
            val atom = atoms[i]
            if (end < atom.range.start) {
                finish(end); start = atom.range.start; exact = false; anchor = null; restrictions.clear()
                issues += DrivingCheckIssue(setOf("D01", "D02", "D06"), "MISSING_ACTIVITY_RANGE", InputRange(end, atom.range.start))
            }
            val ferryBoundary = snapshot.points.filter { ferry(it) && it.timeUtc == atom.range.start }
            if (ferryBoundary.isNotEmpty()) {
                finish(atom.range.start); start = atom.range.start; exact = false; anchor = null; restrictions.clear()
                ferryPending = true
                // Conflicting same-time begin/end markers are not resolved by a source/index winner.
                ferryActive = ferryBoundary.any { it.fields["typeRaw"] == "3" }
                restrictions += CalculationRestriction.M3_FERRY_REST_BOUNDARY_NOT_EVALUATED
            }
            if (!atom.known || (atom.driving && atom.drivingScope?.status !in setOf(ScopeStatus.APPLICABLE, ScopeStatus.NOT_APPLICABLE))) {
                finish(atom.range.start); start = atom.range.end; exact = false; anchor = null; restrictions.clear()
                issues += DrivingCheckIssue(setOf("D01", "D02", "D06"), "DAILY_ACTIVITY_OR_SCOPE_UNDETERMINED", atom.range)
                end = atom.range.end; i++; continue
            }
            if (atom.rest && atom.transfer != true && !atom.contextConflict) {
                var j = i + 1
                var restEnd = atom.range.end
                val support = atom.interval.support.toMutableList()
                while (j < atoms.size && atoms[j].range.start == restEnd && atoms[j].known && atoms[j].rest &&
                    atoms[j].transfer != true && !atoms[j].contextConflict &&
                    snapshot.points.none { ferry(it) && it.timeUtc == atoms[j].range.start }) {
                    restEnd = atoms[j].range.end; support += atoms[j].interval.support; j++
                }
                val range = InputRange(atom.range.start, restEnd)
                val duration = seconds(range)
                val convention = dateState(range.start, CONVENTION_DATE)
                if (duration >= NINE || (duration >= 7 * 3600L && convention != DateState.BEFORE)) {
                    finish(range.start)
                    val kind = if (duration >= NINE) DailyDrivingAnchorKind.RECORDED_REST_AT_LEAST_NINE_HOURS
                        else DailyDrivingAnchorKind.C2011_3759_SEVEN_HOUR_CONVENTION
                    anchor = DrivingAnchor(range, kind, frozen(support.distinct()))
                    start = restEnd; exact = true; restrictions.clear()
                    if (!ferryActive) ferryPending = false
                    if (kind == DailyDrivingAnchorKind.C2011_3759_SEVEN_HOUR_CONVENTION)
                        restrictions += CalculationRestriction.C2011_3759_RECOMMENDED_DRIVING_CONVENTION
                    if (convention == DateState.UNCERTAIN && duration < NINE)
                        restrictions += CalculationRestriction.HISTORICAL_CHANGE_TIME_NOT_CONFIRMED
                    end = restEnd; i = j; continue
                }
            }
            if (atom.driving && atom.drivingScope?.status == ScopeStatus.APPLICABLE && atom.drivingScope.activityHint != LegalActivityHint.OTHER_WORK)
                driving += slice(atom)
            end = atom.range.end; i++
        }
        finish(end)
        return result
    }

    private fun coverage(snapshot: ViolationInputSnapshot, range: InputRange, id: String, article: ScopeArticle) =
        InputCoverage.forRule(snapshot, range, RuleInputRequirement(id, setOf(InputDependency.ACTIVITY, InputDependency.SCOPE), article))

    private fun projection(snapshot: ViolationInputSnapshot, id: String, range: InputRange, driving: List<DrivingSlice>,
        limit: Long, exact: Boolean, restrictions: Collection<CalculationRestriction>, anchor: DrivingAnchor? = null,
        credits: List<BreakCredit> = emptyList()): DrivingProjection {
        val article = if (id.startsWith("B")) ScopeArticle.BREAK_7 else ScopeArticle.DRIVING_6
        val cov = coverage(snapshot, range, id, article)
        val restriction = restrictions.toMutableSet()
        if (!exact) restriction += CalculationRestriction.LEFT_HISTORY_MISSING
        if (!cov.factualPrerequisitesCovered) restriction += CalculationRestriction.ACTIVITY_OR_SCOPE_GAP
        val baseLaw = law(id, range.start)
        val legal = when {
            credits.any { it.ruleId == "B03" } -> baseLaw.copy(version = "561/2006+2024/1258", sourceIds = listOf("Q01-B", "Q02", "Q03"))
            credits.any { it.ruleId == "B04" } && "Q02" !in baseLaw.sourceIds ->
                baseLaw.copy(version = "561/2006+2020/1054", sourceIds = listOf("Q01-B", "Q02"))
            else -> baseLaw
        }
        val projectionId = InputRevision.hash("m2-projection") {
            text(DRIVING_INTERPRETATION); text(id); text(legal.version); instant(range.start); instant(range.end); long(limit)
        }
        return DrivingProjection(projectionId, legal, range, minOf(range.end, snapshot.latestClosedEnd ?: range.start),
            frozen(driving), limit, exact && cov.factualPrerequisitesCovered, frozen(restriction.sortedBy { it.name }), cov, anchor, frozen(credits))
    }

    private fun weekProjection(snapshot: ViolationInputSnapshot, atoms: List<Atom>, range: InputRange, limit: Long, id: String): DrivingProjection {
        val through = minOf(range.end, checkNotNull(snapshot.latestClosedEnd))
        val observedRange = InputRange(range.start, through)
        val driving = atoms.filter { it.driving && it.known && it.drivingScope?.status == ScopeStatus.APPLICABLE &&
            it.drivingScope.activityHint != LegalActivityHint.OTHER_WORK && it.range.overlaps(observedRange) }.map {
            DrivingSlice(InputRange(maxOf(it.range.start, range.start), minOf(it.range.end, through)), it.interval.support)
        }
        val cov = coverage(snapshot, observedRange, id, ScopeArticle.DRIVING_6)
        val base = projection(snapshot, id, observedRange, driving, limit, cov.factualPrerequisitesCovered, WEEK_RESTRICTIONS)
        return base.copy(range = range, observedThrough = through)
    }

    private fun breaks(snapshot: ViolationInputSnapshot, atoms: List<Atom>, issues: MutableList<DrivingCheckIssue>,
        projections: MutableList<DrivingProjection>, check: () -> Unit): Long? {
        var start = atoms.firstOrNull()?.range?.start ?: return null
        var end = start
        var exact = false
        var driving = mutableListOf<DrivingSlice>()
        var firstPart: BreakCredit? = null
        var resetCredits = emptyList<BreakCredit>()
        val restrictions = mutableSetOf<CalculationRestriction>()
        fun finish(until: Instant, completion: List<BreakCredit> = emptyList()) {
            if (driving.isNotEmpty() && start < until) projections += projection(snapshot, "B01", InputRange(start, until), driving,
                BREAK_DRIVING, exact, restrictions, credits = resetCredits + completion)
            driving = mutableListOf()
        }
        var i = 0
        while (i < atoms.size) {
            check()
            val atom = atoms[i]
            if (end < atom.range.start) {
                finish(end); start = atom.range.start; exact = false; firstPart = null; resetCredits = emptyList(); restrictions.clear()
                issues += DrivingCheckIssue(setOf("B01", "B05"), "MISSING_ACTIVITY_RANGE", InputRange(end, atom.range.start))
            }
            if (!atom.known || atom.ambiguousCrew || (atom.driving && atom.breakScope?.status !in setOf(ScopeStatus.APPLICABLE, ScopeStatus.NOT_APPLICABLE))) {
                finish(atom.range.start); start = atom.range.end; exact = false; firstPart = null; resetCredits = emptyList(); restrictions.clear()
                issues += DrivingCheckIssue(setOf("B01", "B05"), "BREAK_ACTIVITY_OR_SCOPE_UNDETERMINED", atom.range)
                end = atom.range.end; i++; continue
            }
            val eligible = atom.transfer != true && !atom.contextConflict && (atom.rest || atom.crewPassenger)
            if (eligible) {
                var j = i + 1
                var breakEnd = atom.range.end
                var crew = atom.crewPassenger
                var allRest = atom.rest
                val support = atom.interval.support.toMutableList()
                while (j < atoms.size && atoms[j].range.start == breakEnd && atoms[j].known &&
                    atoms[j].transfer != true && !atoms[j].contextConflict && (atoms[j].rest || atoms[j].crewPassenger)) {
                    crew = crew || atoms[j].crewPassenger; allRest = allRest && atoms[j].rest
                    breakEnd = atoms[j].range.end; support += atoms[j].interval.support; j++
                }
                val range = InputRange(atom.range.start, breakEnd)
                val duration = seconds(range)
                val parts = atoms.subList(i, j)
                val crewDates = parts.filter { it.crewPassenger }.map { dateState(it.range.start, CREW_DATE) }
                val crewDate = when {
                    DateState.BEFORE in crewDates -> DateState.BEFORE
                    DateState.UNCERTAIN in crewDates -> DateState.UNCERTAIN
                    else -> DateState.AFTER
                }
                val busDates = parts.map { dateState(it.range.start, BUS_DATE) }
                val busDate = if (DateState.UNCERTAIN in busDates) DateState.UNCERTAIN
                    else if (DateState.BEFORE in busDates) DateState.BEFORE else DateState.AFTER
                val occasion = parts.all { it.transportKind == "PASSENGERS" && it.passengerService == "OCCASIONAL" } && busDate != DateState.BEFORE
                val credit = BreakCredit(range, if (crew) "B04" else if (occasion) "B03" else "B02", frozen(support.distinct()))
                val full = duration >= 45 * 60L && (allRest || crewDate != DateState.BEFORE)
                val split = allRest && firstPart != null && if (occasion && firstPart.ruleId == "B03") duration >= 15 * 60L &&
                    seconds(firstPart.range) + duration >= 45 * 60L else duration >= 30 * 60L
                if (full || split) {
                    if ((crew && crewDate == DateState.UNCERTAIN) || (occasion && busDate == DateState.UNCERTAIN))
                        restrictions += CalculationRestriction.HISTORICAL_CHANGE_TIME_NOT_CONFIRMED
                    val completion = if (full) listOf(credit) else listOf(checkNotNull(firstPart), credit)
                    finish(range.start, completion)
                    start = breakEnd; exact = true; firstPart = null; resetCredits = completion; restrictions.clear()
                    if ((crew && crewDate == DateState.UNCERTAIN) || (occasion && busDate == DateState.UNCERTAIN))
                        restrictions += CalculationRestriction.HISTORICAL_CHANGE_TIME_NOT_CONFIRMED
                    end = breakEnd; i = j; continue
                }
                if (allRest && duration >= 15 * 60L && (firstPart == null || (occasion && seconds(firstPart.range) < duration)))
                    firstPart = credit
                if (crew && crewDate == DateState.BEFORE) {
                    // Do not claim the pre-2020 treatment of an on-board break has been legally reviewed.
                    finish(range.start); start = breakEnd; exact = false; firstPart = null; resetCredits = emptyList(); restrictions.clear()
                    issues += DrivingCheckIssue(setOf("B04"), "PRE_2020_CREW_BREAK_REQUIRES_HISTORICAL_REVIEW", range)
                }
                end = breakEnd; i = j; continue
            }
            if (atom.driving && atom.breakScope?.status == ScopeStatus.APPLICABLE && atom.breakScope.activityHint != LegalActivityHint.OTHER_WORK)
                driving += slice(atom)
            if (atom.transportKind == "PASSENGERS") {
                if (atom.passengerService == "OCCASIONAL") restrictions += CalculationRestriction.M4_BUS_TWELVE_DAY_NIGHT_NOT_EVALUATED
                if (atom.passengerService == null) restrictions += CalculationRestriction.PASSENGER_SERVICE_UNDETERMINED
            }
            if (atom.rest && (atom.transfer == true || atom.contextConflict)) {
                restrictions += CalculationRestriction.TRAVEL_REST_PREREQUISITE_NOT_ESTABLISHED
                issues += DrivingCheckIssue(setOf("B05"), "RECORDED_REST_NOT_UNCONDITIONALLY_CREDITABLE_FOR_TRAVEL", atom.range)
            }
            end = atom.range.end; i++
        }
        val currentDrivingSeconds = driving.sumOf { it.seconds }
        val proofStart = if (start < end) start else resetCredits.firstOrNull()?.range?.start
        val last = atoms.lastOrNull()
        val currentBudget = if (exact && restrictions.isEmpty() && end == snapshot.latestClosedEnd &&
            proofStart != null && proofStart < end && last?.breakScope?.status == ScopeStatus.APPLICABLE &&
            !(last.transportKind == "PASSENGERS" && last.passengerService == "OCCASIONAL")) {
            val coverage = coverage(snapshot, InputRange(proofStart, end), "B01", ScopeArticle.BREAK_7)
            if (coverage.factualPrerequisitesCovered && coverage.excludedScope.isEmpty()) maxOf(0, BREAK_DRIVING - currentDrivingSeconds) else null
        } else null
        finish(end)
        return currentBudget
    }

    private fun reached(driving: List<DrivingSlice>, limit: Long): Instant {
        var count = 0L
        for (slice in driving) {
            if (count + slice.seconds >= limit) return slice.range.start.plusSeconds(limit - count)
            count += slice.seconds
        }
        error("No evidenced threshold crossing")
    }
    private fun excess(driving: List<DrivingSlice>, limit: Long): List<DrivingSlice> {
        var count = 0L
        val result = mutableListOf<DrivingSlice>()
        for (slice in driving) {
            val from = maxOf(0, limit - count)
            if (from < slice.seconds) result += slice.copy(range = InputRange(slice.range.start.plusSeconds(from), slice.range.end))
            count += slice.seconds
        }
        return result
    }
}
