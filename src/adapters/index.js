'use strict';

const amexAdapter = require('./amexAdapter');
const visaAdapter = require('./visaAdapter');
const mastercardAdapter = require('./mastercardAdapter');

const ADAPTERS = {
  amex: amexAdapter,
  visa: visaAdapter,
  mastercard: mastercardAdapter,
};

function adapterFor(network) {
  const adapter = ADAPTERS[String(network || '').toLowerCase()];
  if (!adapter) {
    const error = new Error(`Unsupported card network: ${network}`);
    error.httpStatus = 422;
    throw error;
  }
  return adapter;
}

module.exports = { adapterFor, ADAPTERS };
