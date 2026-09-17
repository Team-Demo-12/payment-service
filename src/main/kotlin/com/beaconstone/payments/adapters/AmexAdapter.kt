package com.beaconstone.payments.adapters

import com.beaconstone.payments.config.AuthProfile
import com.beaconstone.payments.errors.PaymentAuthorizationConfigurationError
import com.beaconstone.payments.services.Payment

private const val REQUIRED_PROFILE_ID = "amex-network"

/**
 * American Express adapter.
 *
 * Beaconstone's AMEX agreement is certified against a dedicated acquirer BIN, so the
 * adapter refuses any profile other than its own. Presenting the shared default profile
 * is a misconfiguration the acquirer would reject downstream, so it is rejected here.
 */
object AmexAdapter : CardNetworkAdapter {
    override fun authorize(payment: Payment, profile: AuthProfile): AuthorizationResult {
        if (profile.id != REQUIRED_PROFILE_ID) {
            throw PaymentAuthorizationConfigurationError(
                "AMEX requires its configured network-specific authorisation profile",
                cardNetwork = "amex",
                selectedProfile = profile.id,
                expectedProfile = REQUIRED_PROFILE_ID,
            )
        }

        return AuthorizationResult(
            network = "amex",
            profileId = profile.id,
            captureMode = profile.captureMode,
            authorizationCode = "AX" + payment.amountMinor.toString().padStart(6, '0'),
            approved = true,
            acquirerBin = profile.acquirerBin,
        )
    }
}
