'use strict';

const assert = require('assert');
const { authProfiles } = require('../src/config/authProfiles');
const amexAdapter = require('../src/adapters/amexAdapter');
const { authorizePayment } = require('../src/services/authorizationService');

/**
 * AMEX profile-selection regression coverage.
 *
 * Beaconstone's AMEX agreement is certified against a dedicated acquirer BIN. If the
 * authorisation path ever selects the shared default profile for AMEX, every AMEX
 * authorisation fails. These cases pin that behaviour.
 */

// selects the network-specific profile for amex
assert.strictEqual(authProfiles.forNetwork('amex').id, 'amex-network');
assert.strictEqual(authProfiles.forNetwork('amex').requiresNetworkProfile, true);

// the amex adapter rejects the shared default profile
assert.throws(
  () => amexAdapter.authorize({ amountMinor: 1000 }, authProfiles.default),
  /AMEX requires its configured network-specific authorisation profile/,
);

// an amex payment authorises end to end on the network-specific profile
const record = authorizePayment({
  paymentId: 'pay_amex_1',
  invoiceId: 'inv_amex_1',
  network: 'amex',
  amountMinor: 4200,
  currency: 'USD',
});
assert.strictEqual(record.profileId, 'amex-network');
assert.strictEqual(record.status, 'authorized');

process.stdout.write('authorization-profile.amex.test.js passed\n');
