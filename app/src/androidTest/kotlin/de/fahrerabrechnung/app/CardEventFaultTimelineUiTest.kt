package de.fahrerabrechnung.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
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
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CardEventFaultTimelineUiTest {
    @get:Rule val compose = createComposeRule()
    private val zone = ZoneId.of("Europe/Berlin")
    private val date = LocalDate.of(2026, 9, 28)
    private fun source(kind: CardEventFaultKind, begin: String, end: String?, plate: String,
                       cardId: String = "own") = SavedCardEventFault("fixture-${kind.name}", cardId,
        CardEventFaultFile(kind, CardGeneration.GEN2V2, if (kind == CardEventFaultKind.EVENTS) 12 else 24, byteArrayOf()),
        listOf(CardEventFaultRecord(0, 0, if (kind == CardEventFaultKind.EVENTS) 5 else 0x40,
            Instant.parse(begin), end?.let(Instant::parse), 13, plate)), 0)
    private fun markers(events: SavedCardEventFault?, faults: SavedCardEventFault? = null) =
        CardDayEventFaultMarkers.forDate(date, zone, "own", events, faults)
    private fun show(points: List<CardDayEventFaultMarker>) {
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            ScreenContent(PaddingValues()) { CardTimelinePreview(LocalCardDay(date, emptyList()), zone, eventFaultMarkers = points) }
        } }
    }
    private fun tapTrack(tag: String, fraction: Float = 0.5f) {
        compose.onNodeWithTag(tag).performScrollTo().performTouchInput {
            click(Offset(size.width * fraction, size.height * 0.5f))
        }
    }

    @Test fun tappingARecordedPointOpensExactSecondsAndUnknownEndWithoutEvaluation() {
        show(markers(source(CardEventFaultKind.EVENTS, "2026-09-28T10:00:15Z", null, "TEST EVENT")))
        tapTrack("timeline_issue_overview")
        compose.onNodeWithText("Ereignis · Beginn").assertIsDisplayed()
        compose.onNodeWithText("Karte während der Fahrt eingelegt").assertIsDisplayed()
        compose.onNodeWithText("Beginn: 28.09.2026 12:00:15 +02:00").assertIsDisplayed()
        compose.onNodeWithText("Ende: nicht angegeben").assertIsDisplayed()
        compose.onNodeWithText("Fahrzeug: TEST EVENT").assertIsDisplayed()
        compose.onNodeWithText("Verstoß", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Dauer", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Schließen").performClick()
        compose.onNodeWithText("Ereignis · Beginn").assertDoesNotExist()
    }

    @Test fun overlappingEventAndFaultRemainSeparatelySelectable() {
        val points = markers(source(CardEventFaultKind.EVENTS, "2026-09-28T10:00:00Z", "2026-09-28T10:00:00Z", "TEST EVENT"),
            source(CardEventFaultKind.FAULTS, "2026-09-28T10:00:00Z", "2026-09-28T10:00:00Z", "TEST FAULT"))
        show(points)
        tapTrack("timeline_issue_overview")
        compose.onNodeWithText("Meldungen dieses Tages").assertIsDisplayed()
        points.forEach { compose.onNodeWithTag("choose_issue_${it.id}").assertIsDisplayed() }
        compose.onNodeWithTag("choose_issue_${points.last().id}").performClick()
        compose.onNodeWithText("Störung · Beginn und Ende").assertIsDisplayed()
        compose.onNodeWithText("Fahrzeug: TEST FAULT").assertIsDisplayed()
        compose.onNodeWithText("Fahrzeug: TEST EVENT").assertDoesNotExist()
    }

    @Test fun zoomedTrackSelectsTheVisiblePointWithoutDraggingOffscreenRecordsToItsEdge() {
        val points = markers(source(CardEventFaultKind.EVENTS, "2026-09-28T10:00:00Z", null, "TEST VISIBLE"),
            source(CardEventFaultKind.FAULTS, "2026-09-27T22:30:00Z", null, "TEST OFFSCREEN"))
        show(points)
        repeat(3) { compose.onNodeWithText("+").performScrollTo().performClick() }
        compose.onNodeWithText("8×").assertIsDisplayed()
        tapTrack("timeline_issue_detail")
        compose.onNodeWithText("Fahrzeug: TEST VISIBLE").assertIsDisplayed()
        compose.onNodeWithText("Meldungen dieses Tages").assertDoesNotExist()
        compose.onNodeWithText("Fahrzeug: TEST OFFSCREEN").assertDoesNotExist()
    }

    @Test fun changingTheDayClearsTheOpenRecordAndNoPointIsCarriedIntoMissingTime() {
        val day = mutableStateOf(date)
        val source = source(CardEventFaultKind.EVENTS, "2026-09-28T10:00:00Z", null, "TEST OLD DAY")
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            ScreenContent(PaddingValues()) { CardTimelinePreview(LocalCardDay(day.value, emptyList()), zone,
                eventFaultMarkers = CardDayEventFaultMarkers.forDate(day.value, zone, "own", source, null)) }
        } }
        tapTrack("timeline_issue_overview")
        compose.onNodeWithText("Fahrzeug: TEST OLD DAY").assertIsDisplayed()
        compose.runOnIdle { day.value = date.plusDays(1) }
        compose.onNodeWithText("Fahrzeug: TEST OLD DAY").assertDoesNotExist()
        compose.onNodeWithTag("timeline_issue_overview").assertDoesNotExist()
        compose.onNodeWithTag("day_issue_list").assertDoesNotExist()
    }

    @Test fun archiveMissingBindingAndAnotherPhysicalCardNeverReceiveMarkers() {
        val epoch = Instant.parse("2026-09-28T00:00:00Z").epochSecond.toInt()
        val day = ByteBuffer.allocate(14).putShort(0).putShort(14).putInt(epoch).putShort(1).putShort(0).putShort(0).array()
        val activity = SavedCardActivity(CardActivityFile(0, 0, day), CardGeneration.GEN2V2, 1, 0, "fixture-number")
        val selectedDay = CardDayTimeline.build(activity.days, ZoneId.systemDefault()).last()
        val point = selectedDay.segments.first().start.plusSeconds(30).toString()
        val event = source(CardEventFaultKind.EVENTS, point, null, "TEST OWN")
        val binding = mutableStateOf<String?>(null)
        val archive = mutableStateOf(false)
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            SavedCardPreviewScreen(PaddingValues(), activity, null, { _, _, _ -> }, archive.value,
                driverCardId = binding.value, events = event)
        } }
        compose.onNodeWithTag("timeline_issue_overview").assertDoesNotExist()
        compose.runOnIdle { binding.value = "own" }
        compose.onNodeWithTag("timeline_issue_overview").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { archive.value = true }
        compose.onNodeWithTag("timeline_issue_overview").assertDoesNotExist()
        compose.runOnIdle { archive.value = false; binding.value = "replacement" }
        compose.onNodeWithTag("timeline_issue_overview").assertDoesNotExist()
        assertEquals(1, event.records.size)
    }
}
