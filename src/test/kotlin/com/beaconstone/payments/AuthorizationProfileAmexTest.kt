package com.beaconstone.payments

import com.beaconstone.payments.adapters.AmexAdapter
import com.beaconstone.payments.config.authProfiles
import com.beaconstone.payments.errors.PaymentAuthorizationConfigurationError
import com.beaconstone.payments.services.Payment
import com.beaconstone.payments.services.authorizePayment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * AMEX profile-selection regression coverage.
 *
 * Beaconstone's AMEX agreement is certified against a dedicated acquirer BIN. If the
 * authorisation path ever selects the shared default profile for AMEX, every AMEX
 * authorisation fails. These cases pin that behaviour.
 */
class AuthorizationProfileAmexTest {
    @Test
    fun `selects the network-specific profile for amex`() {
        assertEquals("amex-network", authProfiles.forNetwork("amex").id)
        assertTrue(authProfiles.forNetwork("amex").requiresNetworkProfile)
    }

    @Test
    fun `the amex adapter rejects the shared default profile`() {
        val error = assertThrows<PaymentAuthorizationConfigurationError> {
            AmexAdapter.authorize(
                Payment(paymentId = "pay_amex_reject", invoiceId = "inv_amex_reject", network = "amex", amountMinor = 1000),
                authProfiles.default,
            )
        }
        assertEquals("AMEX requires its configured network-specific authorisation profile", error.message)
        assertEquals("card-default", error.selectedProfile)
    }

    @Test
    fun `an amex payment authorises end to end on the network-specific profile`() {
        val record = authorizePayment(
            Payment(
                paymentId = "pay_amex_1",
                invoiceId = "inv_amex_1",
                network = "amex",
                amountMinor = 4200,
                currency = "USD",
            ),
        )
        assertEquals("amex-network", record.profileId)
        assertEquals("authorized", record.status)
    }
}
