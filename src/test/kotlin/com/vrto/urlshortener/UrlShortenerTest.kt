package com.vrto.urlshortener

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UrlShortenerTest {

    private val urlShortener = UrlShortener()

    @Test
    fun `should shorten url and expand it back`() {
        val shortUrl = urlShortener.shorten("http://www.google.com?query=123")
        assertEquals("17TeTb", shortUrl)
        assertIsBase62(shortUrl)

        val expanded = urlShortener.expand("17TeTb")
        assertEquals("http://www.google.com?query=123", expanded)
    }
}

fun assertIsBase62(input: String): Boolean {
    val regex = Regex("^[0-9A-Za-z]*\$")
    return regex.matches(input)
}