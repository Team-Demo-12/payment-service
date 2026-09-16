'use strict';

const { randomUUID } = require('crypto');

/**
 * In-memory authorisation store. The demo service keeps no external database.
 */
const store = new Map();

const authorizationRepository = {
  save(record) {
    const saved = {
      authorizationId: randomUUID(),
      authorizedAt: new Date().toISOString(),
      ...record,
    };
    store.set(saved.authorizationId, saved);
    return saved;
  },

  findById(authorizationId) {
    return store.get(authorizationId) || null;
  },

  findByInvoice(invoiceId) {
    return [...store.values()].filter((record) => record.invoiceId === invoiceId);
  },

  countByNetwork() {
    return [...store.values()].reduce((counts, record) => {
      counts[record.network] = (counts[record.network] || 0) + 1;
      return counts;
    }, {});
  },

  size() {
    return store.size;
  },
};

module.exports = { authorizationRepository };
