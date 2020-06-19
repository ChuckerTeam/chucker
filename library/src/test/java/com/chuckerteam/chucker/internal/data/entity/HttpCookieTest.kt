package com.chuckerteam.chucker.internal.data.entity

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class HttpCookieTest {
    @Test
    fun requestCookiesHandleEmpty() {
        assertThat(HttpCookie.fromRequestHeader("")).isEmpty()
    }

    @Test
    fun requestCookiesHandleNull() {
        assertThat(HttpCookie.fromRequestHeader(null)).isEmpty()
    }

    @Test
    fun responseCookiesHandleEmpty() {
        assertThat(HttpCookie.fromResponseHeaders(emptyList())).isEmpty()
    }

    @Test
    fun responseCookiesHandleNull() {
        assertThat(HttpCookie.fromResponseHeaders(null)).isEmpty()
    }

    @Test
    fun requestCookies() {
        val cookies = HttpCookie.fromRequestHeader("X=Y; A=B")

        assertThat(cookies.size).isEqualTo(2)
        assertThat(cookies[0].name).isEqualTo("X")
        assertThat(cookies[0].value).isEqualTo("Y")
        assertThat(cookies[1].name).isEqualTo("A")
        assertThat(cookies[1].value).isEqualTo("B")
    }

    @Test
    fun singleResponseCookieHeaderWithSingleValue() {
        val cookies = HttpCookie.fromResponseHeaders(
            listOf(HttpHeader("set-cookie", "X=Y; Path=/"))
        )

        assertThat(cookies.size).isEqualTo(1)
        assertThat(cookies[0].name).isEqualTo("X")
        assertThat(cookies[0].value).isEqualTo("Y")
    }

    @Test
    fun multipleResponseCookieHeaderWithSingleValues() {
        val cookies = HttpCookie.fromResponseHeaders(
            listOf(
                HttpHeader("set-cookie", "X=Y; Path=/"),
                HttpHeader("set-cookie", "A=B; Path=/")
            )
        )

        assertThat(cookies.size).isEqualTo(2)
        assertThat(cookies[0].name).isEqualTo("X")
        assertThat(cookies[0].value).isEqualTo("Y")
        assertThat(cookies[1].name).isEqualTo("A")
        assertThat(cookies[1].value).isEqualTo("B")
    }

    @Test
    fun singleResponseCookieHeaderWithMultipleValues() {
        val cookies = HttpCookie.fromResponseHeaders(
            listOf(
                HttpHeader(
                    "set-cookie",
                    "X=Y; Path=/, A=B; Path=/"
                )
            )
        )

        assertThat(cookies.size).isEqualTo(2)
        assertThat(cookies[0].name).isEqualTo("X")
        assertThat(cookies[0].value).isEqualTo("Y")
        assertThat(cookies[1].name).isEqualTo("A")
        assertThat(cookies[1].value).isEqualTo("B")
    }

    @Test
    fun multipleResponseCookieHeaderWithMultipleValues() {
        val cookies = HttpCookie.fromResponseHeaders(
            listOf(
                HttpHeader("set-cookie", "One=fish; Path=/, two=fish; Path=/"),
                HttpHeader("set-cookie", "Red=fish; Path=/"),
                HttpHeader("set-cookie", "blue=fish; Path=/")
            )
        )

        assertThat(cookies.size).isEqualTo(4)
        assertThat(cookies[0].name).isEqualTo("One")
        assertThat(cookies[0].value).isEqualTo("fish")
        assertThat(cookies[1].name).isEqualTo("two")
        assertThat(cookies[1].value).isEqualTo("fish")
        assertThat(cookies[2].name).isEqualTo("Red")
        assertThat(cookies[2].value).isEqualTo("fish")
        assertThat(cookies[3].name).isEqualTo("blue")
        assertThat(cookies[3].value).isEqualTo("fish")
    }
}
