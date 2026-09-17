package com.beaconstone.payments.controllers

import com.beaconstone.payments.config.authProfiles
import com.beaconstone.payments.config.config
import com.beaconstone.payments.errors.PaymentAuthorizationConfigurationError
import com.beaconstone.payments.repositories.authorizationRepository
import com.beaconstone.payments.services.Payment
import com.beaconstone.payments.services.authorizePayment
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Emit the authorisation log line consumed by the payment dashboards. The field order is
 * fixed because the log parser indexes on it.
 */
fun logAuthorizationFailure(network: String?, error: PaymentAuthorizationConfigurationError, httpStatus: Int) {
    val ts = Instant.now().truncatedTo(ChronoUnit.MILLIS).toString()
    val fields = listOf(
        "service=${config.serviceName}",
        "release=${config.release}",
        "route=/payments/authorize",
        "card_network=${error.cardNetwork ?: network ?: "unknown"}",
        "http_status=$httpStatus",
        "error=${error.errorName}",
        "profile=${error.selectedProfile ?: "none"}",
        "message=\"${error.message}\"",
    )
    System.err.println("$ts ERROR ${fields.joinToString(" ")}")
}

fun authorize(payment: Payment): Pair<Int, Map<String, Any?>> {
    val existing = authorizationRepository.findByIdempotencyKey(payment.idempotencyKey)
    if (existing != null) {
        return 201 to existing.toResponse()
    }

    if (payment.amountMinor <= 0) {
        return 422 to mapOf("error" to "InvalidAmount")
    }
    if (!authProfiles.isSupported(payment.network)) {
        return 422 to mapOf("error" to "UnsupportedNetwork")
    }

    return try {
        val record = authorizePayment(payment)
        System.out.println(
            """{"level":"info","event":"authorization.succeeded","service":"${config.serviceName}","release":"${config.release}","authorizationId":"${record.authorizationId}","network":"${record.network}","profileId":"${record.profileId}"}""",
        )
        201 to record.toResponse()
    } catch (error: PaymentAuthorizationConfigurationError) {
        authorizationRepository.recordFailure(payment.network)
        logAuthorizationFailure(payment.network, error, error.httpStatus)
        error.httpStatus to error.toResponse()
    }
}

private fun com.beaconstone.payments.repositories.AuthorizationRecord.toResponse(): Map<String, Any?> = mapOf(
    "authorizationId" to authorizationId,
    "paymentId" to paymentId,
    "invoiceId" to invoiceId,
    "network" to network,
    "profileId" to profileId,
    "captureMode" to captureMode,
    "authorizationCode" to authorizationCode,
    "status" to status,
    "authorizedAt" to authorizedAt,
)
