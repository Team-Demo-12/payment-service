package com.beaconstone.payments.adapters

import com.beaconstone.payments.config.AuthProfile
import com.beaconstone.payments.services.Payment

data class AuthorizationResult(
    val network: String,
    val profileId: String,
    val captureMode: String,
    val authorizationCode: String,
    val approved: Boolean,
    val acquirerBin: String? = null,
    val installmentPlan: String? = null,
)

interface CardNetworkAdapter {
    fun authorize(payment: Payment, profile: AuthProfile): AuthorizationResult
}

fun adapterFor(network: String?): CardNetworkAdapter =
    when (network.orEmpty().lowercase()) {
        "amex" -> AmexAdapter
        "visa" -> VisaAdapter
        "mastercard" -> MastercardAdapter
        else -> {
            val error = IllegalArgumentException("Unsupported card network: $network")
            throw error
        }
    }
