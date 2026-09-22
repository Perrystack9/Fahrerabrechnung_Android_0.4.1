package de.fahrerabrechnung.domain

import de.fahrerabrechnung.domain.model.ActivityType
import de.fahrerabrechnung.domain.model.Money
import de.fahrerabrechnung.domain.model.ResultValue
import de.fahrerabrechnung.domain.model.TimeContext
import de.fahrerabrechnung.domain.status.DataStatus
import de.fahrerabrechnung.domain.status.RouteStatus
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DomainTypesTest {
    @Test fun `required data statuses stay stable`() {
        assertEquals(listOf("VOLLSTAENDIG","TEILWEISE","UNKLAR","KEINE_DATEN","NICHT_ANWENDBAR"), DataStatus.entries.map { it.name })
    }

    @Test fun `required route statuses stay stable`() {
        assertEquals(5, RouteStatus.entries.size)
        assertEquals(RouteStatus.NUR_GEN2_ORTSDATEN, RouteStatus.valueOf("NUR_GEN2_ORTSDATEN"))
    }

    @Test fun `required card activity types stay stable`() {
        assertEquals(5, ActivityType.entries.size)
    }

    @Test fun `money keeps decimal arithmetic and explicit currency`() {
        val sum = Money.of(BigDecimal("1.005"), "EUR") + Money.of(BigDecimal("2.005"), "EUR")
        assertEquals(0, BigDecimal("3.01").compareTo(sum.rounded().amount))
        assertEquals("EUR", sum.currencyCode)
    }

    @Test fun `money refuses implicit currency mixing`() {
        assertThrows(IllegalArgumentException::class.java) {
            Money.of(BigDecimal.ONE, "EUR") + Money.of(BigDecimal.ONE, "USD")
        }
    }

    @Test fun `unknown result has no fake null value`() {
        val value: ResultValue<Int> = ResultValue.Unclear("Quelle fehlt")
        assertEquals(DataStatus.UNKLAR, value.status)
    }

    @Test fun `report timezone is fixed`() {
        assertEquals("Europe/Berlin", TimeContext.REPORT_ZONE.id)
    }
}
