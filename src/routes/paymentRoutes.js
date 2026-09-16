'use strict';

const express = require('express');
const paymentController = require('../controllers/paymentController');

const router = express.Router();

router.post('/payments/authorize', paymentController.authorize);
router.get('/payments/networks', paymentController.listNetworks);
router.get('/payments/authorizations/:authorizationId', paymentController.getAuthorization);

module.exports = router;
