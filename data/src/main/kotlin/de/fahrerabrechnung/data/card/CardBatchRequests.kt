package de.fahrerabrechnung.data.card

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.security.MessageDigest

/** Separate, length-framed domain; never changes a historic single-application request. */
internal object CardBatchRequests {
    fun child(parent: String, application: CardApplication): String {
        require(parent.isNotBlank() && parent.length <= 128)
        return digest { text("card-batch-child-v1"); text(parent); bytes(application.aid) }
    }
    fun fingerprint(cardId: String, batch: CardApplicationReadBatch, applications: Map<CardApplication, String>): String = digest {
        text("physical-card-read-v1"); text(cardId)
        for (attempt in batch.applications) {
            bytes(attempt.application.aid); text(attempt.status.name); text(attempt.failure?.name.orEmpty())
            text(attempt.result?.generation?.name.orEmpty()); text(applications[attempt.application].orEmpty())
        }
        val master = batch.masterFiles.associateBy { it.fileId }
        for (id in listOf(0x0002, 0x0005, 0x2F00, 0x2F01, 0x0006)) {
            writeInt(id)
            val file = master[id]; writeBoolean(file != null)
            file?.let { writeBoolean(it.readFailed); writeBoolean(it.bytes != null); it.bytes?.let(::bytes) }
        }
    }
    fun master(cardId: String, fileId: Int, bytes: ByteArray) = digest {
        text("physical-card-master-source-v1"); text(cardId); writeInt(fileId); bytes(bytes)
    }
    private fun digest(action: Framed.() -> Unit): String {
        val buffer = ByteArrayOutputStream()
        Framed(buffer).use { it.action() }
        return MessageDigest.getInstance("SHA-256").digest(buffer.toByteArray()).joinToString("") { "%02x".format(it) }
    }
    private class Framed(buffer: ByteArrayOutputStream) : DataOutputStream(buffer) {
        fun bytes(value: ByteArray) { writeInt(value.size); write(value) }
        fun text(value: String) = bytes(value.toByteArray(Charsets.UTF_8))
    }
}
