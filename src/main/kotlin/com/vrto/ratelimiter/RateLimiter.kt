package com.vrto.ratelimiter

import com.vrto.ratelimiter.Result.Dropped
import com.vrto.ratelimiter.Result.Forwarded
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.timerTask

interface Request {
    val id: String
}

sealed class Result(val req: Request) {
    class Forwarded(req: Request) : Result(req)
    class Dropped(req: Request) : Result(req)
}

class RateLimiter(val capacity: Int = 10, val refillRateMillis: Long = 100) {

    private val tokenBucket = AtomicInteger(capacity)
    val remaining: Int get() = tokenBucket.get()

    init {
        require(capacity in 1..1000)
        require(refillRateMillis in 10..500)
        scheduleRefilling()
    }

    private fun scheduleRefilling() {
        val addToBucket = timerTask {
            if (tokenBucket.get() < capacity) {
                tokenBucket.incrementAndGet()
            }
        }
        Timer("token bucket refiller").scheduleAtFixedRate(addToBucket, refillRateMillis, refillRateMillis)
    }

    fun limit(req: Request): Result {
        val tokens = tokenBucket.get()
        val result = when {
            tokens > 0 -> {
                tokenBucket.decrementAndGet()
                Forwarded(req)
            }

            else -> Dropped(req)
        }
        println("Remaining $tokens tokens; request ${result.javaClass.simpleName}")
        return result
    }
}