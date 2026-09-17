package com.beaconstone.payments.repositories

import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class AuthorizationRecord(
    val authorizationId: String = UUID.randomUUID().toString(),
    val authorizedAt: String = Instant.now().toString(),
    val paymentId: String? = null,
    val invoiceId: String? = null,
    val network: String? = null,
    val profileId: String? = null,
    val captureMode: String? = null,
    val installmentPlan: String? = null,
    val authorizationCode: String? = null,
    val amountMinor: Int = 0,
    val capturedMinor: Int = 0,
    val currency: String = "USD",
    val status: String = "authorized",
    val idempotencyKey: String? = null,
)

/**
 * In-memory authorisation store. The demo service keeps no external database.
 */
object authorizationRepository {
    private val store = ConcurrentHashMap<String, AuthorizationRecord>()
    private val failures = ConcurrentHashMap<String, Int>()

    fun save(record: AuthorizationRecord): AuthorizationRecord {
        store[record.authorizationId] = record
        return record
    }

    fun findById(authorizationId: String): AuthorizationRecord? = store[authorizationId]

    fun findByIdempotencyKey(idempotencyKey: String?): AuthorizationRecord? {
        if (idempotencyKey.isNullOrBlank()) return null
        return store.values.find { it.idempotencyKey == idempotencyKey }
    }

    fun findByInvoice(invoiceId: String): List<AuthorizationRecord> =
        store.values.filter { it.invoiceId == invoiceId }

    fun recordFailure(network: String?) {
        val key = network ?: "unknown"
        failures.merge(key, 1, Int::plus)
    }

    fun failuresByNetwork(): Map<String, Int> = failures.toMap()

    fun countByNetwork(): Map<String, Int> =
        store.values.groupingBy { it.network ?: "unknown" }.eachCount()

    fun size(): Int = store.size
}
