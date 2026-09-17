package com.beaconstone.payments.errors

/**
 * Raised when a card-network adapter is handed an authorisation profile it is not
 * certified to use. This is a configuration fault, not a customer or issuer outcome,
 * so it surfaces as HTTP 500 rather than a decline.
 */
class PaymentAuthorizationConfigurationError(
    message: String,
    val cardNetwork: String? = null,
    val selectedProfile: String? = null,
    val expectedProfile: String? = null,
) : RuntimeException(message) {
    val httpStatus: Int = 500
    val errorName: String = "PaymentAuthorizationConfigurationError"

    fun toResponse(): Map<String, Any?> = mapOf(
        "error" to errorName,
        "message" to message,
        "cardNetwork" to cardNetwork,
        "selectedProfile" to selectedProfile,
        "expectedProfile" to expectedProfile,
    )
}
