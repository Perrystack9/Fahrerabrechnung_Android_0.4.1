package de.fahrerabrechnung.app

import android.content.ContentValues
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import de.fahrerabrechnung.core.design.FahrerTagebuchTheme
import de.fahrerabrechnung.core.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@OptIn(ExperimentalTestApi::class)
@RunWith(Parameterized::class)
class AppNavigationTest(private val widthDp: Int, private val mode: ThemeMode) {
    @get:Rule val compose = createComposeRule()

    private fun launch() {
        compose.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.ForcedSize(DpSize(widthDp.dp, 800.dp))) {
                FahrerTagebuchTheme(mode) { AppRoot(mode, {}) }
            }
        }
    }

    @Test fun allFiveTabsHaveEqualSingleLineUnclippedLabelsAndSwitchImmediately() {
        launch()
        val routes = listOf("start", "calendar", "analytics", "payroll", "settings")
        val layouts = routes.map { route ->
            compose.onNodeWithTag("nav_$route").assertIsDisplayed()
            compose.onNodeWithTag("nav_label_$route", useUnmergedTree = true).assertIsDisplayed()
            val result = mutableListOf<TextLayoutResult>()
            compose.onNodeWithTag("nav_label_$route", useUnmergedTree = true)
                .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(result) }
            val layout = result.single()
            assertEquals(1, layout.lineCount)
            // Compose's semantics TextLayoutResult for a String remeasures with a full-width
            // MultiParagraph but retains the narrow intrinsic Text size. Its didOverflowWidth
            // can therefore be true even for "Start". Check the rendered glyph bounds and
            // the complete visible text instead of comparing those incompatible box widths.
            assertEquals("Truncated label at $widthDp dp: $route", layout.layoutInput.text.length, layout.getLineEnd(0, visibleEnd = true))
            assertFalse("Vertically clipped label at $widthDp dp: $route", layout.didOverflowHeight)
            assertTrue("Horizontally clipped label at $widthDp dp: $route (left=${layout.getLineLeft(0)}, right=${layout.getLineRight(0)}, width=${layout.size.width})",
                layout.getLineLeft(0) >= 0f && layout.getLineRight(0) <= layout.size.width.toFloat())
            layout
        }
        assertEquals(1, layouts.map { it.layoutInput.style.fontSize }.distinct().size)
        screenshot("start")
        compose.mainClock.autoAdvance = false
        compose.onNodeWithTag("nav_calendar").performClick()
        compose.mainClock.advanceTimeBy(32)
        compose.onNodeWithTag("nav_calendar").assertIsSelected()
        screenshot("calendar_32ms")
        compose.onNodeWithText("September 2026 · Strukturvorschau").assertIsDisplayed()
        compose.onNodeWithText("Dein Fahrertag im Überblick").assertDoesNotExist()
        compose.mainClock.autoAdvance = true
        listOf("analytics" to "Monatsauswertung", "payroll" to "Abrechnungsvergleich · Beispielstruktur", "settings" to "Deine App. Deine Übersicht.").forEach { (route, heading) ->
            compose.onNodeWithTag("nav_$route").performClick()
            compose.onNodeWithTag("nav_$route").assertIsSelected()
            compose.onNodeWithText(heading).assertIsDisplayed()
            screenshot(route)
        }
    }

    @Test fun tabScrollPositionSurvivesRoundTrip() {
        launch()
        compose.onNodeWithText("Hinweise").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("nav_calendar").performClick()
        compose.onNodeWithTag("nav_start").performClick()
        compose.onNodeWithText("Hinweise").assertIsDisplayed()
    }

    @Test fun allSevenSetupStepsAndSummaryEditingRemainNavigable() {
        launch()
        compose.onNodeWithText("Einrichtung ansehen").performClick()
        compose.waitUntilAtLeastOneExists(hasTestTag("onboarding_username"))
        compose.onNodeWithText("App-GPS", substring = true).assertDoesNotExist()
        compose.onNodeWithTag("onboarding_username").performTextInput("Testfahrer")
        for (step in 1..7) {
            compose.onNodeWithText("Schritt $step von 7").assertIsDisplayed()
            compose.onNodeWithText("App-GPS", substring = true).assertDoesNotExist()
            if (step == 1) {
                compose.onNodeWithText("Vorname", substring = false).assertDoesNotExist()
                compose.onNodeWithText("Nachname", substring = false).assertDoesNotExist()
            }
            if (step == 2) compose.onNodeWithText("Heimbereich: 500 Meter").performScrollTo().assertIsDisplayed()
            if (step == 3) {
                listOf("Spesen über 8 Stunden", "Spesen über 24 Stunden", "Übernachtungspauschale").forEach {
                    compose.onNodeWithTag(it).performScrollTo().assertIsDisplayed()
                }
            }
            if (step == 6) compose.onNodeWithText("Tag 27 · Empfehlung").performScrollTo().assertIsDisplayed()
            screenshot("onboarding_$step")
            if (step < 7) compose.onNodeWithTag("onboarding_next").performClick()
        }
        compose.onNodeWithTag("edit_step_1").performScrollTo().performClick()
        compose.onNodeWithText("Testfahrer").assertIsDisplayed()
        compose.onNodeWithTag("onboarding_next").performClick()
        compose.onNodeWithText("Schritt 7 von 7").assertIsDisplayed()
        compose.onNodeWithTag("onboarding_next").performClick()
        compose.onNodeWithTag("nav_start").assertIsSelected()
        // Draft belongs to the UI session and survives closing/reopening the preview.
        compose.onNodeWithText("Einrichtung ansehen").performClick()
        compose.onNodeWithText("Testfahrer").assertIsDisplayed()
    }

    private fun screenshot(name: String) {
        compose.waitForIdle()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Shared media survives AGP's automatic uninstall of the test application.
        val image = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "${widthDp}dp-${mode.name.lowercase()}-$name.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/FahrerTagebuch-CI")
        }) ?: error("Could not create screenshot in shared media")
        context.contentResolver.openOutputStream(image)?.use {
            assertTrue("Could not encode screenshot: $name",
                compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it))
        } ?: error("Could not open screenshot output stream")
    }

    companion object {
        @JvmStatic @Parameterized.Parameters(name = "{0}dp-{1}")
        fun configurations(): List<Array<Any>> = listOf(
            arrayOf(360, ThemeMode.LIGHT), arrayOf(412, ThemeMode.LIGHT),
            arrayOf(360, ThemeMode.DARK), arrayOf(412, ThemeMode.DARK),
        )
    }
}
