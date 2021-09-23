package com.chuckerteam.chucker.internal.data.har

import com.chuckerteam.chucker.internal.data.har.log.Entry
import com.chuckerteam.chucker.internal.data.har.log.entry.Request
import com.chuckerteam.chucker.internal.data.har.log.entry.Response
import com.chuckerteam.chucker.internal.data.har.log.entry.Timings
import com.chuckerteam.chucker.internal.data.har.log.entry.request.PostData
import com.chuckerteam.chucker.internal.data.har.log.entry.response.Content
import com.chuckerteam.chucker.util.TestTransactionFactory
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Date

internal class EntryTest {
    @Test
    fun fromHttpTransaction_createsEntryWithCorrectStartedDateTime() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val entry = Entry(transaction)

        assertThat(Entry.DateFormat.get()!!.parse(entry.startedDateTime)).isEqualTo(Date(transaction.requestDate!!))
        assertThat(entry.time).isEqualTo(1000)
    }

    @Test
    fun fromHttpTransaction_createsEntryWithCorrectTime() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val entry = Entry(transaction)

        assertThat(entry.time).isEqualTo(1000)
    }

    @Test
    fun fromHttpTransaction_createsEntryWithCorrectRequest() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val entry = Entry(transaction)

        assertThat(entry.request).isEqualTo(
            Request(
                method = "GET",
                url = "http://localhost:80/getUsers",
                httpVersion = "HTTP",
                cookies = emptyList(),
                headers = emptyList(),
                queryString = emptyList(),
                postData = PostData(mimeType = "application/json", params = null, text = null),
                headersSize = -1,
                bodySize = 1000,
                totalSize = 1000
            )
        )
    }

    @Test
    fun fromHttpTransaction_createsEntryWithCorrectResponse() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val entry = Entry(transaction)

        assertThat(entry.response).isEqualTo(
            Response(
                status = 200,
                statusText = "OK",
                httpVersion = "HTTP",
                cookies = emptyList(),
                headers = emptyList(),
                content = Content(
                    size = 1000,
                    compression = null,
                    mimeType = "application/json",
                    text = """{"field": "value"}""",
                    encoding = null
                ),
                redirectUrl = "",
                headersSize = -1,
                bodySize = 1000,
                totalSize = 1000
            )
        )
    }

    @Test
    fun fromHttpTransaction_createsEntryWithCorrectTimings() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val entry = Entry(transaction)

        assertThat(entry.timings).isEqualTo(Timings(transaction))
    }
}
