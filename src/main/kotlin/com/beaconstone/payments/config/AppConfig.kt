package com.beaconstone.payments.config

data class AppConfig(
    val port: Int,
    val serviceName: String,
    val serviceVersion: String,
    val release: String,
    val apiKey: String?,
    val rateLimitWindowMs: Long,
    val rateLimitMax: Int,
)

fun loadConfig(): AppConfig {
    val version = System.getenv("SERVICE_VERSION") ?: "4.17.3"
    return AppConfig(
        port = System.getenv("PORT")?.toIntOrNull() ?: 8080,
        serviceName = System.getenv("SERVICE_NAME") ?: "payment-service",
        serviceVersion = version,
        release = (System.getenv("RELEASE") ?: "payments-$version").trim(),
        apiKey = System.getenv("API_KEY")?.ifBlank { null },
        rateLimitWindowMs = System.getenv("RATE_LIMIT_WINDOW_MS")?.toLongOrNull() ?: 60_000L,
        rateLimitMax = System.getenv("RATE_LIMIT_MAX")?.toIntOrNull() ?: 100,
    )
}

val config = loadConfig()
