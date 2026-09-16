'use strict';

const { authProfiles } = require('../config/authProfiles');
const { adapterFor } = require('../adapters');
const { authorizationRepository } = require('../repositories/authorizationRepository');
const { emitPaymentSucceeded } = require('../events/paymentEvents');

/**
 * Authorise a single payment against its card network.
 *
 * The network's authorisation profile is resolved first, then handed to the adapter for
 * that network. Adapters validate the profile they are given, because presenting an
 * uncertified profile to the acquirer fails downstream in a way that is hard to diagnose.
 */
function authorizePayment(payment) {
  const profile = authProfiles.forNetwork(payment.network);
  const adapter = adapterFor(payment.network);

  const result = adapter.authorize(payment, profile);

  const record = authorizationRepository.save({
    paymentId: payment.paymentId,
    invoiceId: payment.invoiceId,
    network: payment.network,
    profileId: result.profileId,
    captureMode: result.captureMode,
    authorizationCode: result.authorizationCode,
    amountMinor: payment.amountMinor,
    capturedMinor: payment.capturedMinor || payment.amountMinor,
    currency: payment.currency,
    status: 'authorized',
  });

  emitPaymentSucceeded(record);

  return record;
}

module.exports = { authorizePayment };
