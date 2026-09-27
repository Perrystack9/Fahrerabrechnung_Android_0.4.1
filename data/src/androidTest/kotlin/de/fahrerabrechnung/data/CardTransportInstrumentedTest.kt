package de.fahrerabrechnung.data

import androidx.test.core.app.ApplicationProvider
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.fahrerabrechnung.data.card.AndroidCcidTransport
import de.fahrerabrechnung.data.card.ReaderState
import de.fahrerabrechnung.data.card.SourceAvailability
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CardTransportInstrumentedTest {
    @Test fun noUsbHostDeviceDoesNotOpenOrRequestPermission() = runBlocking {
        val transport = AndroidCcidTransport(ApplicationProvider.getApplicationContext<Context>())
        // The emulator has no physical reader. If CI adds a virtual reader, avoid assuming its permission.
        if (transport.devices().isEmpty()) {
            assertEquals(SourceAvailability.UNAVAILABLE, transport.availability())
            assertEquals(ReaderState.Absent, transport.open())
        }
        transport.close()
    }
}
