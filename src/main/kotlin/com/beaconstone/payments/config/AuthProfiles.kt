package com.beaconstone.payments.config

/**
 * Card-network authorisation profiles.
 *
 * Beaconstone's acquirer requires American Express traffic to be presented with its own
 * network-specific profile. Visa and Mastercard are certified against the shared default
 * profile, so they resolve to `card-default`.
 */
data class AuthProfile(
    val id: String,
    val captureMode: String,
    val requiresNetworkProfile: Boolean,
    val merchantCategoryCode: String,
    val acquirerBin: String,
)

object authProfiles {
    /**
     * The shared profile. Valid for every network certified against the default acquirer BIN.
     * It is NOT valid for networks whose adapter sets `requiresNetworkProfile`.
     */
    val default = AuthProfile(
        id = "card-default",
        captureMode = "immediate",
        requiresNetworkProfile = false,
        merchantCategoryCode = "6012",
        acquirerBin = "445566",
    )

    private val NETWORK_PROFILES = mapOf(
        "amex" to AuthProfile(
            id = "amex-network",
            captureMode = "delayed",
            requiresNetworkProfile = true,
            merchantCategoryCode = "6012",
            acquirerBin = "378282",
        ),
        "visa" to default,
        "mastercard" to default,
    )

    /**
     * Resolve the profile a specific card network must be authorised with.
     */
    fun forNetwork(network: String?): AuthProfile =
        NETWORK_PROFILES[network.orEmpty().lowercase()] ?: default

    fun isSupported(network: String?): Boolean =
        supportedNetworks().contains(network.orEmpty().lowercase())

    fun supportedNetworks(): List<String> = NETWORK_PROFILES.keys.toList()
}
