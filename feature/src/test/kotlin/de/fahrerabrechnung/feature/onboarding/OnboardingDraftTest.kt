package de.fahrerabrechnung.feature.onboarding

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class OnboardingDraftTest {
    @Test fun adjacentNightWindowsAreAccepted() {
        assertNull(nightWindowError(listOf(NightWindowDraft("00:00", "02:00", "40"), NightWindowDraft("02:00", "06:00", "25"))))
    }

    @Test fun overlapAcrossMidnightIsRejected() {
        assertNotNull(nightWindowError(listOf(NightWindowDraft("22:00", "02:00", "25"), NightWindowDraft("01:00", "04:00", "40"))))
    }

    @Test fun adjacentWindowAcrossMidnightIsAccepted() {
        assertNull(nightWindowError(listOf(NightWindowDraft("22:00", "02:00", "25"), NightWindowDraft("02:00", "06:00", "40"))))
    }

    @Test fun incompleteInvalidAndZeroLengthWindowsAreRejected() {
        listOf(NightWindowDraft(), NightWindowDraft("25:00", "06:00", "25"), NightWindowDraft("22:00", "22:00", "25"), NightWindowDraft("22:00", "06:00", "-")).forEach {
            assertNotNull(nightWindowError(listOf(it)))
        }
    }

    @Test fun emptyDraftAndEndOfDayAreAccepted() {
        assertNull(nightWindowError(emptyList()))
        assertNull(nightWindowError(listOf(NightWindowDraft("20:00", "24:00", "25"), NightWindowDraft("00:00", "02:00", "40"))))
    }
}
