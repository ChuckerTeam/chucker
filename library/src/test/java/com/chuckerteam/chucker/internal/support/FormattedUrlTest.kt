package com.chuckerteam.chucker.internal.support

import com.google.common.truth.Truth.assertThat
import okhttp3.HttpUrl
import org.junit.Test

class FormattedUrlTest {
    @Test
    fun encodedUrl_withAllParams_isFormattedProperly() {
        val url = HttpUrl.get("https://www.example.com/path/to some/resource?q=\"Hello, world!\"")

        val formattedUrl = FormattedUrl.fromHttpUrl(url, encoded = true)

        assertThat(formattedUrl.scheme).isEqualTo("https")
        assertThat(formattedUrl.host).isEqualTo("www.example.com")
        assertThat(formattedUrl.path).isEqualTo("/path/to%20some/resource")
        assertThat(formattedUrl.query).isEqualTo("q=%22Hello,%20world!%22")
        assertThat(formattedUrl.pathWithQuery).isEqualTo("/path/to%20some/resource?q=%22Hello,%20world!%22")
        assertThat(formattedUrl.url).isEqualTo("https://www.example.com/path/to%20some/resource?q=%22Hello,%20world!%22")
    }

    @Test
    fun encodedUrl_withoutPath_isFormattedProperly() {
        val url = HttpUrl.get("https://www.example.com?q=\"Hello, world!\"")

        val formattedUrl = FormattedUrl.fromHttpUrl(url, encoded = true)

        assertThat(formattedUrl.scheme).isEqualTo("https")
        assertThat(formattedUrl.host).isEqualTo("www.example.com")
        assertThat(formattedUrl.path).isEmpty()
        assertThat(formattedUrl.query).isEqualTo("q=%22Hello,%20world!%22")
        assertThat(formattedUrl.pathWithQuery).isEqualTo("?q=%22Hello,%20world!%22")
        assertThat(formattedUrl.url).isEqualTo("https://www.example.com?q=%22Hello,%20world!%22")
    }

    @Test
    fun encodedUrl_withoutQuery_isFormattedProperly() {
        val url = HttpUrl.get("https://www.example.com/path/to some/resource")

        val formattedUrl = FormattedUrl.fromHttpUrl(url, encoded = true)

        assertThat(formattedUrl.scheme).isEqualTo("https")
        assertThat(formattedUrl.host).isEqualTo("www.example.com")
        assertThat(formattedUrl.path).isEqualTo("/path/to%20some/resource")
        assertThat(formattedUrl.query).isEmpty()
        assertThat(formattedUrl.pathWithQuery).isEqualTo("/path/to%20some/resource")
        assertThat(formattedUrl.url).isEqualTo("https://www.example.com/path/to%20some/resource")
    }

    @Test
    fun decodedUrl_withAllParams_isFormattedProperly() {
        val url = HttpUrl.get("https://www.example.com/path/to some/resource?q=\"Hello, world!\"")

        val formattedUrl = FormattedUrl.fromHttpUrl(url, encoded = false)

        assertThat(formattedUrl.scheme).isEqualTo("https")
        assertThat(formattedUrl.host).isEqualTo("www.example.com")
        assertThat(formattedUrl.path).isEqualTo("/path/to some/resource")
        assertThat(formattedUrl.query).isEqualTo("q=\"Hello, world!\"")
        assertThat(formattedUrl.pathWithQuery).isEqualTo("/path/to some/resource?q=\"Hello, world!\"")
        assertThat(formattedUrl.url).isEqualTo("https://www.example.com/path/to some/resource?q=\"Hello, world!\"")
    }

    @Test
    fun decodedUrl_withoutPath_isFormattedProperly() {
        val url = HttpUrl.get("https://www.example.com?q=\"Hello, world!\"")

        val formattedUrl = FormattedUrl.fromHttpUrl(url, encoded = false)

        assertThat(formattedUrl.scheme).isEqualTo("https")
        assertThat(formattedUrl.host).isEqualTo("www.example.com")
        assertThat(formattedUrl.path).isEmpty()
        assertThat(formattedUrl.query).isEqualTo("q=\"Hello, world!\"")
        assertThat(formattedUrl.pathWithQuery).isEqualTo("?q=\"Hello, world!\"")
        assertThat(formattedUrl.url).isEqualTo("https://www.example.com?q=\"Hello, world!\"")
    }

    @Test
    fun decodedUrl_withoutQuery_isFormattedProperly() {
        val url = HttpUrl.get("https://www.example.com/path/to some/resource")

        val formattedUrl = FormattedUrl.fromHttpUrl(url, encoded = false)

        assertThat(formattedUrl.scheme).isEqualTo("https")
        assertThat(formattedUrl.host).isEqualTo("www.example.com")
        assertThat(formattedUrl.path).isEqualTo("/path/to some/resource")
        assertThat(formattedUrl.query).isEmpty()
        assertThat(formattedUrl.pathWithQuery).isEqualTo("/path/to some/resource")
        assertThat(formattedUrl.url).isEqualTo("https://www.example.com/path/to some/resource")
    }
}
