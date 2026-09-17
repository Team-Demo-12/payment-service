package com.beaconstone.payments

import com.beaconstone.payments.config.authProfiles
import com.beaconstone.payments.services.Payment
import com.beaconstone.payments.services.authorizePayment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Card-network profile-selection coverage.
 *
 * Authorisation resolves the default profile, which every supported network is
 * certified against.
 */
class AuthorizationProfileCardTest {
    @Test
    fun `default profile is card-default`() {
        assertEquals("card-default", authProfiles.default.id)
    }

    @Test
    fun `visa and mastercard authorise against the default profile`() {
        for (network in listOf("visa", "mastercard")) {
            val record = authorizePayment(
                Payment(
                    paymentId = "pay_${network}_1",
                    invoiceId = "inv_${network}_1",
                    network = network,
                    amountMinor = 2500,
                    currency = "USD",
                ),
            )
            assertEquals("card-default", record.profileId)
            assertEquals("authorized", record.status)
        }
    }
}
