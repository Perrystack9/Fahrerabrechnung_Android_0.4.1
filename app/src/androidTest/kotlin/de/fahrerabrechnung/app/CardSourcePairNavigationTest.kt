package de.fahrerabrechnung.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.assertIsDisplayed
import de.fahrerabrechnung.core.design.FahrerTagebuchTheme
import de.fahrerabrechnung.core.model.ThemeMode
import de.fahrerabrechnung.data.card.*
import de.fahrerabrechnung.data.db.AppDatabase
import java.nio.ByteBuffer
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Reopening an old pair must not silently substitute the latest vehicle source. */
class CardSourcePairNavigationTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var db: AppDatabase
    private lateinit var activities: CardActivityImportRepository
    private lateinit var vehicles: CardVehicleImportRepository
    private lateinit var pairs: CardSourcePairRepository

    @Before fun prepare() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java).build()
        val clock = Clock.fixed(Instant.parse("2026-09-29T12:00:00Z"), ZoneOffset.UTC)
        activities = CardActivityImportRepository(db.cardActivityImportDao(), clock)
        vehicles = CardVehicleImportRepository(db.cardVehicleImportDao(), clock)
        val oldPairs = CardSourcePairRepository(db, activities, vehicles, Clock.offset(clock, java.time.Duration.ofDays(-1)))
        pairs = CardSourcePairRepository(db, activities, vehicles, clock)
        oldPairs.save(read("2026-09-27T00:00:00Z", "TEST OLD"))
        pairs.save(read("2026-09-28T00:00:00Z", "TEST NEW"))
        Unit
    }

    @After fun close() { db.close() }

    @Test fun selectedOlderPairOpensBothExactFilesAndCanReturnToLatest() {
        compose.setContent {
            FahrerTagebuchTheme(ThemeMode.LIGHT) {
                AppRoot(ThemeMode.LIGHT, {}, activities, vehicles, pairs)
            }
        }
        compose.onNodeWithText("Fahrerkarte ansehen", substring = true).performScrollTo().performClick()
        compose.onNodeWithText("Gespeicherte Teilimporte ansehen").performScrollTo().performClick()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Gemeinsame Vorschau öffnen").fetchSemanticsNodes().size == 2
        }
        compose.onAllNodesWithText("Gemeinsame Vorschau öffnen")[1].performScrollTo().performClick()
        compose.onNodeWithText("Gemeinsam gespeicherte Vorschau: Beide Dateien stammen aus demselben Lesevorgang.")
            .performScrollTo().assertIsDisplayed()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("TEST OLD · Staat 13").fetchSemanticsNodes().size == 1 }
        compose.onNodeWithText("TEST OLD · Staat 13").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("TEST NEW · Staat 13").assertDoesNotExist()
        compose.onNodeWithText("Neueste einzelne Dateien zeigen").performScrollTo().performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("TEST NEW · Staat 13").fetchSemanticsNodes().size == 1 }
        compose.onNodeWithText("TEST NEW · Staat 13").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("TEST OLD · Staat 13").assertDoesNotExist()
    }

    private fun read(date: String, plate: String): CardReadResult {
        val epoch = Instant.parse(date).epochSecond.toInt()
        val day = ByteBuffer.allocate(14).putShort(0).putShort(14).putInt(epoch)
            .putShort(1).putShort(0).putShort(0).array()
        val vehicle = ByteBuffer.allocate(48).put(byteArrayOf(0, 0, 1, 0, 0, 2))
            .putInt(epoch + 3600).putInt(epoch + 7200).put(13.toByte()).put(1.toByte())
            .put(plate.padEnd(13).toByteArray()).putShort(1)
            .put("WDB12345678901234".toByteArray()).array()
        val identity = CardIdentity("synthetic-card", 13, "Test", Instant.EPOCH,
            Instant.EPOCH, Instant.parse("2030-01-01T00:00:00Z"))
        return CardReadResult(CardRecognition.DRIVER, CardGeneration.GEN2V2,
            DriverCard(CardGeneration.GEN2V2, identity, DriverIdentity("Test", "Fixture", Instant.EPOCH, "de")),
            CardActivityFile(0, 0, day), CardVehiclesFile(0, 1, CardGeneration.GEN2V2, vehicle))
    }
}
