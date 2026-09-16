'use strict';

const assert = require('assert');
const { authProfiles } = require('../src/config/authProfiles');
const { authorizePayment } = require('../src/services/authorizationService');

/**
 * Visa and Mastercard profile-selection coverage. Both networks are certified against
 * the shared default profile.
 */

assert.strictEqual(authProfiles.forNetwork('visa').id, 'card-default');
assert.strictEqual(authProfiles.forNetwork('mastercard').id, 'card-default');

for (const network of ['visa', 'mastercard']) {
  const record = authorizePayment({
    paymentId: `pay_${network}_1`,
    invoiceId: `inv_${network}_1`,
    network,
    amountMinor: 2500,
    currency: 'USD',
  });
  assert.strictEqual(record.profileId, 'card-default');
  assert.strictEqual(record.status, 'authorized');
}

process.stdout.write('authorization-profile.card.test.js passed\n');
