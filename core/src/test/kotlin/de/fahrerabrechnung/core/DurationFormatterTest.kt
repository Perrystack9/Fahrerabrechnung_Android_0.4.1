package de.fahrerabrechnung.core

import de.fahrerabrechnung.core.model.DurationFormatter
import java.time.Duration
import org.junit.Assert.assertEquals
import org.junit.Test

class DurationFormatterTest {
    @Test fun formatsDurationWithoutPuttingFormattedTextIntoDomainModel() {
        assertEquals("2:01 h", DurationFormatter.hoursMinutes(Duration.ofMinutes(121)))
    }
}
