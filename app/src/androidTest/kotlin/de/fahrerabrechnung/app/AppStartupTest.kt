package de.fahrerabrechnung.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class AppStartupTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun realActivityStartsAndOpensSettings() {
        compose.onNodeWithText("FahrerTagebuch").assertIsDisplayed()
        compose.onNodeWithTag("nav_settings").performClick()
        compose.onNodeWithText("Deine App. Deine Übersicht.").assertIsDisplayed()
    }
}
