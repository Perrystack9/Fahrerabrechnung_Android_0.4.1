package de.fahrerabrechnung.data

import android.content.Context
import android.database.sqlite.SQLiteException
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.fahrerabrechnung.data.card.*
import de.fahrerabrechnung.data.db.*
import de.fahrerabrechnung.data.security.EncryptedDatabaseFactory
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Fabricated cards and files only. These tests exercise the real encrypted database. */
@RunWith(AndroidJUnit4::class)
class DriverProfileImportInstrumentedTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var passphrase: ByteArray
    private val clock = Clock.fixed(Instant.parse("2026-10-01T12:00:00Z"), ZoneOffset.UTC)

    @Before fun prepare() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(AppDatabase.DATABASE_NAME)
        passphrase = ByteArray(32).also(SecureRandom()::nextBytes)
        db = EncryptedDatabaseFactory.create(context, passphrase)
    }

    @After fun close() {
        if (::db.isInitialized && db.isOpen) db.close()
        context.deleteDatabase(AppDatabase.DATABASE_NAME)
    }

    private fun activities() = CardActivityImportRepository(db.cardActivityImportDao(), clock)
    private fun vehicles() = CardVehicleImportRepository(db.cardVehicleImportDao(), clock)
    private fun pairs() = CardSourcePairRepository(db, activities(), vehicles(), clock)
    private fun imports() = DriverCardImportRepository(db, activities(), vehicles(), pairs(), clock)

    private fun card(number: String = "synthetic-own-card", holder: DriverIdentity = ownHolder()) =
        DriverCard(CardGeneration.GEN2V2, CardIdentity(number, 13, "Fixture",
            Instant.EPOCH, Instant.EPOCH, Instant.parse("2030-01-01T00:00:00Z")), holder)

    private fun ownHolder() = DriverIdentity("ALPHA", "TEST PERSON", LocalDate.of(2000, 2, 29), "de")

    private fun read(card: DriverCard = card(), plate: String = "TEST OWN"): CardReadResult {
        val epoch = Instant.parse("2026-09-28T00:00:00Z").epochSecond.toInt()
        val day = ByteBuffer.allocate(14).putShort(0).putShort(14).putInt(epoch)
            .putShort(1).putShort(0).putShort(0).array()
        val record = ByteBuffer.allocate(48).put(byteArrayOf(0, 0, 1, 0, 0, 2))
            .putInt(epoch + 3600).putInt(epoch + 7200).put(13.toByte()).put(1.toByte())
            .put(plate.padEnd(13).toByteArray()).putShort(1)
            .put("WDB12345678901234".toByteArray()).array()
        return CardReadResult(CardRecognition.DRIVER, card.generation, card,
            CardActivityFile(0, 0, day), CardVehiclesFile(0, 1, card.generation, record))
    }

    private suspend inline fun <reified T : Throwable> rejected(noinline action: suspend () -> Unit) {
        try { action() } catch (failure: Throwable) {
            if (failure is T) return
            throw failure
        }
        throw AssertionError("Expected ${T::class.java.simpleName}")
    }

    private fun assertPair(expected: SavedCardSourcePair, actual: SavedCardSourcePair) {
        assertEquals(expected.id, actual.id)
        assertEquals(expected.savedAtEpochMillis, actual.savedAtEpochMillis)
        assertEquals(expected.activity.cardNumber, actual.activity.cardNumber)
        assertEquals(expected.activity.importedAtEpochMillis, actual.activity.importedAtEpochMillis)
        assertArrayEquals(expected.activity.file.dailyRecords, actual.activity.file.dailyRecords)
        assertEquals(expected.vehicles.cardNumber, actual.vehicles.cardNumber)
        assertEquals(expected.vehicles.importedAtEpochMillis, actual.vehicles.importedAtEpochMillis)
        assertArrayEquals(expected.vehicles.file.records, actual.vehicles.file.records)
    }

    private fun assertImport(expected: SavedDriverImport, actual: SavedDriverImport) {
        assertEquals(expected.id, actual.id)
        assertEquals(expected.profileId, actual.profileId)
        assertEquals(expected.driverCardId, actual.driverCardId)
        assertEquals(expected.savedAtEpochMillis, actual.savedAtEpochMillis)
        assertEquals(expected.newSourceCount, actual.newSourceCount)
        assertPair(expected.pair!!, actual.pair!!)
    }

    @Test fun firstCardCannotBindOrSaveWithoutExplicitConfirmation() = runBlocking {
        val repository = imports()
        assertEquals(CardBindingKind.FIRST_CARD, repository.review(card()).kind)
        rejected<CardBindingRequired> { repository.bind(card()) }
        rejected<CardBindingRequired> { repository.save(read(), "first-read", CardImportSelection.PAIR) }
        assertNull(repository.profile())
        assertTrue(repository.all().isEmpty())
        assertTrue(db.cardActivityImportDao().all().isEmpty())
        assertTrue(db.cardVehicleImportDao().all().isEmpty())
        assertTrue(pairs().all().isEmpty())
    }

    @Test fun confirmedFirstCardCreatesIndependentStableIdsAndRebindingIsIdempotent() = runBlocking {
        val repository = imports()
        val cardId = repository.bind(card(), true)
        val profile = repository.profile()!!
        assertNotEquals(card().card.number, profile.id)
        assertNotEquals(profile.id, cardId)
        assertNotEquals(card().card.number, cardId)
        assertEquals(cardId, repository.bind(card()))
        assertEquals(CardBindingKind.KNOWN_CARD, repository.review(card()).kind)
        assertEquals(card().card.number, db.driverProfileDao().cards(profile.id).single().cardNumber)
        assertTrue(repository.all().isEmpty())
    }

    @Test fun exactHolderReplacementAddsCardAndKeepsOriginalCardAndImport() = runBlocking {
        val repository = imports()
        val oldCardId = repository.bind(card(), true)
        val original = repository.save(read(), "old-read", CardImportSelection.PAIR)
        val originalIdentity = db.driverProfileDao().cards(original.profileId).single()
        val replacement = card("synthetic-replacement")
        assertEquals(CardBindingKind.SAME_HOLDER, repository.review(replacement).kind)
        val newer = repository.save(read(replacement), "new-read", CardImportSelection.PAIR)
        assertEquals(original.profileId, newer.profileId)
        assertNotEquals(oldCardId, newer.driverCardId)
        assertEquals(2, db.driverProfileDao().cards(original.profileId).size)
        assertEquals(originalIdentity, db.driverProfileDao().cards(original.profileId).first { it.cardId == oldCardId })
        assertImport(original, repository.all().first { it.id == original.id })
        assertEquals("synthetic-own-card", original.activity!!.cardNumber)
        assertEquals("synthetic-replacement", newer.activity!!.cardNumber)
    }

    @Test fun uncertainReplacementRequiresItsOwnConfirmationAndPreservesEarlierIdentity() = runBlocking {
        val repository = imports()
        repository.bind(card(), true)
        val profileId = repository.profile()!!.id
        val original = db.driverProfileDao().cards(profileId).single()
        val replacement = card("synthetic-uncertain", ownHolder().copy(birthDate = null))
        assertEquals(CardBindingKind.CONFIRM_REPLACEMENT, repository.review(replacement).kind)
        rejected<CardBindingRequired> { repository.bind(replacement) }
        rejected<CardBindingRequired> { repository.save(read(replacement), "uncertain", CardImportSelection.PAIR) }
        assertEquals(listOf(original), db.driverProfileDao().cards(profileId))
        repository.bind(replacement, true)
        assertEquals(profileId, repository.profile()!!.id)
        assertEquals(2, db.driverProfileDao().cards(profileId).size)
        assertEquals(original, db.driverProfileDao().cards(profileId).first { it.cardId == original.cardId })
        assertEquals(CardBindingKind.KNOWN_CARD, repository.review(replacement).kind)
    }

    @Test fun foreignCardIsRejectedEvenWithConfirmationAndCannotEnterAnySavedHistory() = runBlocking {
        val repository = imports()
        repository.bind(card(), true)
        val own = repository.save(read(), "own", CardImportSelection.PAIR)
        val foreign = card("synthetic-foreign", DriverIdentity("BETA", "OTHER PERSON", LocalDate.of(1980, 1, 1), "en"))
        assertEquals(CardBindingKind.FOREIGN_CARD, repository.review(foreign).kind)
        rejected<ForeignCardRejected> { repository.bind(foreign, true) }
        for (selection in CardImportSelection.entries) {
            rejected<ForeignCardRejected> { repository.save(read(foreign, "TEST FOREIGN"), "foreign-$selection", selection) }
        }
        assertEquals(listOf(own.id), repository.all().map { it.id })
        assertImport(own, repository.all().single())
        assertEquals(1, db.driverProfileDao().cards(own.profileId).size)
        assertEquals(1, db.cardActivityImportDao().all().size)
        assertEquals(1, db.cardVehicleImportDao().all().size)
        assertEquals(listOf("TEST OWN"), repository.vehicleSources().flatMap { it.uses }.map { it.registrationNumber })
    }

    @Test fun sameCardKeyWithChangedHolderCannotOverwriteAnExistingAssignment() = runBlocking {
        val repository = imports()
        repository.bind(card(), true)
        val profileId = repository.profile()!!.id
        val original = db.driverProfileDao().cards(profileId).single()
        val changed = card().copy(holder = ownHolder().copy(surname = "CHANGED"))
        assertEquals(CardBindingKind.FOREIGN_CARD, repository.review(changed).kind)
        rejected<ForeignCardRejected> { repository.bind(changed, true) }
        assertEquals(listOf(original), db.driverProfileDao().cards(profileId))
    }

    @Test fun repeatedReadsDeduplicateFilesButRetainSeparateSessionsAndIdempotentRetry() = runBlocking {
        val repository = imports()
        repository.bind(card(), true)
        val first = repository.save(read(), "read-1", CardImportSelection.PAIR)
        val second = repository.save(read(), "read-2", CardImportSelection.PAIR)
        assertEquals(2, first.newSourceCount)
        assertEquals(0, second.newSourceCount)
        assertImport(second, repository.save(read(), "read-2", CardImportSelection.PAIR))
        rejected<IllegalStateException> { repository.save(read(), "read-2", CardImportSelection.ACTIVITY) }
        assertEquals(2, repository.all().size)
        assertEquals(1, db.cardActivityImportDao().all().size)
        assertEquals(1, db.cardVehicleImportDao().all().size)
        assertEquals(1, pairs().all().size)
        assertEquals("PARTIAL", db.driverProfileDao().session(first.id, first.profileId)!!.status)
    }

    @Test fun selectedSingleSourcesNeverInventAPairOrBorrowTheOtherFile() = runBlocking {
        val repository = imports()
        repository.bind(card(), true)
        val activity = repository.save(read().copy(vehicles = null, vehicleReadFailed = true), "activity", CardImportSelection.ACTIVITY)
        assertNotNull(activity.activity)
        assertNull(activity.vehicles)
        assertNull(activity.pair)
        val vehicle = repository.save(read().copy(activity = null), "vehicle", CardImportSelection.VEHICLES)
        assertNull(vehicle.activity)
        assertNotNull(vehicle.vehicles)
        assertNull(vehicle.pair)
        assertEquals(1, activity.newSourceCount)
        assertEquals(1, vehicle.newSourceCount)
        assertTrue(pairs().all().isEmpty())
    }

    @Test fun importLinkFailureRollsBackNewCardSessionBothFilesAndPairTogether() = runBlocking {
        val repository = imports()
        repository.bind(card(), true)
        val old = repository.save(read(), "old", CardImportSelection.PAIR)
        db.openHelper.writableDatabase.execSQL("""CREATE TRIGGER reject_import BEFORE INSERT ON driver_import_sources BEGIN SELECT RAISE(ABORT, 'synthetic failure'); END""")
        val replacement = card("synthetic-replacement")
        rejected<SQLiteException> { repository.save(read(replacement, "TEST NEW"), "new", CardImportSelection.PAIR) }
        assertEquals(listOf(old.id), repository.all().map { it.id })
        assertImport(old, repository.all().single())
        assertNull(db.driverProfileDao().session("new", old.profileId))
        assertEquals(1, db.driverProfileDao().cards(old.profileId).size)
        assertEquals(1, db.cardActivityImportDao().all().size)
        assertEquals(1, db.cardVehicleImportDao().all().size)
        assertEquals(1, pairs().all().size)
        db.openHelper.writableDatabase.execSQL("DROP TRIGGER reject_import")
        assertEquals(CardBindingKind.SAME_HOLDER, repository.review(replacement).kind)
    }

    @Test fun malformedOrFailedSelectedFileCannotPartiallyBindOrSave() = runBlocking {
        val repository = imports()
        repository.bind(card(), true)
        val profileId = repository.profile()!!.id
        val replacementRead = read(card("synthetic-replacement"))
        rejected<ParseError> { repository.save(replacementRead.copy(vehicles = CardVehiclesFile(0, 1, CardGeneration.GEN2V2, byteArrayOf(1))), "bad", CardImportSelection.PAIR) }
        rejected<IllegalArgumentException> { repository.save(replacementRead.copy(vehicleReadFailed = true), "failed", CardImportSelection.PAIR) }
        rejected<IllegalArgumentException> { repository.save(replacementRead.copy(recognition = CardRecognition.OTHER_CARD), "other", CardImportSelection.PAIR) }
        assertEquals(1, db.driverProfileDao().cards(profileId).size)
        assertTrue(repository.all().isEmpty())
        assertTrue(db.cardActivityImportDao().all().isEmpty())
        assertTrue(db.cardVehicleImportDao().all().isEmpty())
    }

    @Test fun legacyFilesAreNotGuessedIntoProfileAndFreshReadMayReuseTheirBytes() = runBlocking {
        val legacyRead = read()
        pairs().save(legacyRead)
        val legacyPair = pairs().all().single()
        val repository = imports()
        repository.bind(card(), true)
        assertTrue(repository.all().isEmpty())
        assertTrue(repository.vehicleSources().isEmpty())
        assertPair(legacyPair, pairs().all().single())
        val fresh = repository.save(legacyRead, "fresh-read", CardImportSelection.PAIR)
        assertEquals(0, fresh.newSourceCount)
        assertEquals(legacyPair.id, fresh.pair!!.id)
        assertPair(legacyPair, pairs().all().single())
        assertEquals(listOf(fresh.id), repository.all().map { it.id })
    }

    @Test fun bindingsCardsAndExactPartialImportReopenFromEncryptedDatabase() = runBlocking {
        val repository = imports()
        repository.bind(card(), true)
        val original = repository.save(read(), "saved", CardImportSelection.PAIR)
        val profile = repository.profile()
        db.close()
        db = EncryptedDatabaseFactory.create(context, passphrase)
        assertEquals(profile, imports().profile())
        assertEquals(CardBindingKind.KNOWN_CARD, imports().review(card()).kind)
        assertImport(original, imports().all().single())
        assertEquals("TEST OWN", imports().vehicleSources().single().uses.single().registrationNumber)
    }

    @Test fun migrationFourToFivePreservesArchiveAndOldRowsWithoutInventingBindings() = runBlocking {
        pairs().save(read())
        val archive = pairs().all().single()
        db.profileDao().insert(ProfileEntity("old-profile", "Legacy fixture", 1, 1))
        db.driverProfileDao().insertCard(DriverCardEntity("old-card", "old-profile", "legacy-id", "UNASSIGNED", 1, 1))
        db.driverProfileDao().insertSession(ImportSessionEntity("old-session", "old-profile", "LEGACY", 1, 1))
        db.openHelper.writableDatabase.apply {
            execSQL("DROP TABLE driver_import_sources")
            execSQL("DROP TABLE driver_card_identities")
            execSQL("DROP TABLE local_driver_profile")
            execSQL("DELETE FROM room_master_table")
            version = 4
        }
        db.close()
        db = EncryptedDatabaseFactory.create(context, passphrase)
        assertEquals(AppDatabase.SCHEMA_VERSION, db.openHelper.writableDatabase.version)
        assertPair(archive, pairs().all().single())
        assertEquals("Legacy fixture", db.profileDao().getById("old-profile")!!.name)
        assertEquals("legacy-id", db.driverProfileDao().card("old-card")!!.cardIdentifier)
        assertEquals("LEGACY", db.driverProfileDao().session("old-session", "old-profile")!!.status)
        assertNull(imports().profile())
        assertTrue(imports().all().isEmpty())
        assertTrue(db.driverProfileDao().cards("old-profile").isEmpty())
        assertEquals(CardBindingKind.FIRST_CARD, imports().review(card()).kind)
    }

    @Test fun supportedCardGenerationsUseTheSameExplicitBindingRules() = runBlocking {
        val repository = imports()
        val gen1 = card().copy(generation = CardGeneration.GEN1)
        rejected<CardBindingRequired> { repository.bind(gen1) }
        repository.bind(gen1, true)
        for (generation in listOf(CardGeneration.GEN2, CardGeneration.GEN2V2)) {
            val next = card("synthetic-${generation.name}").copy(generation = generation)
            assertEquals(CardBindingKind.SAME_HOLDER, repository.review(next).kind)
            repository.bind(next)
        }
        assertEquals(3, db.driverProfileDao().cards(repository.profile()!!.id).size)
    }
}
