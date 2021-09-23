package com.chuckerteam.chucker.internal.data.har

import com.chuckerteam.chucker.internal.data.har.log.entry.Request
import com.chuckerteam.chucker.internal.data.har.log.entry.request.PostData
import com.chuckerteam.chucker.util.TestTransactionFactory
import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class RequestTest {
    @Test
    fun fromHttpTransaction_createsRequestWithCorrectMethod() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val request = Request(transaction)

        assertThat(request.method).isEqualTo("GET")
    }

    @Test
    fun fromHttpTransaction_createsRequestWithCorrectUrl() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val request = Request(transaction)

        assertThat(request.url).isEqualTo("http://localhost:80/getUsers")
    }

    @Test
    fun fromHttpTransaction_createsRequestWithCorrectHttpVersion() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val request = Request(transaction)

        assertThat(request.httpVersion).isEqualTo("HTTP")
    }

    @Test
    fun fromHttpTransaction_createsRequestWithCorrectPostData() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val request = Request(transaction)

        assertThat(request.postData)
            .isEqualTo(PostData(mimeType = "application/json", params = null, text = null))
    }

    @Test
    fun fromHttpTransaction_createsRequestWithCorrectBodySize() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val request = Request(transaction)

        assertThat(request.bodySize).isEqualTo(1000)
    }
}
