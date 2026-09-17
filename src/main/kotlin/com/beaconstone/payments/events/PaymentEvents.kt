package com.beaconstone.payments.events

import com.beaconstone.payments.repositories.AuthorizationRecord
import java.time.Instant

const val PAYMENT_SUCCEEDED = "PaymentSucceeded"

data class PaymentSucceededEvent(
    val type: String = PAYMENT_SUCCEEDED,
    val occurredAt: String = Instant.now().toString(),
    val paymentId: String?,
    val invoiceId: String?,
    val network: String?,
    val profileId: String?,
    val captureMode: String?,
    val amountMinor: Int,
    val currency: String?,
)

/**
 * Publish PaymentSucceeded. Invoicing consumes this event to mark an invoice as paid,
 * so an authorisation that never completes leaves its invoice outstanding.
 */
fun emitPaymentSucceeded(record: AuthorizationRecord): PaymentSucceededEvent {
    val event = PaymentSucceededEvent(
        paymentId = record.paymentId,
        invoiceId = record.invoiceId,
        network = record.network,
        profileId = record.profileId,
        captureMode = record.captureMode,
        amountMinor = record.amountMinor,
        currency = record.currency,
    )
    System.out.println(
        """{"level":"info","event":"PaymentSucceeded","invoiceId":"${record.invoiceId}","authorizationId":"${record.authorizationId}","network":"${record.network}","profileId":"${record.profileId}"}""",
    )
    return event
}
