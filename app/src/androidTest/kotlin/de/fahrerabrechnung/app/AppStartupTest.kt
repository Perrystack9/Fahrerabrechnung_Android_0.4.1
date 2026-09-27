package de.fahrerabrechnung.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class AppStartupTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun realActivityStartsWithOnboardingAndOpensSettingsAfterCompletion() {
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodesWithTag("onboarding").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Schritt 1 von 7").assertIsDisplayed()
        compose.onNodeWithTag("nav_settings").assertDoesNotExist()
        repeat(6) { compose.onNodeWithTag("onboarding_next").performClick() }
        compose.onNodeWithText("Schritt 7 von 7").assertIsDisplayed()
        compose.onNodeWithTag("onboarding_next").performClick()
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodesWithTag("nav_settings").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("nav_settings").performClick()
        compose.onNodeWithText("Deine App. Deine Übersicht.").assertIsDisplayed()
    }
}
