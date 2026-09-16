'use strict';

const { PaymentAuthorizationConfigurationError } = require('../errors/PaymentAuthorizationConfigurationError');

const REQUIRED_PROFILE_ID = 'amex-network';

/**
 * American Express adapter.
 *
 * Beaconstone's AMEX agreement is certified against a dedicated acquirer BIN, so the
 * adapter refuses any profile other than its own. Presenting the shared default profile
 * is a misconfiguration the acquirer would reject downstream, so it is rejected here.
 */
function authorize(payment, profile) {
  if (!profile || profile.id !== REQUIRED_PROFILE_ID) {
    throw new PaymentAuthorizationConfigurationError(
      'AMEX requires its configured network-specific authorisation profile',
      {
        cardNetwork: 'amex',
        selectedProfile: profile ? profile.id : null,
        expectedProfile: REQUIRED_PROFILE_ID,
      },
    );
  }

  return {
    network: 'amex',
    profileId: profile.id,
    captureMode: profile.captureMode,
    authorizationCode: `AX${String(payment.amountMinor).padStart(6, '0')}`,
    approved: true,
  };
}

module.exports = { authorize, REQUIRED_PROFILE_ID };
