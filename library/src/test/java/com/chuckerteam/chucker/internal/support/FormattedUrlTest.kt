package com.chuckerteam.chucker.internal.support

import junit.framework.TestCase.assertEquals
import okhttp3.HttpUrl
import org.junit.Test

class FormattedUrlTest {
    @Test
    fun encodedUrl_withAllParams_isFormattedProperly() {
        val url = HttpUrl.get("https://www.example.com/path/to some/resource?q=\"Hello, world!\"")

        val formattedUrl = FormattedUrl.fromHttpUrl(url, encoded = true)

        assertEquals("https", formattedUrl.scheme)
        assertEquals("www.example.com", formattedUrl.host)
        assertEquals("/path/to%20some/resource", formattedUrl.path)
        assertEquals("q=%22Hello,%20world!%22", formattedUrl.query)
        assertEquals("/path/to%20some/resource?q=%22Hello,%20world!%22", formattedUrl.pathWithQuery)
        assertEquals(
            "https://www.example.com/path/to%20some/resource?q=%22Hello,%20world!%22",
            formattedUrl.url
        )
    }

    @Test
    fun encodedUrl_withoutPath_isFormattedProperly() {
        val url = HttpUrl.get("https://www.example.com?q=\"Hello, world!\"")

        val formattedUrl = FormattedUrl.fromHttpUrl(url, encoded = true)

        assertEquals("https", formattedUrl.scheme)
        assertEquals("www.example.com", formattedUrl.host)
        assertEquals("", formattedUrl.path)
        assertEquals("q=%22Hello,%20world!%22", formattedUrl.query)
        assertEquals("?q=%22Hello,%20world!%22", formattedUrl.pathWithQuery)
        assertEquals("https://www.example.com?q=%22Hello,%20world!%22", formattedUrl.url)
    }

    @Test
    fun encodedUrl_withoutQuery_isFormattedProperly() {
        val url = HttpUrl.get("https://www.example.com/path/to some/resource")

        val formattedUrl = FormattedUrl.fromHttpUrl(url, encoded = true)

        assertEquals("https", formattedUrl.scheme)
        assertEquals("www.example.com", formattedUrl.host)
        assertEquals("/path/to%20some/resource", formattedUrl.path)
        assertEquals("", formattedUrl.query)
        assertEquals("/path/to%20some/resource", formattedUrl.pathWithQuery)
        assertEquals("https://www.example.com/path/to%20some/resource", formattedUrl.url)
    }

    @Test
    fun decodedUrl_withAllParams_isFormattedProperly() {
        val url = HttpUrl.get("https://www.example.com/path/to some/resource?q=\"Hello, world!\"")

        val formattedUrl = FormattedUrl.fromHttpUrl(url, encoded = false)

        assertEquals("https", formattedUrl.scheme)
        assertEquals("www.example.com", formattedUrl.host)
        assertEquals("/path/to some/resource", formattedUrl.path)
        assertEquals("q=\"Hello, world!\"", formattedUrl.query)
        assertEquals("/path/to some/resource?q=\"Hello, world!\"", formattedUrl.pathWithQuery)
        assertEquals(
            "https://www.example.com/path/to some/resource?q=\"Hello, world!\"",
            formattedUrl.url
        )
    }

    @Test
    fun decodedUrl_withoutPath_isFormattedProperly() {
        val url = HttpUrl.get("https://www.example.com?q=\"Hello, world!\"")

        val formattedUrl = FormattedUrl.fromHttpUrl(url, encoded = false)

        assertEquals("https", formattedUrl.scheme)
        assertEquals("www.example.com", formattedUrl.host)
        assertEquals("", formattedUrl.path)
        assertEquals("q=\"Hello, world!\"", formattedUrl.query)
        assertEquals("?q=\"Hello, world!\"", formattedUrl.pathWithQuery)
        assertEquals("https://www.example.com?q=\"Hello, world!\"", formattedUrl.url)
    }

    @Test
    fun decodedUrl_withoutQuery_isFormattedProperly() {
        val url = HttpUrl.get("https://www.example.com/path/to some/resource")

        val formattedUrl = FormattedUrl.fromHttpUrl(url, encoded = false)

        assertEquals("https", formattedUrl.scheme)
        assertEquals("www.example.com", formattedUrl.host)
        assertEquals("/path/to some/resource", formattedUrl.path)
        assertEquals("", formattedUrl.query)
        assertEquals("/path/to some/resource", formattedUrl.pathWithQuery)
        assertEquals("https://www.example.com/path/to some/resource", formattedUrl.url)
    }
}
