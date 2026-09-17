package com.beaconstone.payments.services

data class Payment(
    val paymentId: String?,
    val invoiceId: String?,
    val network: String?,
    val amountMinor: Int,
    val currency: String = "USD",
    val capturedMinor: Int? = null,
    val idempotencyKey: String? = null,
    val installmentPlan: String? = null,
)
