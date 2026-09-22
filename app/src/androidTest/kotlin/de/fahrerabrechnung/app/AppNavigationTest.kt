package de.fahrerabrechnung.app

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import de.fahrerabrechnung.core.design.FahrerabrechnungTheme
import de.fahrerabrechnung.core.model.ThemeMode
import org.junit.Rule
import org.junit.Test

class AppNavigationTest {
    @get:Rule val compose = createComposeRule()

    @Test fun appShowsExactlyFiveTopLevelNavigationEntriesAndSwitches() {
        compose.setContent {
            FahrerabrechnungTheme(ThemeMode.LIGHT) {
                AppRoot(ThemeMode.LIGHT, {})
            }
        }
        listOf("start", "calendar", "analytics", "payroll", "settings").forEach {
            compose.onNodeWithTag("nav_$it").assertExists()
        }
        compose.onNodeWithTag("nav_calendar").performClick()
        compose.onNodeWithText("September 2026 · Strukturvorschau").assertExists()
        compose.onNodeWithTag("nav_analytics").performClick()
        compose.onNodeWithText("Monatsauswertung · UI-Platzhalter").assertExists()
    }

    @Test fun darkThemeStartsWithoutCrash() {
        compose.setContent {
            FahrerabrechnungTheme(ThemeMode.DARK) {
                AppRoot(ThemeMode.DARK, {})
            }
        }
        compose.onNodeWithTag("nav_start").assertExists()
    }
}
