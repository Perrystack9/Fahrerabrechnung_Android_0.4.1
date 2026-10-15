package de.fahrerabrechnung.data

import de.fahrerabrechnung.data.card.*
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.time.Instant

/** Entirely synthetic; never a real card dump or APDU log. */
internal object M1InputFixture {
    val day: Instant = Instant.parse("2026-09-07T00:00:00Z")
    fun record(vararg words: Int, date: Instant = day, previous: Int = 0): ByteArray =
        ByteBuffer.allocate(12 + 2 * words.size).apply {
            putShort(previous.toShort()); putShort(capacity().toShort()); putInt(date.epochSecond.toInt())
            putShort(1); putShort(0); words.forEach { putShort(it.toShort()) }
        }.array()

    fun read(generation: CardGeneration = CardGeneration.GEN1, vararg words: Int = intArrayOf(0, 0x11e0, 0x0258),
        number: String = "TESTCARD12345678", fullOriginal: Boolean = true): CardReadResult {
        val rawIdentity = ByteArrayOutputStream().apply {
            fun name(value: String) { write(1); write(value.padEnd(35).toByteArray()) }
            write(1); write(number.toByteArray()); name("TEST AUTHORITY")
            write(byteArrayOf(0x65, 0x53, 0xF1.toByte(), 0)); write(byteArrayOf(0x65, 0x53, 0xF1.toByte(), 0))
            write(byteArrayOf(0x75, 0x53, 0xF1.toByte(), 0)); name("MUSTER"); name("ERIKA")
            write(byteArrayOf(0x20, 0, 0x02, 0x29)); write("DE".toByteArray())
        }.toByteArray()
        val card = DriverCardDecoder.identity(rawIdentity, generation)
        val ring = record(*words)
        val original = CardCoreSnapshot(CardCoreEf.ACTIVITY, generation,
            ByteBuffer.allocate(4 + maxOf(ring.size, 14)).putShort(0).putShort(0).put(ring).array())
        val application = ByteArray(if (generation == CardGeneration.GEN1) 10 else if (generation == CardGeneration.GEN2) 15 else 17)
            .also { it[0] = 1; it[1] = 1; if (generation == CardGeneration.GEN2V2) it[2] = 1 }
        return CardReadResult(CardRecognition.DRIVER, generation, card, activity = original.activity(),
            identitySources = mapOf(
                CardIdentitySourceKind.IDENTIFICATION to CardIdentitySourceRead(CardIdentitySourceFile(CardIdentitySourceKind.IDENTIFICATION, generation, rawIdentity)),
                CardIdentitySourceKind.APPLICATION to CardIdentitySourceRead(CardIdentitySourceFile(CardIdentitySourceKind.APPLICATION, generation, application))),
            coreReads = if (fullOriginal) mapOf(CardCoreEf.ACTIVITY to CardCoreRead(CardCoreEf.ACTIVITY, CardCoreReadStatus.COMPLETE, original)) else emptyMap())
    }

    fun batch(vararg words: Int = intArrayOf(0, 0x11e0, 0x0258)) = CardApplicationReadBatch(listOf(
        CardApplicationAttempt.read(CardApplication.GEN1, read(CardGeneration.GEN1, *words)),
        CardApplicationAttempt.read(CardApplication.GEN2, read(CardGeneration.GEN2V2, *words))), emptyList())
}
