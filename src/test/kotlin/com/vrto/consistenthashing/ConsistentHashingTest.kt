package com.vrto.consistenthashing

import org.amshove.kluent.`should be`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

class ConsistentHashingTest {

    private val consistentHashing = ConsistentHashing(4)

    @ParameterizedTest
    @MethodSource("keysOnHashRing")
    fun `server lookup maps keys to the hash ring`(key: String, ringPosition: Int) {
        val server = consistentHashing.serverLookup(key)
        server `should be` ringPosition
    }

    @ParameterizedTest
    @MethodSource("postAddingServer")
    fun `adding a server only moves fraction of keys`(key: String, ringPosition: Int) {
        // new server's hash places it right between the old server 3 and server 0
        consistentHashing.addServer()

        val server = consistentHashing.serverLookup(key)
        server `should be` ringPosition
    }

    @ParameterizedTest
    @MethodSource("postRemovingServer")
    fun `removing a server only moves fraction of keys`(key: String, ringPosition: Int) {
        // all keys from server 1 will be remapped, others remain unaffected
        consistentHashing.removeServer(1)

        val server = consistentHashing.serverLookup(key)
        server `should be` ringPosition
    }

    companion object {

        @JvmStatic
        fun keysOnHashRing() = listOf(
            arguments("Bv?!111", 0),
            arguments("key3", 0),
            arguments("key211", 1),
            arguments("key212", 1),
            arguments("key1", 2),
            arguments("key7", 2),
            arguments("key2", 3),
            arguments("key4", 3),
        )


        @JvmStatic
        fun postAddingServer() = listOf(
            arguments("Bv?!111", 0),
            arguments("key3", 0),
            arguments("key211", 1),
            arguments("key212", 1),
            arguments("key1", 2),
            arguments("key7", 2),
            arguments("key2", 3),
            arguments("key4", 4),
        )

        @JvmStatic
        fun postRemovingServer() = listOf(
            arguments("Bv?!111", 0),
            arguments("key3", 0),
            arguments("key211", 2),
            arguments("key212", 2),
            arguments("key1", 2),
            arguments("key7", 2),
            arguments("key2", 3),
            arguments("key4", 3),
        )
    }
}