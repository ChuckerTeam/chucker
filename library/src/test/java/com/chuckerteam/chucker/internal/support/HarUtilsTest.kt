package com.chuckerteam.chucker.internal.support

import com.chuckerteam.chucker.internal.data.har.log.Creator
import com.chuckerteam.chucker.internal.data.har.log.Entry
import com.chuckerteam.chucker.util.TestTransactionFactory
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.Date

internal class HarUtilsTest {
    @Test
    fun fromHttpTransactions_createsHarWithMultipleEntries() {
        val getTransaction = TestTransactionFactory.createTransaction("GET")
        val postTransaction = TestTransactionFactory.createTransaction("POST")
        val creator = Creator("Chucker", "3.5.2")
        val har = HarUtils.fromHttpTransactions(listOf(getTransaction, postTransaction), creator)
        assertThat(har.log.entries).hasSize(2)
        assertThat(har.log.entries[0].request.method).isEqualTo("GET")
        assertThat(har.log.entries[1].request.method).isEqualTo("POST")
    }

    @Test
    fun harString_createsJsonString(): Unit = runBlocking {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val creator = Creator("Chucker", "3.5.2")
        val result = HarUtils.harStringFromTransactions(listOf(transaction), creator)
        val startedDateTime = Entry.DateFormat.get()!!.format(Date(transaction.requestDate!!))
        assertThat(result).isEqualTo(
            """
                {
                  "log": {
                    "version": "1.2",
                    "creator": {
                      "name": "Chucker",
                      "version": "3.5.2"
                    },
                    "entries": [
                      {
                        "startedDateTime": "$startedDateTime",
                        "time": 1000,
                        "request": {
                          "method": "GET",
                          "url": "http://localhost:80/getUsers",
                          "httpVersion": "HTTP",
                          "cookies": [],
                          "headers": [],
                          "queryString": [],
                          "postData": {
                            "mimeType": "application/json"
                          },
                          "headersSize": -1,
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
                          "headersSize": -1,
                          "bodySize": 1000,
                          "totalSize": 1000
                        },
                        "cache": {},
                        "timings": {
                          "blocked": -1,
                          "dns": -1,
                          "ssl": -1,
                          "connect": -1,
                          "send": 0,
                          "wait": 0,
                          "receive": 1000
                        }
                      }
                    ]
                  }
                }
            """.trimIndent()
        )
    }
}
