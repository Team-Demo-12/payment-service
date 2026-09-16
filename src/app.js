'use strict';

const express = require('express');
const config = require('./config');
const paymentRoutes = require('./routes/paymentRoutes');
const { authorizationRepository } = require('./repositories/authorizationRepository');
const { requestId, rateLimit, apiKeyAuth, accessLog } = require('./middleware/requestContext');

function createApp() {
  const app = express();

  app.use(express.json({ limit: '1mb' }));
  app.use(requestId);
  app.use(accessLog);

  app.get('/health', (req, res) =>
    res.json({ status: 'ok', service: config.serviceName, release: config.release }),
  );
  app.get('/ready', (req, res) => res.json({ status: 'ready' }));
  app.get('/metrics', (req, res) =>
    res.json({
      service: config.serviceName,
      release: config.release,
      authorizations: authorizationRepository.size(),
      byNetwork: authorizationRepository.countByNetwork(),
    }),
  );

  app.use(rateLimit);
  app.use(apiKeyAuth);
  app.use(paymentRoutes);

  app.use((req, res) => res.status(404).json({ error: 'NotFound', path: req.originalUrl }));

  return app;
}

module.exports = { createApp };
