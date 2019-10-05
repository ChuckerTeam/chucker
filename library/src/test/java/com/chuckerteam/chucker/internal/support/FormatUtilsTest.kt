package com.chuckerteam.chucker.internal.support

import android.content.Context
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.HttpHeader
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FormatUtilsTest {

    @Nested
    inner class CurlCommand {
        @Test
        fun emptyTransaction() {
            assertEquals(
                "curl -X  ",
                getShareCurlCommand(HttpTransaction())
            )
        }

        @Test
        fun realTransaction() {
            val httpTransaction = HttpTransaction(
                id = 1,
                requestDate = 2,
                responseDate = 3,
                tookMs = 4,
                protocol = "https",
                method = "GET",
                url = "https://example.com/",
                host = "example.com",
                path = "/",
                scheme = "https",
                requestContentLength = 5,
                requestContentType = "text/plain",
                requestHeaders = "[{\"name\": \"a\", \"value\": \"b\"}]",
                requestBody = "Request Body",
                isRequestBodyPlainText = true,
                responseCode = 418,
                responseMessage = "d",
                error = "e",
                responseContentLength = 6,
                responseContentType = "text/plain",
                responseHeaders = "[{\"name\": \"c\", \"value\": \"d\"}]",
                responseBody = "Response Body",
                isResponseBodyPlainText = true,
                responseImageData = null
            )
            assertEquals(
                "curl -X GET -H \"a: b\" --data \$'Request Body' https://example.com/",
                getShareCurlCommand(httpTransaction)
            )
        }
    }

    @Nested
    inner class ShareText {
        private val context = mockk<Context>()

        @BeforeEach
        fun setUp() {
            every { context.getString(R.string.chucker_url) } returns "c-url"
            every { context.getString(R.string.chucker_method) } returns "c-method"
            every { context.getString(R.string.chucker_protocol) } returns "c-prot"
            every { context.getString(R.string.chucker_status) } returns "c-stat"
            every { context.getString(R.string.chucker_response) } returns "c-res"
            every { context.getString(R.string.chucker_ssl) } returns "c-ssl"
            every { context.getString(R.string.chucker_request_time) } returns "c-req-time"
            every { context.getString(R.string.chucker_response_time) } returns "c-res-time"
            every { context.getString(R.string.chucker_duration) } returns "c-dur"
            every { context.getString(R.string.chucker_request_size) } returns "c-req-size"
            every { context.getString(R.string.chucker_response_size) } returns "c-res-size"
            every { context.getString(R.string.chucker_total_size) } returns "c-size"
            every { context.getString(R.string.chucker_request) } returns "c-req"
            every { context.getString(R.string.chucker_body_omitted) } returns "c-b-om"
            every { context.getString(R.string.chucker_yes) } returns "c-yes"
            every { context.getString(R.string.chucker_no) } returns "c-no"
        }

        @Test
        fun emptyTransaction() {
            assertEquals(
                """
                c-url: 
                c-method: 
                c-prot: 
                c-stat: Requested
                c-res: 
                c-ssl: c-no
                
                c-req-time: 
                c-res-time: 
                c-dur: 
                
                c-req-size: 0 B
                c-res-size: 
                c-size: 0 B
                
                ---------- c-req ----------
                
                
                
                
                ---------- c-res ----------
                


            """.trimIndent(), getShareText(context, HttpTransaction())
            )
        }

        @Test
        fun realTransaction() {
            val httpTransaction = HttpTransaction(
                id = 1,
                requestDate = 2,
                responseDate = 3,
                tookMs = 4,
                protocol = "https",
                method = "GET",
                url = "https://example.com/",
                host = "example.com",
                path = "/",
                scheme = "https",
                requestContentLength = 5,
                requestContentType = "text/plain",
                requestHeaders = "[{\"name\": \"a\", \"value\": \"b\"}]",
                requestBody = "Request Body",
                isRequestBodyPlainText = true,
                responseCode = 418,
                responseMessage = "d",
                error = "e",
                responseContentLength = 6,
                responseContentType = "text/plain",
                responseHeaders = "[{\"name\": \"c\", \"value\": \"d\"}]",
                responseBody = "Response Body",
                isResponseBodyPlainText = true,
                responseImageData = null
            )
            assertEquals(
                """
                    c-url: https://example.com/
                    c-method: GET
                    c-prot: https
                    c-stat: Failed
                    c-res: e
                    c-ssl: c-yes
                    
                    c-req-time: 2
                    c-res-time: 3
                    c-dur: 4 ms
                    
                    c-req-size: 5 B
                    c-res-size: 6 B
                    c-size: 11 B
                    
                    ---------- c-req ----------
                    
                    a: b
                    
                    Request Body
                    
                    ---------- c-res ----------
                    
                    c: d
                    
                    Response Body
            """.trimIndent(), getShareText(context, httpTransaction)
            )
        }
    }

    @Nested
    inner class HeaderFormattingWithFormatting {
        @Test
        fun nullList() {
            assertEquals("", formatHeaders(null, true))
        }

        @Test
        fun emptyList() {
            assertEquals("", formatHeaders(emptyList<HttpHeader>(), true))
        }

        @Test
        fun singleHeader() {
            assertEquals(
                "<b>foo: </b>bar<br />",
                formatHeaders(
                    listOf(
                        HttpHeader("foo", "bar")
                    ), true
                )
            )
        }

        @Test
        fun singleHeaderWithEmptyValue() {
            assertEquals(
                "<b>foo: </b><br />",
                formatHeaders(
                    listOf(
                        HttpHeader("foo", "")
                    ), true
                )
            )
        }

        @Test
        fun multipleHeaders() {
            assertEquals(
                "<b>foo: </b>bar<br /><b>baz: </b>zot<br /><b>apples: </b>oranges<br />",
                formatHeaders(
                    listOf(
                        HttpHeader("foo", "bar"),
                        HttpHeader("baz", "zot"),
                        HttpHeader("apples", "oranges")
                    ), true
                )
            )
        }
    }

    @Nested
    inner class HeaderFormattingWithoutFormatting {
        @Test
        fun nullList() {
            assertEquals("", formatHeaders(null, false))
        }

        @Test
        fun emptyList() {
            assertEquals("", formatHeaders(emptyList<HttpHeader>(), false))
        }

        @Test
        fun singleHeader() {
            assertEquals(
                "foo: bar\n",
                formatHeaders(
                    listOf(
                        HttpHeader("foo", "bar")
                    ), false
                )
            )
        }

        @Test
        fun singleHeaderWithEmptyValue() {
            assertEquals(
                "foo: \n",
                formatHeaders(
                    listOf(
                        HttpHeader("foo", "")
                    ), false
                )
            )
        }

        @Test
        fun multipleHeaders() {
            assertEquals(
                "foo: bar\nbaz: zot\napples: oranges\n",
                formatHeaders(
                    listOf(
                        HttpHeader("foo", "bar"),
                        HttpHeader("baz", "zot"),
                        HttpHeader("apples", "oranges")
                    ), false
                )
            )
        }
    }

    @Nested
    inner class SizeFormattingSi {
        @Test
        fun zeroBytes() {
            assertEquals("0 B", formatByteCount(0, true))
        }

        @Test
        fun nonZeroBytes() {
            assertEquals("42 B", formatByteCount(42, true))
        }

        @Test
        fun kiloBytes() {
            assertEquals("1.0 kB", formatByteCount(1001, true))
            assertEquals("1.1 kB", formatByteCount(1055, true))
        }

        @Test
        fun megaBytes() {
            assertEquals("1.0 MB", formatByteCount(1020300, true))
            assertEquals("2.0 MB", formatByteCount(2030400, true))
        }
    }

    @Nested
    inner class SizeFormattingNonSi {
        @Test
        fun zeroBytes() {
            assertEquals("0 B", formatByteCount(0, false))
        }

        @Test
        fun nonZeroBytes() {
            assertEquals("42 B", formatByteCount(42, false))
        }

        @Test
        fun kiloBytes() {
            assertEquals("1001 B", formatByteCount(1001, false))
            assertEquals("1.0 KiB", formatByteCount(1055, false))
        }

        @Test
        fun megaBytes() {
            assertEquals("996.4 KiB", formatByteCount(1020300, false))
            assertEquals("1.9 MiB", formatByteCount(2030400, false))
        }
    }

    @Nested
    inner class SizeFormattingExtensionFunctions {
        @Test
        fun integerZeroBytes() {
            assertEquals("0 B", 0.formatBytes())
        }

        @Test
        fun longZeroBytes() {
            assertEquals("0 B", 0L.formatBytes())
        }

        @Test
        fun integerBytes() {
            assertEquals("23 B", 23.formatBytes())
        }

        @Test
        fun longBytes() {
            assertEquals("23 B", 23L.formatBytes())
        }

        @Test
        fun integerKB() {
            assertEquals("23.0 kB", 23000.formatBytes())
        }

        @Test
        fun longKB() {
            assertEquals("23.0 kB", 23000L.formatBytes())
        }

        @Test
        fun integerMB() {
            assertEquals("23.0 MB", 23000000.formatBytes())
        }

        @Test
        fun longMB() {
            assertEquals("23.0 MB", 23000000L.formatBytes())
        }

        @Test
        fun handlesNullableInteger() {
            val value: Int? = null
            assertEquals("", value.formatBytes())
        }

        @Test
        fun handlesNullableLong() {
            val value: Long? = null
            assertEquals("", value.formatBytes())
        }
    }

    @Nested
    inner class JsonFormatting {
        @Test
        fun testFormatJson_withNullValues() {
            val parsedJson = formatJson(
                """
            {
              "field": null
            }
            """.trimIndent()
            )

            assertEquals(
                """
            {
              "field": null
            }
            """.trimIndent(),
                parsedJson
            )
        }

        @Test
        fun testFormatJson_withEmptyValues() {
            val parsedJson = formatJson(
                """
            {
              "field": ""
            }
            """.trimIndent()
            )

            assertEquals(
                """
            {
              "field": ""
            }
            """.trimIndent(),
                parsedJson
            )
        }

        @Test
        fun testFormatJson_willPrettyPrint() {
            val parsedJson = formatJson(
                """{ "field1": "something", "field2": "else" }"""
            )

            assertEquals(
                """
            {
              "field1": "something",
              "field2": "else"
            }
            """.trimIndent(),
                parsedJson
            )
        }
    }
}