'use strict';

const { EventEmitter } = require('events');

const PAYMENT_SUCCEEDED = 'PaymentSucceeded';

const paymentEvents = new EventEmitter();

/**
 * Publish PaymentSucceeded. Invoicing consumes this event to mark an invoice as paid,
 * so an authorisation that never completes leaves its invoice outstanding.
 */
function emitPaymentSucceeded(record) {
  const event = {
    type: PAYMENT_SUCCEEDED,
    occurredAt: new Date().toISOString(),
    paymentId: record.paymentId,
    invoiceId: record.invoiceId,
    network: record.network,
    amountMinor: record.amountMinor,
    currency: record.currency,
  };
  paymentEvents.emit(PAYMENT_SUCCEEDED, event);
  return event;
}

module.exports = { paymentEvents, emitPaymentSucceeded, PAYMENT_SUCCEEDED };
