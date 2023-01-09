package com.vrto.ratelimiter

import com.vrto.ratelimiter.BucketState.*
import com.vrto.ratelimiter.Result.Dropped
import com.vrto.ratelimiter.Result.Forwarded
import org.amshove.kluent.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RateLimiterTest {

    @Nested
    @DisplayName("when creating rate limiter")
    inner class CreationPreconditions {

        @Test
        fun `capacity must be within bounds`() {
            invoking { RateLimiter(capacity = 0) } shouldThrow IllegalArgumentException::class
            invoking { RateLimiter(capacity = -1) } shouldThrow IllegalArgumentException::class
            invoking { RateLimiter(capacity = 1001) } shouldThrow IllegalArgumentException::class
            invoking { RateLimiter(/* reasonable defaults*/) } shouldNotThrow Exception::class
        }

        @Test
        fun `refill rate must be within bounds`() {
            invoking { RateLimiter(refillRateMillis = 9) } shouldThrow IllegalArgumentException::class
            invoking { RateLimiter(refillRateMillis = 501) } shouldThrow IllegalArgumentException::class
            invoking { RateLimiter(/* reasonable defaults*/) } shouldNotThrow Exception::class
        }
    }

    @Nested
    @DisplayName("when handling consistent request rate")
    inner class ConsistentRequestRate {

        @Test
        fun `refilling can keep up for a short period`() {
            val limiter = RateLimiter(capacity = 10, refillRateMillis = 25)
            val results = RequestGenerator(limiter).generate(count = 10, rateMillis = 25)

            results.shouldBeUnique()
            results `should match all with` {
                it is Forwarded
            }
            limiter shouldBeInState FULL
        }

        @Test
        fun `refilling can keep up for a long period`() {
            val limiter = RateLimiter(capacity = 100, refillRateMillis = 10)
            val results = RequestGenerator(limiter).generate(count = 500, rateMillis = 7)

            results.shouldBeUnique()
            results `should match all with` {
                it is Forwarded
            }
            limiter shouldBeInState PARTIALLY_FILLED
        }

        @Test
        fun `refilling can't keep up`() {
            val limiter = RateLimiter(capacity = 10, refillRateMillis = 20)
            val results = RequestGenerator(limiter).generate(count = 20, rateMillis = 5)

            results.shouldBeUnique()
            results `should match at least one of` {
                it is Dropped
            }
            limiter shouldBeInState STARVING
        }
    }

    @Nested
    @DisplayName("when handling request rates with bursts")
    inner class RequestRateWithBursts {

        @Test
        fun `refilling can keep up with a single burst`() {
            val limiter = RateLimiter(capacity = 10, refillRateMillis = 20)

            // X - request, space - sleep
            // X X X X X X X X X X XXXXX X X X X X X X X X X XXXXX
            val burst = Burst(len = 5, freq = 10)

            val results = RequestGenerator(limiter).generate(count = 20, rateMillis = 15, burst)
            results.shouldBeUnique()
            results `should match all with` {
                it is Forwarded
            }
            limiter shouldBeInState PARTIALLY_FILLED
        }

        @Test
        fun `refilling can keep up with multiple bursts`() {
            val limiter = RateLimiter(capacity = 100, refillRateMillis = 10)

            // burst of 5 requests beginning every 100th request
            val burst = Burst(len = 5, freq = 100)

            val results = RequestGenerator(limiter).generate(count = 500, rateMillis = 7, burst)
            results.shouldBeUnique()
            results `should match all with` {
                it is Forwarded
            }
            limiter shouldBeInState PARTIALLY_FILLED
        }

        @Test
        fun `refilling can't keep up with a single burst`() {
            val limiter = RateLimiter(capacity = 10, refillRateMillis = 20)

            // the burst immediately consumes the entire capacity
            val burst = Burst(len = 10, freq = 10)

            val results = RequestGenerator(limiter).generate(count = 20, rateMillis = 15, burst)
            results.shouldBeUnique()
            results `should match at least one of` {
                it is Dropped
            }
            limiter shouldBeInState STARVING
        }

        @Test
        fun `refilling can't keep up with multiple bursts`() {
            val limiter = RateLimiter(capacity = 100, refillRateMillis = 10)

            // the burst consume the remaining capacity too quickly
            val burst = Burst(len = 5, freq = 10)

            val results = RequestGenerator(limiter).generate(count = 500, rateMillis = 7, burst)
            results.shouldBeUnique()
            results `should match at least one of` {
                it is Dropped
            }
            limiter shouldBeInState STARVING
        }

    }
}

private fun List<Result>.shouldBeUnique() = this.map { it.req.id }.distinct().size == this.size

enum class BucketState {
    STARVING, FULL, PARTIALLY_FILLED
}

private infix fun RateLimiter.shouldBeInState(state: BucketState) = when(state) {
    STARVING -> this.remaining shouldBeInRange 0..1 // oscillating around 0
    FULL -> this.remaining shouldBeInRange this.capacity-1..this.capacity // oscillating around max
    PARTIALLY_FILLED -> this.remaining shouldBeInRange 1..this.capacity
}