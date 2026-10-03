package de.fahrerabrechnung.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import de.fahrerabrechnung.core.design.FahrerTagebuchTheme
import de.fahrerabrechnung.core.model.ThemeMode
import de.fahrerabrechnung.data.card.*
import de.fahrerabrechnung.feature.common.ScreenContent
import java.nio.ByteBuffer
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Rule
import org.junit.Test

class CardSpecificConditionTimelineUiTest {
    @get:Rule val compose = createComposeRule()
    private val zone = ZoneId.of("Europe/Berlin")
    private val date = LocalDate.of(2026, 9, 28)
    private fun source(vararg entries: Pair<String?, Int>, generation: CardGeneration = CardGeneration.GEN2V2,
                       cardId: String = "own", id: String = "fixture-conditions"): SavedCardSpecificConditions {
        val count = if (generation == CardGeneration.GEN1) 56 else 112
        val header = if (generation == CardGeneration.GEN1) 0 else 2
        val bytes = ByteArray(CardSpecificConditionParser.fileLength(generation, count))
        entries.forEachIndexed { slot, (time, type) ->
            ByteBuffer.wrap(bytes).apply { position(header + slot * 5)
                putInt(time?.let { Instant.parse(it).epochSecond.toInt() } ?: 0); put(type.toByte()) }
        }
        val file = CardSpecificConditionFile(generation, count, bytes)
        return SavedCardSpecificConditions(id, cardId, file, CardSpecificConditionParser.parse(file), 0)
    }
    private fun points(source: SavedCardSpecificConditions, day: LocalDate = date) =
        CardDaySpecificConditionMarkers.forDate(day, zone, "own", source)
    private fun show(points: List<CardDaySpecificConditionMarker>, issues: List<CardDayEventFaultMarker> = emptyList(),
                     day: LocalDate = date) {
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            ScreenContent(PaddingValues()) { CardTimelinePreview(LocalCardDay(day, emptyList()), zone,
                eventFaultMarkers = issues, specificConditionMarkers = points) }
        } }
    }
    private fun tap(tag: String) = compose.onNodeWithTag(tag).performScrollTo().performTouchInput { click() }

    @Test fun tappingARecordedEndShowsExactSecondsWithoutInventingABeginOrDuration() {
        show(points(source("2026-09-28T10:00:15Z" to 4)))
        tap("timeline_condition_overview")
        compose.onNodeWithText("Fähre/Zug · Ende").assertIsDisplayed()
        compose.onNodeWithText("Zeitpunkt: 28.09.2026 12:00:15 +02:00").assertIsDisplayed()
        compose.onNodeWithText("Fähre/Zug · Beginn").assertDoesNotExist()
        compose.onNodeWithText("Dauer", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Verstoß", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Schließen").performClick()
        compose.onNodeWithText("Fähre/Zug · Ende").assertDoesNotExist()
    }

    @Test fun coincidentFerryOutOfScopeAndEventPointsRemainIndependentlySelectable() {
        val time = Instant.parse("2026-09-28T10:00:00Z")
        val points = points(source(time.toString() to 1, time.toString() to 3))
        val event = CardDayEventFaultMarker("fixture-event", CardGeneration.GEN2V2, CardEventFaultKind.EVENTS,
            CardEventFaultRecord(0, 0, 5, time, time, 13, "TEST EVENT"), time, CardEventFaultBoundary.BEGIN_AND_END)
        show(points, listOf(event))
        tap("timeline_condition_overview")
        compose.onNodeWithText("Sonderbedingungen dieses Tages").assertIsDisplayed()
        points.forEach { compose.onNodeWithTag("choose_condition_${it.id}").assertIsDisplayed() }
        compose.onNodeWithTag("choose_condition_${points.last().id}").performClick()
        compose.onNodeWithText("Fähre/Zug · Beginn").assertIsDisplayed()
        compose.onNodeWithText("Fahrzeug: TEST EVENT").assertDoesNotExist()
        compose.onNodeWithText("Schließen").performClick()
        tap("timeline_issue_overview")
        compose.onNodeWithText("Fahrzeug: TEST EVENT").assertIsDisplayed()
        compose.onNodeWithText("Fähre/Zug · Beginn").assertDoesNotExist()
    }

    @Test fun zoomedTrackSelectsTheVisiblePointWithoutClampingAnOffscreenPoint() {
        show(points(source("2026-09-28T10:00:00Z" to 1, "2026-09-27T22:30:00Z" to 3)))
        repeat(3) { compose.onNodeWithText("+").performScrollTo().performClick() }
        compose.onNodeWithText("8×").assertIsDisplayed()
        tap("timeline_condition_detail")
        compose.onNodeWithText("Out of Scope · Beginn").assertIsDisplayed()
        compose.onNodeWithText("Fähre/Zug · Beginn").assertDoesNotExist()
        compose.onNodeWithText("Sonderbedingungen dieses Tages").assertDoesNotExist()
    }

    @Test fun changingSourceOrDayClearsTheOpenRecordWithoutCarryingMarkersForward() {
        val day = mutableStateOf(date)
        val selected = mutableStateOf(source("2026-09-28T10:00:00Z" to 1))
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            ScreenContent(PaddingValues()) { CardTimelinePreview(LocalCardDay(day.value, emptyList()), zone,
                specificConditionMarkers = points(selected.value, day.value)) }
        } }
        tap("timeline_condition_overview")
        compose.onNodeWithText("Out of Scope · Beginn").assertIsDisplayed()
        compose.runOnIdle { selected.value = source("2026-09-28T10:00:00Z" to 4, id = "fixture-next") }
        compose.onNodeWithText("Out of Scope · Beginn").assertDoesNotExist()
        tap("timeline_condition_overview")
        compose.onNodeWithText("Fähre/Zug · Ende").assertIsDisplayed()
        compose.runOnIdle { day.value = date.plusDays(1) }
        compose.onNodeWithText("Fähre/Zug · Ende").assertDoesNotExist()
        compose.onNodeWithTag("timeline_condition_overview").assertDoesNotExist()
        compose.onNodeWithTag("day_condition_list").assertDoesNotExist()
    }

    @Test fun archiveMissingSourceBindingAndReplacementCardNeverReceiveMarkers() {
        val epoch = Instant.parse("2026-09-28T00:00:00Z").epochSecond.toInt()
        val raw = ByteBuffer.allocate(14).putShort(0).putShort(14).putInt(epoch).putShort(1).putShort(0).putShort(0).array()
        val activity = SavedCardActivity(CardActivityFile(0, 0, raw), CardGeneration.GEN2V2, 1, 0, "fixture-number")
        val day = CardDayTimeline.build(activity.days, ZoneId.systemDefault()).last()
        val conditions = source(day.segments.first().start.plusSeconds(30).toString() to 1)
        val binding = mutableStateOf<String?>(null)
        val archive = mutableStateOf(false)
        val selected = mutableStateOf<SavedCardSpecificConditions?>(conditions)
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            SavedCardPreviewScreen(PaddingValues(), activity, null, { _, _, _ -> }, archive.value,
                driverCardId = binding.value, specificConditions = selected.value)
        } }
        compose.onNodeWithTag("timeline_condition_overview").assertDoesNotExist()
        compose.runOnIdle { binding.value = "own" }
        compose.onNodeWithTag("timeline_condition_overview").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { archive.value = true }
        compose.onNodeWithTag("timeline_condition_overview").assertDoesNotExist()
        compose.runOnIdle { archive.value = false; binding.value = "replacement" }
        compose.onNodeWithTag("timeline_condition_overview").assertDoesNotExist()
        compose.runOnIdle { binding.value = "own"; selected.value = null }
        compose.onNodeWithTag("timeline_condition_overview").assertDoesNotExist()
        compose.runOnIdle { selected.value = source() }
        compose.onNodeWithTag("timeline_condition_overview").assertDoesNotExist()
        compose.runOnIdle { selected.value = source(null to 1) }
        compose.onNodeWithTag("timeline_condition_overview").assertDoesNotExist()
    }

    @Test fun gen1FerryHasNoInventedBeginAndReservedCodesStayNeutral() {
        val points = points(source("2026-09-28T10:00:00Z" to 3, "2026-09-28T11:00:00Z" to 4,
            generation = CardGeneration.GEN1))
        show(points)
        compose.onNodeWithTag("day_condition_list").performScrollTo().performClick()
        compose.onNodeWithTag("choose_condition_${points.first().id}").performClick()
        compose.onNodeWithText("Fähre/Zug").assertIsDisplayed()
        compose.onNodeWithText("Fähre/Zug · Beginn").assertDoesNotExist()
        compose.onNodeWithText("Schließen").performClick()
        compose.onNodeWithTag("day_condition_list").performScrollTo().performClick()
        compose.onNodeWithTag("choose_condition_${points.last().id}").performClick()
        compose.onNodeWithText("Unbekannte Sonderbedingung (Code 04)").assertIsDisplayed()
        compose.onNodeWithText("Fähre/Zug · Ende").assertDoesNotExist()
    }

    @Test fun repeatedAutumnClockTimeShowsBothRecordedOffsetsInTheDayList() {
        val dst = LocalDate.of(2026, 10, 25)
        val points = points(source("2026-10-25T00:30:15Z" to 3, "2026-10-25T01:30:15Z" to 4), dst)
        show(points, day = dst)
        compose.onNodeWithTag("day_condition_list").performScrollTo().performClick()
        compose.onNodeWithText("25.10.2026 02:30:15 +02:00", substring = true).assertIsDisplayed()
        compose.onNodeWithText("25.10.2026 02:30:15 +01:00", substring = true).assertIsDisplayed()
        compose.onNodeWithTag("choose_condition_${points.last().id}").performClick()
        compose.onNodeWithText("Zeitpunkt: 25.10.2026 02:30:15 +01:00").assertIsDisplayed()
    }
}
