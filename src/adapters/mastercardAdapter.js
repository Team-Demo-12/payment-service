'use strict';

const { PaymentAuthorizationConfigurationError } = require('../errors/PaymentAuthorizationConfigurationError');

const ACCEPTED_PROFILE_IDS = ['card-default'];

/**
 * Mastercard adapter. Certified against the shared default profile.
 */
function authorize(payment, profile) {
  if (!profile || !ACCEPTED_PROFILE_IDS.includes(profile.id)) {
    throw new PaymentAuthorizationConfigurationError(
      'Mastercard requires a profile certified against the default acquirer BIN',
      {
        cardNetwork: 'mastercard',
        selectedProfile: profile ? profile.id : null,
        expectedProfile: ACCEPTED_PROFILE_IDS[0],
      },
    );
  }

  return {
    network: 'mastercard',
    profileId: profile.id,
    captureMode: profile.captureMode,
    installmentPlan: payment.installmentPlan || null,
    authorizationCode: `MC${String(payment.amountMinor).padStart(6, '0')}`,
    approved: true,
  };
}

module.exports = { authorize, ACCEPTED_PROFILE_IDS };
