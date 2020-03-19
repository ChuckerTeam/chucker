package com.chuckerteam.chucker.internal.support

import MockTransactionFactory
import android.content.Context
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.HttpHeader
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class FormatUtilsTest {

    private val exampleHeadersList = listOf(
        HttpHeader("Accept", "text/html"),
        HttpHeader("Authorization", "exampleToken")
    )

    private val contextMock = mockk<Context> {
        every { getString(R.string.chucker_url) } returns "URL"
        every { getString(R.string.chucker_method) } returns "Method"
        every { getString(R.string.chucker_protocol) } returns "Protocol"
        every { getString(R.string.chucker_status) } returns "Status"
        every { getString(R.string.chucker_response) } returns "Response"
        every { getString(R.string.chucker_ssl) } returns "SSL"
        every { getString(R.string.chucker_yes) } returns "Yes"
        every { getString(R.string.chucker_no) } returns "No"
        every { getString(R.string.chucker_request_time) } returns "Request time"
        every { getString(R.string.chucker_response_time) } returns "Response time"
        every { getString(R.string.chucker_duration) } returns "Duration"
        every { getString(R.string.chucker_request_size) } returns "Request size"
        every { getString(R.string.chucker_response_size) } returns "Response size"
        every { getString(R.string.chucker_total_size) } returns "Total size"
        every { getString(R.string.chucker_request) } returns "Request"
        every { getString(R.string.chucker_body_omitted) } returns "(encoded or binary body omitted)"
    }

    @Test
    fun testFormatJson_withNullValues() {
        val parsedJson = FormatUtils.formatJson(
            """
            {
              "field": null
            }
            """.trimIndent()
        )

        assertThat(parsedJson).isEqualTo(
            """
            {
              "field": null
            }
            """.trimIndent()
        )
    }

    @Test
    fun testFormatJson_withEmptyValues() {
        val parsedJson = FormatUtils.formatJson(
            """
            {
              "field": ""
            }
            """.trimIndent()
        )

        assertThat(parsedJson).isEqualTo(
            """
            {
              "field": ""
            }
            """.trimIndent()
        )
    }

    @Test
    fun testFormatJson_willPrettyPrint() {
        val parsedJson = FormatUtils.formatJson(
            """{ "field1": "something", "field2": "else" }"""
        )

        assertThat(parsedJson).isEqualTo(
            """
            {
              "field1": "something",
              "field2": "else"
            }
            """.trimIndent()
        )
    }

    @Test
    fun testFormatHeaders_withNullValues() {
        val result = FormatUtils.formatHeaders(null, false)
        assertThat(result).isEmpty()
    }

    @Test
    fun testFormatHeaders_withEmptyValues() {
        val result = FormatUtils.formatHeaders(listOf(), false)
        assertThat(result).isEmpty()
    }

    @Test
    fun testFormatHeaders_withoutMarkup() {
        val result = FormatUtils.formatHeaders(exampleHeadersList, false)
        val expected = "Accept: text/html\nAuthorization: exampleToken\n"
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun testFormatHeaders_withMarkup() {
        val result = FormatUtils.formatHeaders(exampleHeadersList, true)
        val expected = "<b> Accept: </b>text/html <br /><b> Authorization: </b>exampleToken <br />"
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun testFormatByteCount_zeroBytes() {
        val resultNonSi = FormatUtils.formatByteCount(0, false)
        val resultSi = FormatUtils.formatByteCount(0, true)
        val expected = "0 B"
        assertThat(resultNonSi).isEqualTo(expected)
        assertThat(resultSi).isEqualTo(expected)
    }

    @Test
    fun testFormatByteCount_oneKiloByte() {
        val oneKb = 1024L
        val resultNonSi = FormatUtils.formatByteCount(oneKb, false)
        val resultSi = FormatUtils.formatByteCount(oneKb, true)
        val expectedNonSi = "1.0 KiB"
        val expectedSi = "1.0 kB"
        assertThat(resultNonSi).isEqualTo(expectedNonSi)
        assertThat(resultSi).isEqualTo(expectedSi)
    }

    @Test
    fun formatXml_emptyString() {
        assertThat(FormatUtils.formatXml("")).isEmpty()
    }

    @Test
    fun formatXml_properXml() {
        val xml =
            """<example>value</example>"""
        val expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<example>value</example>\n"
        assertThat(FormatUtils.formatXml(xml)).isEqualTo(expected)
    }

    @Test
    fun getShareTextForGetTransaction() {
        assertThat(
            FormatUtils.getShareText(contextMock, MockTransactionFactory.createTransaction("GET"), false)
        )
            .isEqualTo(MockTransactionFactory.expectedGetHttpTransaction)
    }

    @Test
    fun getShareTextForPostTransaction() {
        assertThat(
            FormatUtils.getShareText(contextMock, MockTransactionFactory.createTransaction("POST"), false)
        )
            .isEqualTo(MockTransactionFactory.expectedHttpPostTransaction)
    }
}
