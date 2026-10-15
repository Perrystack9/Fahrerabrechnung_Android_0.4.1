package de.fahrerabrechnung.domain.violation.input

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class ScopeArticle { DRIVING_6, BREAK_7, DAILY_REST_8, WEEKLY_REST_8, FERRY_9 }
enum class ScopeStatus { APPLICABLE, NOT_APPLICABLE, UNKNOWN, UNSUPPORTED }
enum class LegalActivityHint { NONE, OTHER_WORK, UNDETERMINED }

/** Supplied by reviewed rule metadata, never the freely selected display zone. Null stays unknown. */
data class ScopeCalendar(val zoneId: String? = null, val evidenceId: String? = null) {
    init { require((zoneId == null) == (evidenceId == null)); if (zoneId != null) { ZoneId.of(zoneId); require(!evidenceId.isNullOrBlank()) } }
}

/** No user-facing editor registers exceptions. These come from a separately reviewed manifest. */
data class ReviewedScopeAdjustment(
    val range: InputRange, val country: String, val articles: Set<ScopeArticle>,
    val legalArticle: String, val sourceUri: String, val sourceDigest: String,
    val reviewId: String, val exemption: Boolean,
) {
    init {
        require(country.matches(Regex("[A-Z]{2}")) && articles.isNotEmpty())
        require(legalArticle in setOf("3", "11", "13", "14"))
        require(sourceUri.startsWith("https://") && sourceDigest.matches(Regex("[a-f0-9]{64}")) && reviewId.isNotBlank())
        require(legalArticle != "11" || !exemption)
    }
}

data class ScopeDecision(val article: ScopeArticle, val status: ScopeStatus, val legalBasis: String,
    val reason: String, val contextEvidence: List<String>, val activityHint: LegalActivityHint)
data class ScopeSegment(val range: InputRange, val decisions: List<ScopeDecision>, val needs: List<ContextNeed>)

object ScopeGuard {
    const val INTERPRETATION = "561-minimal-input-scope-1"

    fun evaluate(range: InputRange, context: InputContext, calendar: ScopeCalendar = ScopeCalendar(),
        reviewed: List<ReviewedScopeAdjustment> = emptyList()): List<ScopeSegment> {
        val boundaries = sortedSetOf(range.start, range.end)
        context.assertions.forEach { entry ->
            if (entry.from > range.start && entry.from < range.end) boundaries += entry.from
            entry.until?.takeIf { it > range.start && it < range.end }?.let(boundaries::add)
        }
        reviewed.forEach { entry ->
            if (entry.range.start > range.start && entry.range.start < range.end) boundaries += entry.range.start
            if (entry.range.end > range.start && entry.range.end < range.end) boundaries += entry.range.end
        }
        calendar.zoneId?.let { zone ->
            LocalDate.of(2026, 7, 1).atStartOfDay(ZoneId.of(zone)).toInstant()
                .takeIf { it > range.start && it < range.end }?.let(boundaries::add)
        }
        return frozen(boundaries.toList().zipWithNext().map { (start, end) ->
            val part = InputRange(start, end)
            val evidence = linkedSetOf<String>()
            val needs = mutableListOf<ContextNeed>()
            fun read(key: ContextKey): ResolvedContext = context.resolve(key, part).also {
                evidence += it.evidence.map(ContextAssertion::semanticId)
                if (it.value == null) needs += ContextNeed(key, null, part,
                    if (it.conflict) "CONTEXT_CONFLICT" else "MISSING_SCOPE_PREREQUISITE")
            }
            fun result(status: ScopeStatus, legal: String, reason: String) = Triple(status, legal, reason)
            val road = read(ContextKey.ROAD_CARRIAGE)
            val area by lazy { read(ContextKey.JOURNEY_AREA) }
            val type by lazy { read(ContextKey.TRANSPORT_KIND) }
            val base = when {
                road.flag == false -> result(ScopeStatus.NOT_APPLICABLE, "561/2006 Art.4(a)", "NOT_ROAD_CARRIAGE")
                area.text == "AETR_MIXED" -> result(ScopeStatus.UNSUPPORTED, "561/2006 Art.2(3)", "AETR_WHOLE_JOURNEY")
                road.flag != true || area.text == null || type.text == null -> result(ScopeStatus.UNKNOWN, "561/2006 Art.2/4(a)", "MISSING_OR_CONFLICTING_CONTEXT")
                area.text != "EU_ONLY" -> result(ScopeStatus.UNKNOWN, "561/2006 Art.2(2)", "CONCRETE_JURISDICTION_NOT_REVIEWED")
                type.text == "PASSENGERS" -> {
                    val capacity = read(ContextKey.PASSENGER_CAPACITY).number
                    val service by lazy { read(ContextKey.PASSENGER_SERVICE).text }
                    when {
                        capacity == null -> result(ScopeStatus.UNKNOWN, "561/2006 Art.2(1)(b)", "MISSING_PASSENGER_CAPACITY")
                        capacity <= 9 -> result(ScopeStatus.NOT_APPLICABLE, "561/2006 Art.2(1)(b)", "AT_MOST_NINE_PERSONS")
                        service == "OCCASIONAL" -> result(ScopeStatus.APPLICABLE, "561/2006 Art.2(1)(b)", "OCCASIONAL_PASSENGERS")
                        service != "REGULAR" -> result(ScopeStatus.UNKNOWN, "561/2006 Art.3(a)", "MISSING_PASSENGER_SERVICE")
                        else -> {
                            val line = read(ContextKey.LINE_LENGTH_METRES).number
                            when {
                                line == null -> result(ScopeStatus.UNKNOWN, "561/2006 Art.3(a)", "MISSING_LINE_LENGTH")
                                line <= 50_000 -> result(ScopeStatus.NOT_APPLICABLE, "561/2006 Art.3(a)", "REGULAR_LINE_AT_MOST_50_KM")
                                else -> result(ScopeStatus.APPLICABLE, "561/2006 Art.2(1)(b)/3(a)", "REGULAR_LINE_OVER_50_KM")
                            }
                        }
                    }
                }
                else -> {
                    val mass = read(ContextKey.MAX_MASS_KG).number
                    when {
                        mass == null -> result(ScopeStatus.UNKNOWN, "561/2006 Art.2(1)", "MISSING_MAX_MASS")
                        mass > 3500 -> result(ScopeStatus.APPLICABLE, "561/2006 Art.2(1)(a)", "GOODS_OVER_3500_KG")
                        mass <= 2500 -> result(ScopeStatus.NOT_APPLICABLE, "561/2006 Art.2(1)(a)/(aa)", "GOODS_AT_MOST_2500_KG")
                        else -> {
                            val journey = read(ContextKey.JOURNEY_KIND).text
                            val date = calendar.zoneId?.let { start.atZone(ZoneId.of(it)).toLocalDate() }
                            when {
                                journey == "DOMESTIC" -> result(ScopeStatus.NOT_APPLICABLE, "561/2006 Art.2(1)(aa)", "SMALL_GOODS_DOMESTIC")
                                journey == null || date == null -> result(ScopeStatus.UNKNOWN, "561/2006 Art.2(1)(aa)", "MISSING_JOURNEY_OR_LEGAL_DATE")
                                date < LocalDate.of(2026, 7, 1) -> result(ScopeStatus.NOT_APPLICABLE, "561/2006 Art.2(1)(aa)", "BEFORE_SCOPE_EXTENSION")
                                else -> {
                                    val own = read(ContextKey.OWN_ACCOUNT).flag
                                    val main by lazy { read(ContextKey.DRIVING_MAIN_ACTIVITY).flag }
                                    when {
                                        own == false -> result(ScopeStatus.APPLICABLE, "561/2006 Art.2(1)(aa)", "INTERNATIONAL_GOODS_OVER_2500_KG")
                                        own == true && main == false -> result(ScopeStatus.NOT_APPLICABLE, "561/2006 Art.3(ha)", "OWN_ACCOUNT_NOT_MAIN_DRIVING")
                                        main == true -> {
                                            needs.removeAll { it.key == ContextKey.OWN_ACCOUNT }
                                            result(ScopeStatus.APPLICABLE, "561/2006 Art.2(1)(aa)", "INTERNATIONAL_GOODS_OVER_2500_KG")
                                        }
                                        else -> result(ScopeStatus.UNKNOWN, "561/2006 Art.3(ha)", "OWN_ACCOUNT_EXCEPTION_UNDETERMINED")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            val country = context.resolve(ContextKey.OPERATION_COUNTRY, part).text
            val matches = reviewed.filter { base.first == ScopeStatus.APPLICABLE &&
                it.country == country && it.range.start <= start && it.range.end >= end }
                .sortedWith(compareBy({ it.legalArticle }, { it.reviewId }, { it.sourceDigest }))
            fun hint(status: ScopeStatus) = if (status == ScopeStatus.NOT_APPLICABLE) {
                if (context.resolve(ContextKey.COMMERCIAL_OPERATION, part).flag == true ||
                    context.resolve(ContextKey.TRANSFER_REQUIRES_OTHER_WORK, part).flag == true) LegalActivityHint.OTHER_WORK
                else LegalActivityHint.UNDETERMINED
            } else LegalActivityHint.NONE
            ScopeSegment(part, frozen(ScopeArticle.entries.map { article ->
                val adjustment = matches.filter { article in it.articles }
                val status = when {
                    adjustment.any { !it.exemption } -> ScopeStatus.UNKNOWN
                    adjustment.isNotEmpty() -> ScopeStatus.NOT_APPLICABLE
                    else -> base.first
                }
                ScopeDecision(article, status,
                    adjustment.firstOrNull()?.let { "561/2006 Art.${it.legalArticle}" } ?: base.second,
                    when { adjustment.any { !it.exemption } -> "STRICTER_RULE_NOT_IMPLEMENTED"
                        adjustment.isNotEmpty() -> "REVIEWED_PARTIAL_EXEMPTION"; else -> base.third },
                    frozen((evidence + adjustment.map { it.reviewId }).sorted()), hint(status))
            }), frozen(needs.distinctBy { it.id }))
        })
    }
}
