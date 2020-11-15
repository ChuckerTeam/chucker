package com.chuckerteam.chucker.internal.support

import com.google.common.truth.Truth.assertThat
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Test

internal class FormattedUrlTest {
    @Test
    fun encodedUrl_withAllParams_isFormattedProperly() {
        val url = "https://www.example.com/path/to some/resource?q=\"Hello, world!\"".toHttpUrl()

        val formattedUrl = FormattedUrl.fromHttpUrl(url, encoded = true)

        assertThat(formattedUrl.scheme).isEqualTo("https")
        assertThat(formattedUrl.host).isEqualTo("www.example.com")
        assertThat(formattedUrl.port).isEqualTo(443)
        assertThat(formattedUrl.path).isEqualTo("/path/to%20some/resource")
        assertThat(formattedUrl.query).isEqualTo("q=%22Hello,%20world!%22")
        assertThat(formattedUrl.pathWithQuery).isEqualTo("/path/to%20some/resource?q=%22Hello,%20world!%22")
        assertThat(formattedUrl.url).isEqualTo("https://www.example.com/path/to%20some/resource?q=%22Hello,%20world!%22")
    }

    @Test
    fun encodedUrl_withoutPath_isFormattedProperly() {
        val url = "https://www.example.com?q=\"Hello, world!\"".toHttpUrl()

        val formattedUrl = FormattedUrl.fromHttpUrl(url, encoded = true)

        assertThat(formattedUrl.scheme).isEqualTo("https")
        assertThat(formattedUrl.host).isEqualTo("www.example.com")
        assertThat(formattedUrl.port).isEqualTo(443)
        assertThat(formattedUrl.path).isEmpty()
        assertThat(formattedUrl.query).isEqualTo("q=%22Hello,%20world!%22")
        assertThat(formattedUrl.pathWithQuery).isEqualTo("?q=%22Hello,%20world!%22")
        assertThat(formattedUrl.url).isEqualTo("https://www.example.com?q=%22Hello,%20world!%22")
    }

    @Test
    fun encodedUrl_withoutQuery_isFormattedProperly() {
        val url = "https://www.example.com/path/to some/resource".toHttpUrl()

        val formattedUrl = FormattedUrl.fromHttpUrl(url, encoded = true)

        assertThat(formattedUrl.scheme).isEqualTo("https")
        assertThat(formattedUrl.host).isEqualTo("www.example.com")
        assertThat(formattedUrl.port).isEqualTo(443)
        assertThat(formattedUrl.path).isEqualTo("/path/to%20some/resource")
        assertThat(formattedUrl.query).isEmpty()
        assertThat(formattedUrl.pathWithQuery).isEqualTo("/path/to%20some/resource")
        assertThat(formattedUrl.url).isEqualTo("https://www.example.com/path/to%20some/resource")
    }

    @Test
    fun decodedUrl_withAllParams_isFormattedProperly() {
        val url = "https://www.example.com/path/to some/resource?q=\"Hello, world!\"".toHttpUrl()

        val formattedUrl = FormattedUrl.fromHttpUrl(url, encoded = false)

        assertThat(formattedUrl.scheme).isEqualTo("https")
        assertThat(formattedUrl.host).isEqualTo("www.example.com")
        assertThat(formattedUrl.port).isEqualTo(443)
        assertThat(formattedUrl.path).isEqualTo("/path/to some/resource")
        assertThat(formattedUrl.query).isEqualTo("q=\"Hello, world!\"")
        assertThat(formattedUrl.pathWithQuery).isEqualTo("/path/to some/resource?q=\"Hello, world!\"")
        assertThat(formattedUrl.url).isEqualTo("https://www.example.com/path/to some/resource?q=\"Hello, world!\"")
    }

    @Test
    fun decodedUrl_withoutPath_isFormattedProperly() {
        val url = "https://www.example.com?q=\"Hello, world!\"".toHttpUrl()

        val formattedUrl = FormattedUrl.fromHttpUrl(url, encoded = false)

        assertThat(formattedUrl.scheme).isEqualTo("https")
        assertThat(formattedUrl.host).isEqualTo("www.example.com")
        assertThat(formattedUrl.port).isEqualTo(443)
        assertThat(formattedUrl.path).isEmpty()
        assertThat(formattedUrl.query).isEqualTo("q=\"Hello, world!\"")
        assertThat(formattedUrl.pathWithQuery).isEqualTo("?q=\"Hello, world!\"")
        assertThat(formattedUrl.url).isEqualTo("https://www.example.com?q=\"Hello, world!\"")
    }

    @Test
    fun decodedUrl_withoutQuery_isFormattedProperly() {
        val url = "https://www.example.com/path/to some/resource".toHttpUrl()

        val formattedUrl = FormattedUrl.fromHttpUrl(url, encoded = false)

        assertThat(formattedUrl.scheme).isEqualTo("https")
        assertThat(formattedUrl.host).isEqualTo("www.example.com")
        assertThat(formattedUrl.port).isEqualTo(443)
        assertThat(formattedUrl.path).isEqualTo("/path/to some/resource")
        assertThat(formattedUrl.query).isEmpty()
        assertThat(formattedUrl.pathWithQuery).isEqualTo("/path/to some/resource")
        assertThat(formattedUrl.url).isEqualTo("https://www.example.com/path/to some/resource")
    }

    @Test
    fun decodedUrl_withNonStandardHttpsPort_isFormattedProperly() {
        val url = "https://www.example.com:8443/path/to some/resource".toHttpUrl()

        val formattedUrl = FormattedUrl.fromHttpUrl(url, encoded = false)

        assertThat(formattedUrl.scheme).isEqualTo("https")
        assertThat(formattedUrl.host).isEqualTo("www.example.com")
        assertThat(formattedUrl.port).isEqualTo(8443)
        assertThat(formattedUrl.path).isEqualTo("/path/to some/resource")
        assertThat(formattedUrl.query).isEmpty()
        assertThat(formattedUrl.pathWithQuery).isEqualTo("/path/to some/resource")
        assertThat(formattedUrl.url).isEqualTo("https://www.example.com:8443/path/to some/resource")
    }

    @Test
    fun decodedUrl_withNonStandardHttpPort_isFormattedProperly() {
        val url = "https://www.example.com:8080/path/to some/resource".toHttpUrl()

        val formattedUrl = FormattedUrl.fromHttpUrl(url, encoded = false)

        assertThat(formattedUrl.scheme).isEqualTo("https")
        assertThat(formattedUrl.host).isEqualTo("www.example.com")
        assertThat(formattedUrl.port).isEqualTo(8080)
        assertThat(formattedUrl.path).isEqualTo("/path/to some/resource")
        assertThat(formattedUrl.query).isEmpty()
        assertThat(formattedUrl.pathWithQuery).isEqualTo("/path/to some/resource")
        assertThat(formattedUrl.url).isEqualTo("https://www.example.com:8080/path/to some/resource")
    }

    @Test
    fun decodedUrl_withStandardHttpPort_isFormattedProperly() {
        val url = "http://www.example.com/path/to some/resource".toHttpUrl()

        val formattedUrl = FormattedUrl.fromHttpUrl(url, encoded = false)

        assertThat(formattedUrl.scheme).isEqualTo("http")
        assertThat(formattedUrl.host).isEqualTo("www.example.com")
        assertThat(formattedUrl.port).isEqualTo(80)
        assertThat(formattedUrl.path).isEqualTo("/path/to some/resource")
        assertThat(formattedUrl.query).isEmpty()
        assertThat(formattedUrl.pathWithQuery).isEqualTo("/path/to some/resource")
        assertThat(formattedUrl.url).isEqualTo("http://www.example.com/path/to some/resource")
    }
}
