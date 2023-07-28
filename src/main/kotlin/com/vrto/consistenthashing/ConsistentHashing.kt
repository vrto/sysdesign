package com.vrto.consistenthashing

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*

class ConsistentHashing(initialCount: Int) {

    private var serverCount = 0
    private var hashRing = TreeMap<Long, String>()

    init {
        for (i in 0 until initialCount) {
            addServer()
        }
    }

    fun serverLookup(key: String): Int {
        val hash = hash(key)
        // move clockwise
        val tailMap = hashRing.tailMap(hash)
        val serverHash = if (tailMap.isEmpty()) hashRing.firstKey() else tailMap.firstKey()
        return hashRing[serverHash]!!.toInt()
    }

    fun addServer() {
        val hash = hash("server-$serverCount")
        hashRing[hash] = "$serverCount"
        serverCount++
    }

    fun removeServer(serverIndex: Int) {
        serverCount--
        val hash = hash("server-$serverIndex")
        hashRing.remove(hash)
    }

    private fun hash(key: String): Long { // SHA-1 chosen randomly :)
        val sha1 = MessageDigest.getInstance("SHA-1")
        val hashBytes = sha1.digest(key.toByteArray(StandardCharsets.UTF_8))
        return (hashBytes[0].toLong() and 0xFF) or
                ((hashBytes[1].toLong() and 0xFF) shl 8) or
                ((hashBytes[2].toLong() and 0xFF) shl 16) or
                ((hashBytes[3].toLong() and 0xFF) shl 24)
    }
}