package com.beaconstone.payments.adapters

import com.beaconstone.payments.config.AuthProfile
import com.beaconstone.payments.errors.PaymentAuthorizationConfigurationError
import com.beaconstone.payments.services.Payment

private val ACCEPTED_PROFILE_IDS = setOf("card-default")

/** Visa adapter. Certified against the shared default profile. */
object VisaAdapter : CardNetworkAdapter {
    override fun authorize(payment: Payment, profile: AuthProfile): AuthorizationResult {
        if (profile.id !in ACCEPTED_PROFILE_IDS) {
            throw PaymentAuthorizationConfigurationError(
                "Visa requires a profile certified against the default acquirer BIN",
                cardNetwork = "visa",
                selectedProfile = profile.id,
                expectedProfile = "card-default",
            )
        }

        return AuthorizationResult(
            network = "visa",
            profileId = profile.id,
            captureMode = profile.captureMode,
            authorizationCode = "VI" + payment.amountMinor.toString().padStart(6, '0'),
            approved = true,
            acquirerBin = profile.acquirerBin,
        )
    }
}
