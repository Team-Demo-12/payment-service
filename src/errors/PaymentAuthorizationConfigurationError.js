'use strict';

/**
 * Raised when a card-network adapter is handed an authorisation profile it is not
 * certified to use. This is a configuration fault, not a customer or issuer outcome,
 * so it surfaces as HTTP 500 rather than a decline.
 */
class PaymentAuthorizationConfigurationError extends Error {
  constructor(message, context = {}) {
    super(message);
    this.name = 'PaymentAuthorizationConfigurationError';
    this.cardNetwork = context.cardNetwork || null;
    this.selectedProfile = context.selectedProfile || null;
    this.expectedProfile = context.expectedProfile || null;
    this.httpStatus = 500;
    Error.captureStackTrace(this, PaymentAuthorizationConfigurationError);
  }

  toLogFields() {
    return {
      error: this.name,
      card_network: this.cardNetwork,
      profile: this.selectedProfile,
      expected_profile: this.expectedProfile,
    };
  }
}

module.exports = { PaymentAuthorizationConfigurationError };
