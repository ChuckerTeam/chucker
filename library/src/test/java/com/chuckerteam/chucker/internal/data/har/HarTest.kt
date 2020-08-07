package com.chuckerteam.chucker.internal.data.har

import com.chuckerteam.chucker.BuildConfig
import com.chuckerteam.chucker.TestTransactionFactory
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.Date

class HarTest {
    @Test fun fromHttpTransactions_createsHarWithCorrectValues() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val har = Har.fromHttpTransactions(listOf(transaction))
        assertThat(har.log.version).isEqualTo("1.2")
        assertThat(har.log.creator).isEqualTo(Creator("com.chuckerteam.chucker", BuildConfig.VERSION_NAME))
        assertThat(har.log.entries).hasSize(1)
        val entry = har.log.entries[0]
        assertThat(Har.DateFormat.get()!!.parse(entry.startedDateTime)).isEqualTo(Date(transaction.requestDate!!))
        assertThat(entry.time).isEqualTo(1000)
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

    @Test fun fromHttpTransactions_createsHarWithMultipleEntries() {
        val getTransaction = TestTransactionFactory.createTransaction("GET")
        val postTransaction = TestTransactionFactory.createTransaction("POST")
        val har = Har.fromHttpTransactions(listOf(getTransaction, postTransaction))
        assertThat(har.log.entries).hasSize(2)
        assertThat(har.log.entries[0].request!!.method).isEqualTo("GET")
        assertThat(har.log.entries[1].request!!.method).isEqualTo("POST")
    }

    @Test fun harString_createsJsonString(): Unit = runBlocking {
        val transaction = TestTransactionFactory.createTransaction("GET")
        assertThat(Har.harStringFromTransactions(listOf(transaction))).isEqualTo(
            """
            {
              "log": {
                "version": "1.2",
                "creator": {
                  "name": "com.chuckerteam.chucker",
                  "version": "${BuildConfig.VERSION_NAME}"
                },
                "entries": [
                  {
                    "startedDateTime": "${Har.DateFormat.get()!!.format(Date(transaction.requestDate!!))}",
                    "time": 1000,
                    "request": {
                      "method": "GET",
                      "url": "http://localhost:80/getUsers",
                      "httpVersion": "HTTP",
                      "cookies": [],
                      "headers": [],
                      "queryString": [],
                      "postData": {
                        "size": 1000,
                        "mimeType": "application/json",
                        "text": ""
                      },
                      "headersSize": 0,
                      "bodySize": 1000
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
                      "timings": {
                        "send": 0,
                        "wait": 0,
                        "receive": 1000
                      }
                    }
                  }
                ]
              }
            }
            """.trimIndent()
        )
    }
}
