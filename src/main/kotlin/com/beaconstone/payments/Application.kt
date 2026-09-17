package com.beaconstone.payments

import com.beaconstone.payments.config.config
import com.beaconstone.payments.http.createServer

fun main() {
    val server = createServer()
    server.start()
    println(
        """{"level":"info","message":"payment-service started","service":"${config.serviceName}","release":"${config.release}","port":${config.port}}""",
    )
    Runtime.getRuntime().addShutdownHook(Thread {
        println("""{"level":"info","message":"received SIGTERM"}""")
        server.stop(1)
    })
}
