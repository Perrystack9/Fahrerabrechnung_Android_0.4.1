package de.fahrerabrechnung.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.fahrerabrechnung.core.design.FahrerTagebuchTheme
import de.fahrerabrechnung.core.model.ThemeMode
import de.fahrerabrechnung.data.card.*
import de.fahrerabrechnung.data.db.AppDatabase
import java.nio.ByteBuffer
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** No USB or real card fixtures: confirmations and visible source boundaries. */
class DriverProfileBindingTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var db: AppDatabase
    private lateinit var activities: CardActivityImportRepository
    private lateinit var vehicles: CardVehicleImportRepository
    private lateinit var pairs: CardSourcePairRepository
    private lateinit var imports: DriverCardImportRepository

    @Before fun prepare() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java).build()
        val clock = Clock.fixed(Instant.parse("2026-10-01T12:00:00Z"), ZoneOffset.UTC)
        activities = CardActivityImportRepository(db.cardActivityImportDao(), clock)
        vehicles = CardVehicleImportRepository(db.cardVehicleImportDao(), clock)
        pairs = CardSourcePairRepository(db, activities, vehicles, clock)
        imports = DriverCardImportRepository(db, activities, vehicles, pairs, clock)
    }

    @After fun close() { db.close() }

    private fun card(number: String = "synthetic-own", holder: DriverIdentity =
        DriverIdentity("ALPHA", "TEST PERSON", LocalDate.of(2000, 2, 29), "de")) =
        DriverCard(CardGeneration.GEN2V2, CardIdentity(number, 13, "Fixture", Instant.EPOCH,
            Instant.EPOCH, Instant.parse("2030-01-01T00:00:00Z")), holder)

    private fun read(card: DriverCard = card(), plate: String = "TEST OWN", date: String = "2026-09-28T00:00:00Z"): CardReadResult {
        val epoch = Instant.parse(date).epochSecond.toInt()
        val day = ByteBuffer.allocate(14).putShort(0).putShort(14).putInt(epoch)
            .putShort(1).putShort(0).putShort(0).array()
        val record = ByteBuffer.allocate(48).put(byteArrayOf(0, 0, 1, 0, 0, 2))
            .putInt(epoch + 3600).putInt(epoch + 7200).put(13.toByte()).put(1.toByte())
            .put(plate.padEnd(13).toByteArray()).putShort(1)
            .put("WDB12345678901234".toByteArray()).array()
        return CardReadResult(CardRecognition.DRIVER, CardGeneration.GEN2V2, card,
            CardActivityFile(0, 0, day), CardVehiclesFile(0, 1, CardGeneration.GEN2V2, record))
    }

    private fun panel(card: DriverCard) {
        compose.setContent {
            FahrerTagebuchTheme(ThemeMode.LIGHT) {
                Column { DriverProfileBindingPanel(card, imports) {} }
            }
        }
    }

    private fun awaitText(text: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty() }
    }

    @Test fun firstCardCancelWritesNothingAndConfirmationBindsOnlyAfterClick() {
        panel(card())
        awaitText("Eigene Fahrerkarte zuordnen")
        assertNull(runBlocking { imports.profile() })
        compose.onNodeWithTag("request_card_binding").performClick()
        compose.onNodeWithText("Abbrechen").performClick()
        assertNull(runBlocking { imports.profile() })
        compose.onNodeWithTag("request_card_binding").performClick()
        compose.onNodeWithTag("confirm_own_card").performClick()
        awaitText("Diese Karte ist deinem Fahrerprofil zugeordnet.")
        assertEquals(CardBindingKind.KNOWN_CARD, runBlocking { imports.review(card()).kind })
        assertTrue(runBlocking { imports.all().isEmpty() })
    }

    @Test fun uncertainReplacementHasItsOwnConfirmationAndKeepsOriginalCard() {
        val profileId = runBlocking { imports.bind(card(), true); imports.profile()!!.id }
        val original = runBlocking { db.driverProfileDao().cards(profileId).single() }
        val replacement = card("synthetic-new", card().holder.copy(birthDate = null))
        panel(replacement)
        awaitText("Eigene Folge-/Ersatzkarte bestätigen")
        assertEquals(1, runBlocking { db.driverProfileDao().cards(profileId).size })
        compose.onNodeWithTag("request_card_binding").performClick()
        compose.onNodeWithTag("confirm_own_card").performClick()
        awaitText("Diese Karte ist deinem Fahrerprofil zugeordnet.")
        assertEquals(2, runBlocking { db.driverProfileDao().cards(profileId).size })
        assertEquals(original, runBlocking { db.driverProfileDao().cards(profileId).first { it.cardId == original.cardId } })
    }

    @Test fun foreignCardOffersNoBindingAndLeavesProfileAndImportsUntouched() {
        val profileId = runBlocking { imports.bind(card(), true); imports.profile()!!.id }
        val foreign = card("synthetic-foreign", DriverIdentity("BETA", "OTHER PERSON", LocalDate.of(1980, 1, 1), "en"))
        panel(foreign)
        awaitText("Nur temporäre Vorschau")
        compose.onNodeWithTag("request_card_binding").assertDoesNotExist()
        compose.onNodeWithTag("confirm_own_card").assertDoesNotExist()
        assertFalse(runBlocking { imports.review(foreign).canSave })
        assertEquals(profileId, runBlocking { imports.profile()!!.id })
        assertEquals(1, runBlocking { db.driverProfileDao().cards(profileId).size })
        assertTrue(runBlocking { imports.all().isEmpty() })
    }

    @Test fun reliableReplacementPreviewDoesNotWriteUntilExplicitSave() {
        val profileId = runBlocking { imports.bind(card(), true); imports.profile()!!.id }
        val replacement = card("synthetic-next")
        panel(replacement)
        awaitText("Beim Speichern wird diese Karte")
        compose.onNodeWithTag("request_card_binding").assertDoesNotExist()
        assertTrue(runBlocking { imports.review(replacement).canSave })
        assertEquals(1, runBlocking { db.driverProfileDao().cards(profileId).size })
        assertTrue(runBlocking { imports.all().isEmpty() })
    }

    @Test fun boundVehicleHistoryContainsOwnSourcesOnlyEvenWhenArchiveHasSameVin() {
        runBlocking {
            vehicles.save(read(card("synthetic-legacy"), "TEST LEGACY", "2026-09-29T00:00:00Z"))
            imports.bind(card(), true)
            imports.save(read(), "own", CardImportSelection.PAIR)
        }
        compose.setContent {
            FahrerTagebuchTheme(ThemeMode.LIGHT) {
                VehicleHistoryScreen(PaddingValues(), vehicles, driverImports = imports)
            }
        }
        awaitText("TEST OWN")
        compose.onNodeWithText("TEST OWN", substring = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("TEST LEGACY", substring = true).assertDoesNotExist()
    }

    @Test fun explicitlySelectedArchiveShowsOnlyThatFileWithoutBindingIt() {
        val archive = runBlocking {
            vehicles.save(read(card("synthetic-legacy"), "TEST LEGACY", "2026-09-29T00:00:00Z"))
            val saved = vehicles.all().single()
            imports.bind(card(), true)
            imports.save(read(), "own", CardImportSelection.PAIR)
            saved
        }
        compose.setContent {
            FahrerTagebuchTheme(ThemeMode.LIGHT) {
                VehicleHistoryScreen(PaddingValues(), vehicles, previewSnapshot = archive, driverImports = imports)
            }
        }
        awaitText("TEST LEGACY")
        compose.onNodeWithText("TEST LEGACY", substring = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("TEST OWN", substring = true).assertDoesNotExist()
        assertEquals(listOf("own"), runBlocking { imports.all().map { it.id } })
        assertEquals(1, runBlocking { db.driverProfileDao().cards(imports.profile()!!.id).size })
    }

    @Test fun archiveDayNavigationKeepsTheSelectedVehicleSourceOutsideProfileHistory() {
        runBlocking {
            pairs.save(read(card("synthetic-archive"), "TEST ARCHIVE", "2026-09-27T00:00:00Z"))
            imports.bind(card(), true)
            imports.save(read(), "own", CardImportSelection.PAIR)
        }
        compose.setContent {
            FahrerTagebuchTheme(ThemeMode.LIGHT) {
                AppRoot(ThemeMode.LIGHT, {}, activities, vehicles, pairs, driverCardImports = imports)
            }
        }
        compose.onNodeWithText("Fahrerkarte ansehen", substring = true).performScrollTo().performClick()
        compose.onNodeWithText("Gespeicherte Teilimporte ansehen").performScrollTo().performClick()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Gemeinsame Vorschau öffnen").fetchSemanticsNodes().size == 2
        }
        // Both source pairs share a timestamp; select by the underlying archive identity.
        val archiveIndex = runBlocking { pairs.all().indexOfFirst { it.vehicles.uses.single().registrationNumber == "TEST ARCHIVE" } }
        compose.onAllNodesWithText("Gemeinsame Vorschau öffnen")[archiveIndex].performScrollTo().performClick()
        awaitText("TEST ARCHIVE · Staat 13")
        compose.onNodeWithText("Fahrzeughistorie öffnen").performScrollTo().performClick()
        awaitText("Fahrzeuge aus dieser gespeicherten Datei.")
        compose.onNodeWithText("TEST ARCHIVE", substring = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("TEST OWN", substring = true).assertDoesNotExist()
        assertEquals(listOf("own"), runBlocking { imports.all().map { it.id } })
    }
}
