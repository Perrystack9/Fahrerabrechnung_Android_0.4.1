package de.fahrerabrechnung.domain.violation.input

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.security.MessageDigest
import java.time.Instant

/** Versioned length-framed UTF-8, never toString(), locale, display zone or device time. */
object InputRevision {
    class Writer internal constructor(private val output: DataOutputStream) {
        fun text(value: String) { val bytes = value.toByteArray(Charsets.UTF_8); output.writeInt(bytes.size); output.write(bytes) }
        fun nullable(value: String?) { output.writeBoolean(value != null); if (value != null) text(value) }
        fun int(value: Int) = output.writeInt(value)
        fun long(value: Long) = output.writeLong(value)
        fun bool(value: Boolean) = output.writeBoolean(value)
        fun instant(value: Instant) { long(value.epochSecond); int(value.nano) }
        fun source(value: InputSourceKey) {
            text(value.profileId); text(value.cardId); text(value.sessionId); nullable(value.sourceId)
            text(value.generation); nullable(value.application); nullable(value.batchId); nullable(value.runId)
        }
        fun meaning(value: ActivityMeaning) {
            text(value.activity.name); text(value.quality.name); text(value.crew.name)
            text(value.slot.name); bool(value.manualTachograph); nullable(value.reason?.name)
        }
    }

    fun hash(kind: String, block: Writer.() -> Unit): String {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { output -> Writer(output).apply { text("violation-input-v1"); text(kind); block() } }
        return digest(bytes.toByteArray())
    }

    fun digest(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(bytes)
        .joinToString("") { (it.toInt() and 255).toString(16).padStart(2, '0') }

    fun provenance(input: InputSources): String = hash("provenance") {
        text(input.profileId)
        val cards = input.ownCardIds.distinct().sorted(); int(cards.size); cards.forEach(::text)
        val sources = input.sources.sortedBy { it.key.stableKey }; int(sources.size)
        for (source in sources) {
            this.source(source.key)
            val files = source.files.sortedWith(compareBy({ it.family.name }, { it.ef })); int(files.size)
            files.forEach { text(it.family.name); nullable(it.ef?.toString()); text(it.availability.name)
                nullable(it.sourceId); nullable(it.contentDigest); nullable(it.completeOriginalId); nullable(it.completeOriginalDigest) }
            val receipts = source.receipts.sortedWith(compareBy({ it.ef }, { it.fingerprint })); int(receipts.size)
            receipts.forEach { text(it.family); int(it.ef); text(it.fingerprint); nullable(it.originalSourceId)
                nullable(it.certificateSourceId); text(it.captureStatus); nullable(it.originalDigest)
                nullable(it.signatureDigest); nullable(it.certificateDigest); nullable(it.issuerSourceId); nullable(it.issuerDigest) }
            val metadata = source.metadataDigests.toSortedMap(); int(metadata.size)
            metadata.forEach { (key, value) -> text(key); text(value) }
        }
        val manifest = input.manifest.toSortedMap(); int(manifest.size)
        manifest.forEach { (key, value) -> text(key); text(value) }
    }
}
