package com.chuckerteam.chucker.internal.data.har

import com.chuckerteam.chucker.internal.data.har.log.entry.Response
import com.chuckerteam.chucker.internal.data.har.log.entry.response.Content
import com.chuckerteam.chucker.util.TestTransactionFactory
import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class ResponseTest {
    @Test
    fun fromHttpTransaction_createsResponseWithCorrectStatus() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val response = Response(transaction)

        assertThat(response.status).isEqualTo(200)
    }

    @Test
    fun fromHttpTransaction_createsResponseWithCorrectStatusText() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val response = Response(transaction)

        assertThat(response.statusText).isEqualTo("OK")
    }

    @Test
    fun fromHttpTransaction_createsResponseWithCorrectHttpVersion() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val response = Response(transaction)

        assertThat(response.httpVersion).isEqualTo("HTTP")
    }

    @Test
    fun fromHttpTransaction_createsResponseWithCorrectContent() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val response = Response(transaction)

        assertThat(response.content).isEqualTo(
            Content(
                size = 1000,
                compression = null,
                mimeType = "application/json",
                text = """{"field": "value"}""",
                encoding = null
            )
        )
    }

    @Test
    fun fromHttpTransaction_createsResponseWithCorrectBodySize() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val response = Response(transaction)

        assertThat(response.bodySize).isEqualTo(1000)
    }
}
