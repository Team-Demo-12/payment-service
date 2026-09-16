'use strict';

const assert = require('assert');
const { authProfiles } = require('../src/config/authProfiles');
const { authorizePayment } = require('../src/services/authorizationService');

/**
 * Card-network profile-selection coverage.
 *
 * Authorisation resolves the default profile, which every supported network is
 * certified against.
 */

assert.strictEqual(authProfiles.default.id, 'card-default');

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
