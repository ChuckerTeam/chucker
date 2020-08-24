package com.chuckerteam.chucker.internal.data.har

import com.chuckerteam.chucker.TestTransactionFactory
import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class ResponseTest {
    @Test fun fromHttpTransaction_createsResponseWithCorrectStatus() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val response = Response.fromHttpTransaction(transaction)

        assertThat(response?.status).isEqualTo(200)
    }

    @Test fun fromHttpTransaction_createsResponseWithCorrectStatusText() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val response = Response.fromHttpTransaction(transaction)

        assertThat(response?.statusText).isEqualTo("OK")
    }

    @Test fun fromHttpTransaction_createsResponseWithCorrectHttpVersion() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val response = Response.fromHttpTransaction(transaction)

        assertThat(response?.httpVersion).isEqualTo("HTTP")
    }

    @Test fun fromHttpTransaction_createsResponseWithCorrectContent() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val response = Response.fromHttpTransaction(transaction)

        assertThat(response?.content).isEqualTo(
            PostData(
                size = 1000,
                mimeType = "application/json",
                text =
                    """{"field": "value"}"""
            )
        )
    }

    @Test fun fromHttpTransaction_createsResponseWithCorrectBodySize() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val response = Response.fromHttpTransaction(transaction)

        assertThat(response?.bodySize).isEqualTo(1000)
    }

    @Test fun fromHttpTransaction_createsResponseWithCorrectTimings() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val response = Response.fromHttpTransaction(transaction)

        assertThat(response?.timings).isEqualTo(Timings(send = 0, wait = 0, receive = 1000))
    }
}
