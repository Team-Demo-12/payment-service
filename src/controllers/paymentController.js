'use strict';

const { authorizePayment } = require('../services/authorizationService');
const { PaymentAuthorizationConfigurationError } = require('../errors/PaymentAuthorizationConfigurationError');
const config = require('../config');

/**
 * Emit the authorisation log line consumed by the payment dashboards. The field order is
 * fixed because the log parser indexes on it.
 */
function logAuthorizationFailure(req, error, httpStatus) {
  const fields = [
    `service=${config.serviceName}`,
    `release=${config.release}`,
    `route=/payments/authorize`,
    `card_network=${error.cardNetwork || req.body.network || 'unknown'}`,
    `http_status=${httpStatus}`,
    `error=${error.name}`,
    `profile=${error.selectedProfile || 'none'}`,
    `message="${error.message}"`,
  ];
  process.stderr.write(`${new Date().toISOString()} ERROR ${fields.join(' ')}\n`);
}

function authorize(req, res) {
  const payment = {
    paymentId: req.body.paymentId,
    invoiceId: req.body.invoiceId,
    network: req.body.network,
    amountMinor: req.body.amountMinor,
    currency: req.body.currency || 'USD',
    capturedMinor: req.body.capturedMinor || null,
    idempotencyKey: req.get('idempotency-key') || null,
  };

  const { authProfiles } = require('../config/authProfiles');
  if (!Number.isInteger(payment.amountMinor) || payment.amountMinor <= 0) {
    return res.status(422).json({ error: 'InvalidAmount', requestId: req.requestId });
  }
  if (!authProfiles.isSupported(payment.network)) {
    return res.status(422).json({ error: 'UnsupportedNetwork', requestId: req.requestId });
  }

  try {
    const record = authorizePayment(payment);
    return res.status(201).json({
      authorizationId: record.authorizationId,
      paymentId: record.paymentId,
      invoiceId: record.invoiceId,
      network: record.network,
      profileId: record.profileId,
      authorizationCode: record.authorizationCode,
      status: record.status,
      authorizedAt: record.authorizedAt,
    });
  } catch (error) {
    if (error instanceof PaymentAuthorizationConfigurationError) {
      logAuthorizationFailure(req, error, error.httpStatus);
      return res.status(error.httpStatus).json({
        error: error.name,
        message: error.message,
        cardNetwork: error.cardNetwork,
        requestId: req.requestId,
      });
    }

    const status = error.httpStatus || 500;
    process.stderr.write(
      `${new Date().toISOString()} ERROR service=${config.serviceName} release=${config.release} ` +
        `route=/payments/authorize http_status=${status} error=${error.name} message="${error.message}"\n`,
    );
    return res.status(status).json({ error: error.name, message: error.message, requestId: req.requestId });
  }
}

function getAuthorization(req, res) {
  const { authorizationRepository } = require('../repositories/authorizationRepository');
  const record = authorizationRepository.findById(req.params.authorizationId);
  if (!record) {
    return res.status(404).json({ error: 'NotFound', message: 'Authorization not found' });
  }
  return res.json(record);
}

function listNetworks(req, res) {
  const { authProfiles } = require('../config/authProfiles');
  return res.json({
    networks: authProfiles.supportedNetworks().map((network) => ({
      network,
      profileId: authProfiles.forNetwork(network).id,
    })),
  });
}

module.exports = { authorize, getAuthorization, listNetworks };
