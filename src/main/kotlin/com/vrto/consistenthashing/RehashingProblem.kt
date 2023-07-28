package com.vrto.consistenthashing

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.UUID

fun main() {
    // 4 servers, each will get some keys
    val serverMap = createServerMap(4)

    // assign some keys to server
    val numKeys = 10
    for (i in 1..numKeys) {
        val key = UUID.randomUUID().toString().take(6) // just to make them easier to read
        val serverIndex = hash(key) % serverMap.size
        serverMap[serverIndex.toInt()]!!.add(key)
    }

    // print status
    println("Generated $numKeys keys, they've been assigned to 4 servers:")
    println(serverMap)

    // server 1 goes offline, so we need to reassign its keys to other servers
    val keys = serverMap.values.flatten()
    val newServerMap = createServerMap(3)
    for (key in keys) {
        val serverIndex = hash(key) % newServerMap.size
        newServerMap[serverIndex.toInt()]!!.add(key)
    }

    // print status
    println("Same $numKeys keys, they've been assigned to 3 servers:")
    println(newServerMap)

    var reassigned = 0
    for (key in keys) {
        val originalServer = serverMap.findServerByKey(key)
        val rehashedServer = newServerMap.findServerByKey(key)
        if (originalServer != rehashedServer) {
            println("$key was reassigned after server 1 went down")
            reassigned++
        }
    }
    println("\n${10*reassigned}% of keys needed to be reassigned")
}


private fun createServerMap(size: Int) = mutableMapOf<Int, MutableList<String>>()
    .apply {
        for (i in 0 until size) {
            put(i, mutableListOf())
        }
    }

private fun MutableMap<Int, MutableList<String>>.findServerByKey(key: String) =
    entries.find { it.value.contains(key) }!!.key

// any hash func will do, this is just an example
private fun hash(key: String): Long {
    val sha1 = MessageDigest.getInstance("SHA-1")
    val hashBytes = sha1.digest(key.toByteArray(StandardCharsets.UTF_8))
    return (hashBytes[0].toLong() and 0xFF) or
            ((hashBytes[1].toLong() and 0xFF) shl 8) or
            ((hashBytes[2].toLong() and 0xFF) shl 16) or
            ((hashBytes[3].toLong() and 0xFF) shl 24)
}