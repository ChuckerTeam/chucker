package com.chuckerteam.chucker.internal.support

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.har.log.Entry
import com.chuckerteam.chucker.util.HarTestUtils
import com.chuckerteam.chucker.util.HarTestUtils.createHarString
import com.chuckerteam.chucker.util.HarTestUtils.createListTransactionHar
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date

@RunWith(RobolectricTestRunner::class)
internal class HarUtilsTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `entry list is created correctly with different methods`() {
        val har = context.createListTransactionHar()

        assertThat(har.log.entries).hasSize(2)
        assertThat(har.log.entries[0].request.method).isEqualTo("GET")
        assertThat(har.log.entries[1].request.method).isEqualTo("POST")
    }

    @Test
    fun `har content is created correctly`(): Unit = runBlocking {
        val transaction = HarTestUtils.createTransaction("POST")
        val result = context.createHarString()
        val chuckerName = context.getString(R.string.chucker_name)
        val chuckerVersion = context.getString(R.string.chucker_version)
        val startedDateTime = Entry.DateFormat.get()!!.format(Date(transaction.requestDate!!))

        assertThat(result).isEqualTo(
            """
                {
                  "log": {
                    "version": "1.2",
                    "creator": {
                      "name": "$chuckerName",
                      "version": "$chuckerVersion"
                    },
                    "entries": [
                      {
                        "startedDateTime": "$startedDateTime",
                        "time": 1000,
                        "request": {
                          "method": "POST",
                          "url": "http://localhost:80/getUsers",
                          "httpVersion": "HTTP",
                          "cookies": [],
                          "headers": [],
                          "queryString": [],
                          "postData": {
                            "mimeType": "application/json"
                          },
                          "headersSize": 0,
                          "bodySize": 1000,
                          "totalSize": 1000
                        },
                        "response": {
                          "status": 200,
                          "statusText": "OK",
                          "httpVersion": "HTTP",
                          "cookies": [],
                          "headers": [],
                          "content": {
                            "size": 1000,
                            "mimeType": "application/json",
                            "text": "{\"field\": \"value\"}"
                          },
                          "redirectURL": "",
                          "headersSize": 0,
                          "bodySize": 1000,
                          "totalSize": 1000
                        },
                        "cache": {},
                        "timings": {
                          "connect": 0,
                          "send": 0,
                          "wait": 1000,
                          "receive": 0,
                          "comment": "The information described by this object is incomplete."
                        }
                      }
                    ]
                  }
                }
            """.trimIndent()
        )
    }
}
