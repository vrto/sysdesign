package com.vrto.urlshortener

class UrlShortener {

    private val db = hashMapOf<String, String>()

    fun shorten(longUrl: String): String {
        val shortUrl = longUrl.toBase62()
        db[shortUrl] = longUrl
        return shortUrl
    }

    fun expand(shortUrl: String): String = db[shortUrl] ?: throw IllegalArgumentException("Unknown url: $shortUrl")
}

fun String.toBase62(): String {
    val charset = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    val base = charset.length

    // Convert the input string to a byte array
    val bytes = toByteArray()

    // Convert bytes to a single integer
    var num = 0
    for (byte in bytes) {
        num = (num shl 8) or (byte.toInt() and 0xFF)
    }

    // Build the base62 representation
    val base62Chars = mutableListOf<Char>()
    while (num > 0) {
        val remainder = num % base
        base62Chars.add(charset[remainder])
        num /= base
    }

    // Reverse the characters and convert them to a string
    return base62Chars.reversed().joinToString("")
}