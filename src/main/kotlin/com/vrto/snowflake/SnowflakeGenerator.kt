package com.vrto.snowflake

class SnowflakeGenerator(private val machineId: Long) {

    private val customEpoch = 1577836800000L // January 1, 2020 in milliseconds
    private var sequence = 0L
    internal var lastTimestamp = -1L

    init {
        if (machineId < 0 || machineId >= (1L shl 10)) {
            throw IllegalArgumentException("Machine ID must be between 0 and ${1L shl 10 - 1}")
        }
    }

    @Synchronized
    fun generateId(): Long {
        var currentTimestamp = System.currentTimeMillis()

        if (currentTimestamp < lastTimestamp) {
            throw RuntimeException("Clock moved backwards. Refusing to generate ID for ${lastTimestamp - currentTimestamp} milliseconds.")
        }

        // up to 4096 unique sequences per the same timestamp
        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) and 0xFFF
            if (sequence == 0L) {
                currentTimestamp = waitNextMillis(currentTimestamp)
            }
        } else {
            sequence = 0L
        }

        lastTimestamp = currentTimestamp

        return ((currentTimestamp - customEpoch) shl 22) or (machineId shl 12) or sequence
    }

    private fun waitNextMillis(currentTimestamp: Long): Long {
        var newTimestamp = System.currentTimeMillis()
        while (newTimestamp <= currentTimestamp) {
            newTimestamp = System.currentTimeMillis()
        }
        return newTimestamp
    }
}
