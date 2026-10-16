package de.fahrerabrechnung.domain.violation.input

import java.time.Instant

enum class ContextSubjectKind { PROFILE, OPERATION, JOURNEY, SERVICE, REST }
data class ContextSubject(val kind: ContextSubjectKind, val id: String) { init { require(id.isNotBlank()) } }
enum class ContextOrigin { EXISTING_PROFILE, USER_CONTEXT, RECORDED_CONTEXT, REVIEWED_MANIFEST }

sealed interface ContextValue {
    data class Flag(val value: Boolean) : ContextValue
    data class Number(val value: Long) : ContextValue
    data class Text(val value: String) : ContextValue
}

/** Deliberately no activity editing, Art.12 recognition, address, home radius or accommodation key. */
enum class ContextKey {
    ROAD_CARRIAGE, TRANSPORT_KIND, MAX_MASS_KG, PASSENGER_CAPACITY, JOURNEY_AREA, JOURNEY_KIND,
    PASSENGER_SERVICE, LINE_LENGTH_METRES, OWN_ACCOUNT, DRIVING_MAIN_ACTIVITY, COMMERCIAL_OPERATION,
    TRANSFER_REQUIRES_OTHER_WORK, OPERATION_COUNTRY, EMPLOYER_COUNTRY, RESIDENCE_COUNTRY, REST_START_COUNTRY,
    FERRY_SLEEPING_FACILITY, FERRY_SCHEDULED_MINUTES, FERRY_ACCOMPANIES_VEHICLE,
    SINGLE_OCCASIONAL_SERVICE, ROAD_SAFETY_PRESERVED, WORKING_CONDITIONS_PRESERVED,
    APPLICABLE_MAX_WORKING_TIME_RESPECTED, BUS_DIGITAL_TACHOGRAPH_ANNEX_IB,
    OCCASIONAL_FOREIGN_STAY_24H;

    fun accepts(value: ContextValue): Boolean = when (this) {
        ROAD_CARRIAGE, OWN_ACCOUNT, DRIVING_MAIN_ACTIVITY, COMMERCIAL_OPERATION, TRANSFER_REQUIRES_OTHER_WORK,
        FERRY_ACCOMPANIES_VEHICLE, SINGLE_OCCASIONAL_SERVICE, ROAD_SAFETY_PRESERVED,
        WORKING_CONDITIONS_PRESERVED, APPLICABLE_MAX_WORKING_TIME_RESPECTED,
        BUS_DIGITAL_TACHOGRAPH_ANNEX_IB, OCCASIONAL_FOREIGN_STAY_24H -> value is ContextValue.Flag
        MAX_MASS_KG, PASSENGER_CAPACITY -> value is ContextValue.Number && value.value > 0
        LINE_LENGTH_METRES, FERRY_SCHEDULED_MINUTES -> value is ContextValue.Number && value.value >= 0
        TRANSPORT_KIND -> value.textIn("GOODS", "PASSENGERS")
        JOURNEY_AREA -> value.textIn("EU_ONLY", "EU_EEA_CH", "AETR_MIXED", "OTHER")
        JOURNEY_KIND -> value.textIn("DOMESTIC", "INTERNATIONAL", "CABOTAGE")
        PASSENGER_SERVICE -> value.textIn("REGULAR", "OCCASIONAL")
        FERRY_SLEEPING_FACILITY -> value.textIn("CABIN", "BUNK", "COUCHETTE", "NONE")
        OPERATION_COUNTRY, EMPLOYER_COUNTRY, RESIDENCE_COUNTRY, REST_START_COUNTRY ->
            value is ContextValue.Text && value.value.matches(Regex("[A-Z]{2}"))
    }
}

private fun ContextValue.textIn(vararg allowed: String) = this is ContextValue.Text && value in allowed

data class ContextAssertion(
    val key: ContextKey, val value: ContextValue, val subject: ContextSubject,
    val from: Instant, val until: Instant?, val origin: ContextOrigin,
    val originReference: String, val recordedAt: Instant,
) {
    init {
        require(key.accepts(value)); require(until == null || from < until)
        require(originReference.isNotBlank() && originReference.length <= 256)
        when (key) {
            ContextKey.REST_START_COUNTRY -> require(subject.kind == ContextSubjectKind.REST)
            ContextKey.FERRY_SLEEPING_FACILITY, ContextKey.FERRY_SCHEDULED_MINUTES, ContextKey.FERRY_ACCOMPANIES_VEHICLE ->
                require(subject.kind in setOf(ContextSubjectKind.REST, ContextSubjectKind.JOURNEY))
            ContextKey.SINGLE_OCCASIONAL_SERVICE, ContextKey.ROAD_SAFETY_PRESERVED,
            ContextKey.WORKING_CONDITIONS_PRESERVED, ContextKey.APPLICABLE_MAX_WORKING_TIME_RESPECTED,
            ContextKey.BUS_DIGITAL_TACHOGRAPH_ANNEX_IB, ContextKey.OCCASIONAL_FOREIGN_STAY_24H ->
                require(subject.kind == ContextSubjectKind.SERVICE)
            else -> Unit
        }
    }
    fun covers(range: InputRange) = from <= range.start && (until == null || until >= range.end)
    val semanticId: String get() = InputRevision.hash("context-assertion") {
        text(key.name); text(subject.kind.name); text(subject.id); instant(from)
        nullable(until?.toString()); text(origin.name); text(originReference)
        when (val actual = value) {
            is ContextValue.Flag -> { text("flag"); bool(actual.value) }
            is ContextValue.Number -> { text("number"); long(actual.value) }
            is ContextValue.Text -> { text("text"); text(actual.value) }
        }
    }
}

data class ResolvedContext(val value: ContextValue?, val conflict: Boolean, val evidence: List<ContextAssertion>) {
    val flag: Boolean? get() = (value as? ContextValue.Flag)?.value
    val number: Long? get() = (value as? ContextValue.Number)?.value
    val text: String? get() = (value as? ContextValue.Text)?.value
}

data class InputContext(val profileId: String, val revisionId: String, val assertions: List<ContextAssertion>) {
    init {
        require(profileId.isNotBlank() && revisionId.isNotBlank())
        require(assertions.map { it.semanticId }.distinct().size == assertions.size)
        require(assertions.filter { it.subject.kind == ContextSubjectKind.PROFILE }.all { it.subject.id == profileId })
    }
    val semanticRevision: String get() = InputRevision.hash("context-state") {
        text(profileId); val ids = assertions.map { it.semanticId }.sorted(); int(ids.size); ids.forEach(::text)
    }

    fun resolve(key: ContextKey, range: InputRange, subject: ContextSubject? = null): ResolvedContext {
        return select(assertions.filter { it.key == key && it.covers(range) }, subject)
    }

    fun resolveAt(key: ContextKey, at: Instant, subject: ContextSubject? = null): ResolvedContext =
        select(assertions.filter { it.key == key && it.from <= at && (it.until == null || at < it.until) }, subject)

    private fun select(applicable: List<ContextAssertion>, subject: ContextSubject?): ResolvedContext {
        // Explicit concrete context overrides a default only in its declared interval/subject.
        val specific = applicable.filter {
            if (subject != null) it.subject == subject
            else it.subject.kind in setOf(ContextSubjectKind.OPERATION, ContextSubjectKind.JOURNEY, ContextSubjectKind.SERVICE)
        }
        val selected = specific.ifEmpty { applicable.filter { it.subject == ContextSubject(ContextSubjectKind.PROFILE, profileId) } }
        val values = selected.map { it.value }.distinct()
        return ResolvedContext(values.singleOrNull(), values.size > 1, frozen(selected.sortedBy { it.semanticId }))
    }

    fun freeze() = copy(assertions = frozen(assertions.sortedBy { it.semanticId }))
    companion object { fun empty(profileId: String) = InputContext(profileId, "none", emptyList()) }
}

data class ContextNeed(val key: ContextKey, val subject: ContextSubject?, val range: InputRange?, val reason: String,
    val at: Instant? = null) {
    init { require((range == null) != (at == null)) }
    val id: String get() = InputRevision.hash("context-need") {
        text(key.name); nullable(subject?.kind?.name); nullable(subject?.id)
        nullable(range?.start?.toString()); nullable(range?.end?.toString()); nullable(at?.toString()); text(reason)
    }
}

enum class FerryRestKind { REGULAR_DAILY, REDUCED_WEEKLY, REGULAR_WEEKLY }

/** Called only for a concrete relevant assessment, never as a standard per-import questionnaire. */
object InputContextNeeds {
    fun passengerAt(context: InputContext, at: Instant): List<ContextNeed> {
        val service = context.resolveAt(ContextKey.PASSENGER_SERVICE, at)
        val key = when {
            service.value == null -> ContextKey.PASSENGER_SERVICE
            service.text == "REGULAR" && context.resolveAt(ContextKey.LINE_LENGTH_METRES, at).value == null -> ContextKey.LINE_LENGTH_METRES
            else -> return emptyList()
        }
        return listOf(ContextNeed(key, null, null, "RECORDED_PASSENGER_SERVICE_POINT", at))
    }

    fun passenger(context: InputContext, range: InputRange, subject: ContextSubject? = null): List<ContextNeed> {
        val service = context.resolve(ContextKey.PASSENGER_SERVICE, range, subject)
        val key = when {
            service.value == null -> ContextKey.PASSENGER_SERVICE
            service.text == "REGULAR" && context.resolve(ContextKey.LINE_LENGTH_METRES, range, subject).value == null -> ContextKey.LINE_LENGTH_METRES
            else -> return emptyList()
        }
        return listOf(ContextNeed(key, subject, range, "PASSENGER_SERVICE_SCOPE"))
    }

    fun ferry(context: InputContext, range: InputRange, subject: ContextSubject, rest: FerryRestKind): List<ContextNeed> {
        val keys = if (rest == FerryRestKind.REGULAR_WEEKLY)
            listOf(ContextKey.FERRY_SLEEPING_FACILITY, ContextKey.FERRY_SCHEDULED_MINUTES)
        else listOf(ContextKey.FERRY_SLEEPING_FACILITY)
        return frozen(keys.filter { context.resolve(it, range, subject).value == null }
            .map { ContextNeed(it, subject, range, "CONCRETE_FERRY_PREREQUISITE") })
    }

    fun internationalPair(context: InputContext, first: Pair<ContextSubject, InputRange>,
        second: Pair<ContextSubject, InputRange>): List<ContextNeed> {
        val needs = mutableListOf<ContextNeed>()
        for ((subject, range) in listOf(first, second)) {
            for (key in listOf(ContextKey.EMPLOYER_COUNTRY, ContextKey.RESIDENCE_COUNTRY))
                if (context.resolve(key, range).value == null) needs += ContextNeed(key, null, range, "INTERNATIONAL_PAIR_COUNTRY")
            if (context.resolve(ContextKey.REST_START_COUNTRY, range, subject).value == null)
                needs += ContextNeed(ContextKey.REST_START_COUNTRY, subject, range, "CONCRETE_REST_START_COUNTRY")
        }
        return frozen(needs.distinctBy { it.id })
    }
}
