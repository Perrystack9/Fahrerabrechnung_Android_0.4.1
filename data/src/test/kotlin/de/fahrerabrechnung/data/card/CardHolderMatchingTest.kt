package de.fahrerabrechnung.data.card

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class CardHolderMatchingTest {
    private val known = DriverIdentity("ALPHA", "TEST PERSON", LocalDate.of(2000, 2, 29), "de")

    @Test fun exactCompleteIdentityMatchesWithoutUsingCardNumberOrLanguage() {
        assertEquals(CardHolderMatch.SAME, CardHolderMatching.compare(known.copy(language = "en"), listOf(known)))
    }

    @Test fun missingBirthDateOrGivenNamesRequiresConfirmation() {
        for (value in listOf(known.copy(birthDate = null), known.copy(givenNames = ""), known.copy(surname = "")))
            assertEquals(CardHolderMatch.UNCLEAR, CardHolderMatching.compare(value, listOf(known)))
        assertEquals(CardHolderMatch.UNCLEAR, CardHolderMatching.compare(known, listOf(known.copy(birthDate = null))))
    }

    @Test fun namePrefixCaseAndSpellingNeverAutomaticallyMatch() {
        for (name in listOf("TEST", "test person", "TEST  PERSON", "TEST PERSON OTHER"))
            assertEquals(CardHolderMatch.UNCLEAR, CardHolderMatching.compare(known.copy(givenNames = name), listOf(known)))
    }

    @Test fun changedNameOrDateAloneIsUnclear() {
        assertEquals(CardHolderMatch.UNCLEAR, CardHolderMatching.compare(known.copy(surname = "BETA"), listOf(known)))
        assertEquals(CardHolderMatch.UNCLEAR, CardHolderMatching.compare(known.copy(birthDate = LocalDate.of(2001, 2, 28)), listOf(known)))
    }

    @Test fun contradictoryCompleteNameAndBirthDateIsForeign() {
        val foreign = known.copy(surname = "FOREIGN", givenNames = "OTHER", birthDate = LocalDate.of(1980, 1, 1))
        assertEquals(CardHolderMatch.DIFFERENT, CardHolderMatching.compare(foreign, listOf(known)))
    }

    @Test fun potentiallyTruncatedOrUndecodedNameCannotMatchAutomatically() {
        for (name in listOf("X".repeat(35), "TEST\uFFFD", "TEST\n")) {
            val value = known.copy(givenNames = name)
            assertEquals(CardHolderMatch.UNCLEAR, CardHolderMatching.compare(value, listOf(value)))
        }
    }

    @Test fun anyExplicitlyAssignedCompleteIdentityCanMatchAFollowingCard() {
        val changedName = known.copy(surname = "BETA")
        assertEquals(CardHolderMatch.SAME, CardHolderMatching.compare(changedName, listOf(known, changedName)))
    }

    @Test fun incompleteKnownIdentityPreventsAConfidentForeignClassification() {
        val foreign = known.copy(surname = "FOREIGN", givenNames = "OTHER", birthDate = LocalDate.of(1980, 1, 1))
        assertEquals(CardHolderMatch.UNCLEAR, CardHolderMatching.compare(foreign, listOf(known, known.copy(birthDate = null))))
        assertEquals(CardHolderMatch.UNCLEAR, CardHolderMatching.compare(known, emptyList()))
    }
}
