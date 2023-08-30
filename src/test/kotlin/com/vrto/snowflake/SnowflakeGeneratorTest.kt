package com.vrto.snowflake

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertFailsWith

class SnowflakeGeneratorTest {

    private val machineId = 123L
    private val snowflakeGenerator = SnowflakeGenerator(machineId)

    @Test
    fun `generated IDs are unique`() {
        val generatedIds = mutableSetOf<Long>()
        repeat(10000) {
            val snowflakeId = snowflakeGenerator.generateId()
            generatedIds.add(snowflakeId)
        }

        assertEquals(10000, generatedIds.size)
    }

    @Test
    fun `clock moves backwards exception`() {
        snowflakeGenerator.generateId()

        // Simulate clock moving backwards
        snowflakeGenerator.lastTimestamp += 1000

        val exception = assertFailsWith<RuntimeException> {
            snowflakeGenerator.generateId()
        }

        assertContains(exception.message!!, "Clock moved backwards. Refusing to generate ID")
    }

}