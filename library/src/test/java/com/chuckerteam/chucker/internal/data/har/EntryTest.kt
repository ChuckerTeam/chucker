package com.chuckerteam.chucker.internal.data.har

import com.chuckerteam.chucker.internal.data.har.log.Entry
import com.chuckerteam.chucker.internal.data.har.log.entry.Request
import com.chuckerteam.chucker.internal.data.har.log.entry.Response
import com.chuckerteam.chucker.internal.data.har.log.entry.Timings
import com.chuckerteam.chucker.internal.data.har.log.entry.request.PostData
import com.chuckerteam.chucker.internal.data.har.log.entry.response.Content
import com.chuckerteam.chucker.util.HarTestUtils
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Date

internal class EntryTest {
    @Test
    fun `entry is created correctly with start datetime`() {
        val transaction = HarTestUtils.createTransaction("GET")
        val entry = HarTestUtils.createEntry("GET")

        assertThat(Entry.DateFormat.get()!!.parse(entry?.startedDateTime))
            .isEqualTo(Date(transaction.requestDate!!))
    }

    @Test
    fun `entry is created correctly with time`() {
        val entry = HarTestUtils.createEntry("GET")

        assertThat(entry?.time).isEqualTo(1000)
    }

    @Test
    fun `entry is created correctly with request`() {
        val entry = HarTestUtils.createEntry("POST")

        assertThat(entry?.request).isEqualTo(
            Request(
                method = "POST",
                url = "http://localhost:80/getUsers",
                httpVersion = "HTTP",
                cookies = emptyList(),
                headers = emptyList(),
                queryString = emptyList(),
                postData = PostData(mimeType = "application/json", params = null, text = null),
                headersSize = 0,
                bodySize = 1000,
                totalSize = 1000
            )
        )
    }

    @Test
    fun `entry is created correctly with response`() {
        val entry = HarTestUtils.createEntry("GET")

        assertThat(entry?.response).isEqualTo(
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
                headersSize = 0,
                bodySize = 1000,
                totalSize = 1000
            )
        )
    }

    @Test
    fun `entry is created correctly with timing`() {
        val transaction = HarTestUtils.createTransaction("GET")
        val entry = HarTestUtils.createEntry("GET")

        assertThat(entry?.timings).isEqualTo(Timings(transaction))
    }
}
