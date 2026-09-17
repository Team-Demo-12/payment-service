package com.beaconstone.payments.adapters

import com.beaconstone.payments.config.AuthProfile
import com.beaconstone.payments.errors.PaymentAuthorizationConfigurationError
import com.beaconstone.payments.services.Payment

private val ACCEPTED_PROFILE_IDS = setOf("card-default")

/** Mastercard adapter. Certified against the shared default profile. */
object MastercardAdapter : CardNetworkAdapter {
    override fun authorize(payment: Payment, profile: AuthProfile): AuthorizationResult {
        if (profile.id !in ACCEPTED_PROFILE_IDS) {
            throw PaymentAuthorizationConfigurationError(
                "Mastercard requires a profile certified against the default acquirer BIN",
                cardNetwork = "mastercard",
                selectedProfile = profile.id,
                expectedProfile = "card-default",
            )
        }

        return AuthorizationResult(
            network = "mastercard",
            profileId = profile.id,
            captureMode = profile.captureMode,
            authorizationCode = "MC" + payment.amountMinor.toString().padStart(6, '0'),
            approved = true,
            acquirerBin = profile.acquirerBin,
            installmentPlan = payment.installmentPlan,
        )
    }
}
