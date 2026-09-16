'use strict';

/**
 * Card-network authorisation profiles.
 *
 * Beaconstone's acquirer requires American Express traffic to be presented with its own
 * network-specific profile. Visa and Mastercard are certified against the shared default
 * profile, so they resolve to `card-default`.
 */

const DEFAULT_PROFILE = Object.freeze({
  id: 'card-default',
  captureMode: 'immediate',
  requiresNetworkProfile: false,
  merchantCategoryCode: '6012',
  acquirerBin: '445566',
});

const NETWORK_PROFILES = Object.freeze({
  amex: Object.freeze({
    id: 'amex-network',
    captureMode: 'delayed',
    requiresNetworkProfile: true,
    merchantCategoryCode: '6012',
    acquirerBin: '378282',
  }),
  visa: DEFAULT_PROFILE,
  mastercard: DEFAULT_PROFILE,
});

const authProfiles = {
  /**
   * The shared profile. Valid for every network certified against the default acquirer BIN.
   * It is NOT valid for networks whose adapter sets `requiresNetworkProfile`.
   */
  default: DEFAULT_PROFILE,

  /**
   * Resolve the profile a specific card network must be authorised with.
   */
  forNetwork(network) {
    return NETWORK_PROFILES[String(network || '').toLowerCase()] || DEFAULT_PROFILE;
  },

  isSupported(network) {
    return this.supportedNetworks().includes(String(network || '').toLowerCase());
  },

  supportedNetworks() {
    return Object.keys(NETWORK_PROFILES);
  },
};

module.exports = { authProfiles, DEFAULT_PROFILE, NETWORK_PROFILES };
