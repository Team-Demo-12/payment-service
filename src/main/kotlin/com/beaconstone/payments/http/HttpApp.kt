package com.beaconstone.payments.http

import com.beaconstone.payments.config.authProfiles
import com.beaconstone.payments.config.config
import com.beaconstone.payments.controllers.authorize
import com.beaconstone.payments.repositories.authorizationRepository
import com.beaconstone.payments.services.Payment
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

private val mapper = jacksonObjectMapper()

fun createServer(): HttpServer {
    val server = HttpServer.create(InetSocketAddress(config.port), 0)
    server.createContext("/health", ::health)
    server.createContext("/ready") { exchange -> json(exchange, 200, mapOf("status" to "ready")) }
    server.createContext("/metrics", ::metrics)
    server.createContext("/payments/networks", ::networks)
    server.createContext("/payments/authorize", ::authorizeRoute)
    server.createContext("/payments/authorizations/", ::getAuthorization)
    server.createContext("/payments/invoices/") { exchange -> listByInvoice(exchange) }
    server.executor = null
    return server
}

private fun health(exchange: HttpExchange) {
    json(exchange, 200, mapOf("status" to "ok", "service" to config.serviceName, "release" to config.release))
}

private fun metrics(exchange: HttpExchange) {
    json(
        exchange,
        200,
        mapOf(
            "service" to config.serviceName,
            "release" to config.release,
            "authorizations" to authorizationRepository.size(),
            "byNetwork" to authorizationRepository.countByNetwork(),
            "failuresByNetwork" to authorizationRepository.failuresByNetwork(),
        ),
    )
}

private fun networks(exchange: HttpExchange) {
    if (exchange.requestMethod != "GET") {
        json(exchange, 405, mapOf("error" to "MethodNotAllowed"))
        return
    }
    json(
        exchange,
        200,
        mapOf(
            "networks" to authProfiles.supportedNetworks().map { network ->
                mapOf("network" to network, "profileId" to authProfiles.forNetwork(network).id)
            },
        ),
    )
}

private fun authorizeRoute(exchange: HttpExchange) {
    if (exchange.requestMethod != "POST") {
        json(exchange, 405, mapOf("error" to "MethodNotAllowed"))
        return
    }
    if (!allow(exchange)) return

    val body: Map<String, Any?> = mapper.readValue(exchange.requestBody)
    val payment = Payment(
        paymentId = body["paymentId"] as String?,
        invoiceId = body["invoiceId"] as String?,
        network = body["network"] as String?,
        amountMinor = (body["amountMinor"] as? Number)?.toInt() ?: 0,
        currency = (body["currency"] as String?) ?: "USD",
        capturedMinor = (body["capturedMinor"] as? Number)?.toInt(),
        idempotencyKey = exchange.requestHeaders.getFirst("Idempotency-Key"),
        installmentPlan = body["installmentPlan"] as String?,
    )
    val (status, payload) = authorize(payment)
    json(exchange, status, payload + ("requestId" to requestId(exchange)))
}

private fun getAuthorization(exchange: HttpExchange) {
    val id = exchange.requestURI.path.removePrefix("/payments/authorizations/").substringBefore('/')
    if (id.isBlank() || id.contains('/')) {
        json(exchange, 404, mapOf("error" to "NotFound"))
        return
    }
    val record = authorizationRepository.findById(id)
    if (record == null) {
        json(exchange, 404, mapOf("error" to "NotFound", "message" to "Authorization not found"))
        return
    }
    json(exchange, 200, record)
}

private fun listByInvoice(exchange: HttpExchange) {
    val path = exchange.requestURI.path
    val match = Regex("^/payments/invoices/([^/]+)/authorizations$").matchEntire(path)
    if (match == null) {
        json(exchange, 404, mapOf("error" to "NotFound", "path" to path))
        return
    }
    val invoiceId = match.groupValues[1]
    json(
        exchange,
        200,
        mapOf(
            "invoiceId" to invoiceId,
            "authorizations" to authorizationRepository.findByInvoice(invoiceId),
        ),
    )
}

private val buckets = ConcurrentHashMap<String, Pair<Int, Long>>()

private fun allow(exchange: HttpExchange): Boolean {
    val apiKey = config.apiKey
    if (apiKey != null && exchange.requestHeaders.getFirst("x-api-key") != apiKey) {
        json(exchange, 401, mapOf("error" to "Unauthorized", "requestId" to requestId(exchange)))
        return false
    }

    val now = System.currentTimeMillis()
    val key = (exchange.remoteAddress?.address?.hostAddress ?: "anonymous") + ":" + exchange.requestURI.path
    val (count, resetAt) = buckets[key] ?: (0 to now + config.rateLimitWindowMs)
    val next = if (now > resetAt) 1 to now + config.rateLimitWindowMs else (count + 1) to resetAt
    buckets[key] = next
    if (next.first > config.rateLimitMax) {
        json(exchange, 429, mapOf("error" to "RateLimited", "requestId" to requestId(exchange)))
        return false
    }
    return true
}

private fun requestId(exchange: HttpExchange): String =
    exchange.requestHeaders.getFirst("x-request-id") ?: UUID.randomUUID().toString()

private fun json(exchange: HttpExchange, status: Int, payload: Any) {
    val bytes = mapper.writeValueAsBytes(payload)
    exchange.responseHeaders.set("Content-Type", "application/json")
    exchange.responseHeaders.set("x-request-id", requestId(exchange))
    exchange.sendResponseHeaders(status, bytes.size.toLong())
    exchange.responseBody.use { it.write(bytes) }
}
