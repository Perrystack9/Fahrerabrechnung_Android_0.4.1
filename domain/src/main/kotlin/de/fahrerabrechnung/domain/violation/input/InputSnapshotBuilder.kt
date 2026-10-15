package de.fahrerabrechnung.domain.violation.input

object InputSnapshotBuilder {
    fun build(raw: InputSources, suppliedContext: InputContext,
        calendar: ScopeCalendar = ScopeCalendar(), reviewed: List<ReviewedScopeAdjustment> = emptyList(),
        checkCancellation: () -> Unit = {}): ViolationInputSnapshot {
        require(raw.profileId == suppliedContext.profileId)
        val cards = raw.ownCardIds.distinct().sorted()
        require(raw.sources.all { it.key.profileId == raw.profileId && it.key.cardId in cards })
        val sourceGroups = raw.sources.groupBy { it.key.stableKey }
        require(sourceGroups.values.all { group -> group.distinct().size == 1 })
        require(raw.activities.all { it.source in raw.sources })
        require(raw.points.all { point -> raw.sources.any { it.key == point.source } })
        val sources = raw.sources.distinct().sortedBy { it.key.stableKey }.map { source ->
            source.copy(files = frozen(source.files.sortedBy { it.family.name + it.ef }),
                receipts = frozen(source.receipts.sortedBy { it.ef.toString() + it.fingerprint }),
                metadataDigests = frozenMap(source.metadataDigests.toSortedMap()))
        }
        val input = raw.copy(ownCardIds = frozen(cards), sources = frozen(sources),
            activities = frozen(raw.activities.map { activity -> activity.copy(days = frozen(activity.days.map { day ->
                day.copy(changes = frozen(day.changes)) })) }),
            points = frozen(raw.points.map { it.copy(fields = frozenMap(it.fields.toSortedMap())) }),
            issues = frozen(raw.issues), manifest = frozenMap(raw.manifest.toSortedMap()))
        val context = suppliedContext.freeze()
        checkCancellation()
        val prepared = ActivityIntervals.prepare(input.activities, checkCancellation)
        val points = input.points.sortedWith(compareBy({ it.timeUtc }, { it.family.name }, { it.source.stableKey }, { it.recordIndex }))
        val range = prepared.observations.firstOrNull()?.at?.let { start ->
            prepared.observations.lastOrNull()?.at?.takeIf { start < it }?.let { InputRange(start, it) }
        }
        val scopes = range?.let { ScopeGuard.evaluate(it, context, calendar, reviewed) }.orEmpty()
        val needs = scopes.flatMap { it.needs } + points.filter { it.family == InputFamily.LOAD_TYPE && it.fields["loadTypeRaw"] == "2" }
            .flatMap { it.timeUtc?.let { at -> InputContextNeeds.passengerAt(context, at) }.orEmpty() }
        val issues = (input.issues + prepared.issues).distinct().sortedWith(compareBy(
            { it.code.name }, { it.source?.stableKey }, { it.at ?: it.range?.start }, { it.sessionId }, { it.cardId },
            { it.batchId }, { it.application }))
        val dataRevision = InputRevision.hash("facts") {
            text(input.profileId); int(prepared.intervals.size)
            prepared.intervals.forEach { instant(it.range.start); instant(it.range.end); meaning(it.meaning) }
            val observations = prepared.observations.map { point -> InputRevision.hash("activity-observation-fact") {
                instant(point.at); meaning(point.meaning)
                val states = point.rawChanges.map { change -> InputRevision.hash("observed-state") {
                    meaning(ActivityIntervals.meaning(change))
                } }.distinct().sorted()
                int(states.size); states.forEach(::text)
            } }.distinct().sorted()
            int(observations.size); observations.forEach(::text)
            val pointFacts = points.map { point -> InputRevision.hash("point-fact") {
                text(point.family.name); text(point.source.generation)
                nullable(point.timeUtc?.toString()); int(point.fields.size)
                point.fields.toSortedMap().forEach { (key, value) -> text(key); text(value) }
            } }.distinct().sorted()
            int(pointFacts.size); pointFacts.forEach(::text)
        }
        val provenance = InputRevision.provenance(input)
        val revision = InputRevision.hash("snapshot") {
            text(INPUT_INTERPRETATION); text(ScopeGuard.INTERPRETATION)
            text(dataRevision); text(provenance); text(context.revisionId); text(context.semanticRevision)
            nullable(calendar.zoneId); nullable(calendar.evidenceId)
            val adjustments = reviewed.map { entry -> InputRevision.hash("reviewed-scope") {
                instant(entry.range.start); instant(entry.range.end); text(entry.country); text(entry.legalArticle)
                text(entry.sourceUri); text(entry.sourceDigest); text(entry.reviewId); bool(entry.exemption)
                val articles = entry.articles.map { it.name }.sorted(); int(articles.size); articles.forEach(::text)
            } }.sorted()
            int(adjustments.size); adjustments.forEach(::text)
        }
        checkCancellation()
        return ViolationInputSnapshot(input.profileId, frozen(cards), frozen(sources), prepared.intervals,
            prepared.observations, frozen(points), frozen(issues), context, frozen(scopes),
            frozen(needs.distinctBy { it.id }.sortedBy { it.id }), input.manifest, dataRevision, provenance, revision)
    }
}
