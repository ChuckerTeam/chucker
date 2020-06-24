package com.chuckerteam.chucker.internal.data.entity

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class HttpTransactionTest {
    @Test
    fun requestCookiesHandleMissingHeader() {
        val testObject = HttpTransaction()
        assertThat(testObject.requestCookies).isEmpty()
        assertThat(testObject.cookiesPresent).isFalse()
    }

    @Test
    fun responseCookiesHandleMissingHeader() {
        val testObject = HttpTransaction()
        assertThat(testObject.responseCookies).isEmpty()
        assertThat(testObject.cookiesPresent).isFalse()
    }

    @Test
    fun requestCookies() {
        val testObject = HttpTransaction().apply {
            setRequestHeaders(listOf(HttpHeader("Cookie", "X=Y; A=B")))
        }
        assertThat(testObject.cookiesPresent).isTrue()
        assertThat(testObject.responseCookies).isEmpty()

        val cookies = testObject.requestCookies
        assertThat(cookies.size).isEqualTo(2)
        assertThat(cookies[0].name).isEqualTo("X")
        assertThat(cookies[0].value).isEqualTo("Y")
        assertThat(cookies[1].name).isEqualTo("A")
        assertThat(cookies[1].value).isEqualTo("B")
    }

    @Test
    fun formatRequestCookies() {
        val testObject = HttpTransaction().apply {
            setRequestHeaders(listOf(HttpHeader("Cookie", "X=Y; A=B")))
        }
        assertThat(testObject.getRequestCookiesString()).isEqualTo(
            "<b>X: </b>Y<br /><b>A: </b>B<br />"
        )
    }

    @Test
    fun singleResponseCookieHeaderWithSingleValue() {
        val testObject = HttpTransaction().apply {
            setResponseHeaders(listOf(HttpHeader("set-cookie", "X=Y; Path=/")))
        }
        assertThat(testObject.cookiesPresent).isTrue()
        assertThat(testObject.requestCookies).isEmpty()

        val cookies = testObject.responseCookies
        assertThat(cookies.size).isEqualTo(1)
        assertThat(cookies[0].name).isEqualTo("X")
        assertThat(cookies[0].value).isEqualTo("Y")
    }

    @Test
    fun formatSingleResponseCookieHeaderWithSingleValue() {
        val testObject = HttpTransaction().apply {
            setResponseHeaders(listOf(HttpHeader("set-cookie", "X=Y; Path=/")))
        }
        assertThat(testObject.getResponseCookiesString()).isEqualTo(
            "<b>X: </b>Y<br />"
        )
    }

    @Test
    fun multipleResponseCookieHeaderWithSingleValues() {
        val testObject = HttpTransaction().apply {
            setResponseHeaders(
                listOf(
                    HttpHeader("set-cookie", "X=Y; Path=/"),
                    HttpHeader("set-cookie", "A=B; Path=/")
                )
            )
        }
        assertThat(testObject.cookiesPresent).isTrue()
        assertThat(testObject.requestCookies).isEmpty()

        val cookies = testObject.responseCookies
        assertThat(cookies.size).isEqualTo(2)
        assertThat(cookies[0].name).isEqualTo("X")
        assertThat(cookies[0].value).isEqualTo("Y")
        assertThat(cookies[1].name).isEqualTo("A")
        assertThat(cookies[1].value).isEqualTo("B")
    }

    @Test
    fun formatMultipleResponseCookieHeaderWithSingleValues() {
        val testObject = HttpTransaction().apply {
            setResponseHeaders(
                listOf(
                    HttpHeader("set-cookie", "X=Y; Path=/"),
                    HttpHeader("set-cookie", "A=B; Path=/")
                )
            )
        }
        assertThat(testObject.getResponseCookiesString()).isEqualTo(
            "<b>X: </b>Y<br /><b>A: </b>B<br />"
        )
    }

    @Test
    fun singleResponseCookieHeaderWithMultipleValues() {
        val testObject = HttpTransaction().apply {
            setResponseHeaders(listOf(HttpHeader("set-cookie", "X=Y; Path=/, A=B; Path=/")))
        }
        assertThat(testObject.cookiesPresent).isTrue()
        assertThat(testObject.requestCookies).isEmpty()

        val cookies = testObject.responseCookies
        assertThat(cookies.size).isEqualTo(2)
        assertThat(cookies[0].name).isEqualTo("X")
        assertThat(cookies[0].value).isEqualTo("Y")
        assertThat(cookies[1].name).isEqualTo("A")
        assertThat(cookies[1].value).isEqualTo("B")
    }

    @Test
    fun formatSingleResponseCookieHeaderWithMultipleValues() {
        val testObject = HttpTransaction().apply {
            setResponseHeaders(listOf(HttpHeader("set-cookie", "X=Y; Path=/, A=B; Path=/")))
        }
        assertThat(testObject.getResponseCookiesString()).isEqualTo(
            "<b>X: </b>Y<br /><b>A: </b>B<br />"
        )
    }

    @Test
    fun multipleResponseCookieHeaderWithMultipleValues() {
        val testObject = HttpTransaction().apply {
            setResponseHeaders(
                listOf(
                    HttpHeader("set-cookie", "One=fish; Path=/, two=fish; Path=/"),
                    HttpHeader("set-cookie", "Red=fish; Path=/"),
                    HttpHeader("set-cookie", "blue=fish; Path=/")
                )
            )
        }
        assertThat(testObject.cookiesPresent).isTrue()
        assertThat(testObject.requestCookies).isEmpty()

        val cookies = testObject.responseCookies
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

    @Test
    fun formatMultipleResponseCookieHeaderWithMultipleValues() {
        val testObject = HttpTransaction().apply {
            setResponseHeaders(
                listOf(
                    HttpHeader("set-cookie", "One=fish; Path=/, two=fish; Path=/"),
                    HttpHeader("set-cookie", "Red=fish; Path=/"),
                    HttpHeader("set-cookie", "blue=fish; Path=/")
                )
            )
        }
        assertThat(testObject.getResponseCookiesString()).isEqualTo(
            "<b>One: </b>fish<br /><b>two: </b>fish<br /><b>Red: </b>fish<br /><b>blue: </b>fish<br />"
        )
    }
}
