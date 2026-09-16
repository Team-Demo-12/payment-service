'use strict';

const { PaymentAuthorizationConfigurationError } = require('../errors/PaymentAuthorizationConfigurationError');

const ACCEPTED_PROFILE_IDS = ['card-default'];

/**
 * Visa adapter. Certified against the shared default profile.
 */
function authorize(payment, profile) {
  if (!profile || !ACCEPTED_PROFILE_IDS.includes(profile.id)) {
    throw new PaymentAuthorizationConfigurationError(
      'Visa requires a profile certified against the default acquirer BIN',
      {
        cardNetwork: 'visa',
        selectedProfile: profile ? profile.id : null,
        expectedProfile: ACCEPTED_PROFILE_IDS[0],
      },
    );
  }

  return {
    network: 'visa',
    profileId: profile.id,
    captureMode: profile.captureMode,
    authorizationCode: `VI${String(payment.amountMinor).padStart(6, '0')}`,
    approved: true,
  };
}

module.exports = { authorize, ACCEPTED_PROFILE_IDS };
