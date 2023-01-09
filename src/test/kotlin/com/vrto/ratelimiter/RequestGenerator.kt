package com.vrto.ratelimiter

import java.lang.Thread.sleep
import java.util.*

class RequestGenerator(private val rateLimiter: RateLimiter) {

    fun generate(count: Int, rateMillis: Long, burst: Burst? = null): List<Result> {
        // when no burst setting is specified, then every iteration sleeps the same amount of time (consistent rate)
        // otherwise burst setting causes some iterations not to sleep at all (hence the 'burst' request accumulation)
        val sleepTimes = (1..count).map { sleepTime(it, rateMillis, burst) }
        return (1..count).map {
            sleep(sleepTimes[it-1])
            rateLimiter.limit(MockRequest())
        }
    }

    private fun sleepTime(iterNum: Int, rateMillis: Long, burst: Burst?): Long = when {
        // no burst setting -> consistent sleep rate
        burst == null -> rateMillis
        // iterations before the first burst appearance
        iterNum < burst.freq -> rateMillis
        // burst duration (first X requests of the burst) -> no sleep
        iterNum % burst.freq < burst.len -> 0
        // outside of burst -> normal sleep
        else -> rateMillis
    }
}

// Burst appears every freq occurrences and lasts for len size (no sleeps are applied during the burst period)
data class Burst(val len: Int, val freq: Int)

class MockRequest : Request {
    override val id: String
        get() = UUID.randomUUID().toString()
}