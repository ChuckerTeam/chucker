package com.chuckerteam.chucker.internal.data.har

import com.chuckerteam.chucker.TestTransactionFactory
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Date

internal class EntryTest {
    @Test fun fromHttpTransaction_createsEntryWithCorrectStartedDateTime() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val entry = Entry.fromHttpTransaction(transaction)

        assertThat(Entry.DateFormat.get()!!.parse(entry.startedDateTime)).isEqualTo(Date(transaction.requestDate!!))
        assertThat(entry.time).isEqualTo(1000)
    }

    @Test fun fromHttpTransaction_createsEntryWithCorrectTime() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val entry = Entry.fromHttpTransaction(transaction)

        assertThat(entry.time).isEqualTo(1000)
    }

    @Test fun fromHttpTransaction_createsEntryWithCorrectRequest() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val entry = Entry.fromHttpTransaction(transaction)

        assertThat(entry.request).isEqualTo(
            Request(
                method = "GET",
                url = "http://localhost:80/getUsers",
                httpVersion = "HTTP",
                cookies = emptyList(),
                headers = emptyList(),
                queryString = emptyList(),
                postData = PostData(size = 1000, mimeType = "application/json", text = ""),
                headersSize = 0,
                bodySize = 1000
            )
        )
    }

    @Test fun fromHttpTransaction_createsEntryWithCorrectResponse() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val entry = Entry.fromHttpTransaction(transaction)

        assertThat(entry.response).isEqualTo(
            Response(
                status = 200,
                statusText = "OK",
                httpVersion = "HTTP",
                cookies = emptyList(),
                headers = emptyList(),
                content = PostData(
                    size = 1000,
                    mimeType = "application/json",
                    text =
                        """{"field": "value"}"""
                ),
                redirectUrl = "",
                headersSize = 0,
                bodySize = 1000,
                timings = Timings(send = 0, wait = 0, receive = 1000)
            )
        )
    }
}
