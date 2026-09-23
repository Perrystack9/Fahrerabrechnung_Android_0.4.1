package de.fahrerabrechnung.app

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
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
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
            val result = mutableListOf<TextLayoutResult>()
            compose.onNodeWithTag("nav_label_$route", useUnmergedTree = true)
                .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(result) }
            assertEquals(1, result.single().lineCount)
            assertFalse("Clipped label at $widthDp dp: $route", result.single().hasVisualOverflow)
            result.single()
        }
        assertEquals(1, layouts.map { it.layoutInput.style.fontSize }.distinct().size)
        screenshot("start")
        compose.mainClock.autoAdvance = false
        compose.onNodeWithTag("nav_calendar").performClick()
        compose.mainClock.advanceTimeBy(32)
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

    @Test fun allEightSetupStepsAndSummaryEditingRemainNavigable() {
        launch()
        compose.onNodeWithText("Einrichtung ansehen").performClick()
        compose.onNodeWithTag("onboarding_username").performTextInput("Testfahrer")
        for (step in 1..8) {
            compose.onNodeWithText("Schritt $step von 8").assertIsDisplayed()
            if (step == 1) {
                compose.onNodeWithText("Vorname", substring = false).assertDoesNotExist()
                compose.onNodeWithText("Nachname", substring = false).assertDoesNotExist()
            }
            if (step == 2) compose.onNodeWithText("Ohne App-GPS fortfahren").performClick()
            if (step == 3) compose.onNodeWithText("Heimbereich: 500 Meter").performScrollTo().assertIsDisplayed()
            if (step == 4) {
                listOf("Spesen über 8 Stunden", "Spesen über 24 Stunden", "Übernachtungspauschale").forEach {
                    compose.onNodeWithTag(it).performScrollTo().assertIsDisplayed()
                }
            }
            if (step == 7) compose.onNodeWithText("Tag 27 · Empfehlung").performScrollTo().assertIsDisplayed()
            screenshot("onboarding_$step")
            if (step < 8) compose.onNodeWithTag("onboarding_next").performClick()
        }
        compose.onNodeWithTag("edit_step_1").performScrollTo().performClick()
        compose.onNodeWithText("Testfahrer").assertIsDisplayed()
        compose.onNodeWithTag("onboarding_next").performClick()
        compose.onNodeWithText("Schritt 8 von 8").assertIsDisplayed()
        compose.onNodeWithTag("onboarding_next").performClick()
        compose.onNodeWithTag("nav_start").assertIsSelected()
        // Draft belongs to the UI session and survives closing/reopening the preview.
        compose.onNodeWithText("Einrichtung ansehen").performClick()
        compose.onNodeWithText("Testfahrer").assertIsDisplayed()
    }

    private fun screenshot(name: String) {
        compose.waitForIdle()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.getExternalFilesDir(null), "ui-screenshots").apply { mkdirs() }
        File(directory, "${widthDp}dp-${mode.name.lowercase()}-$name.png").outputStream().use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    companion object {
        @JvmStatic @Parameterized.Parameters(name = "{0}dp-{1}")
        fun configurations(): List<Array<Any>> = listOf(
            arrayOf(360, ThemeMode.LIGHT), arrayOf(412, ThemeMode.LIGHT),
            arrayOf(360, ThemeMode.DARK), arrayOf(412, ThemeMode.DARK),
        )
    }
}
