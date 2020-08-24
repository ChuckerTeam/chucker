package com.chuckerteam.chucker.internal.support

import com.chuckerteam.chucker.BuildConfig
import com.chuckerteam.chucker.TestTransactionFactory
import com.chuckerteam.chucker.internal.data.har.Entry
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

internal class HarUtilsTest {
    @Test fun fromHttpTransactions_createsHarWithMultipleEntries() {
        val getTransaction = TestTransactionFactory.createTransaction("GET")
        val postTransaction = TestTransactionFactory.createTransaction("POST")
        val har = HarUtils.fromHttpTransactions(listOf(getTransaction, postTransaction))
        assertThat(har.log.entries).hasSize(2)
        assertThat(har.log.entries[0].request!!.method).isEqualTo("GET")
        assertThat(har.log.entries[1].request!!.method).isEqualTo("POST")
    }

    @Test fun harString_createsJsonString(): Unit = runBlocking {
        val transaction = TestTransactionFactory.createTransaction("GET")
        assertThat(HarUtils.harStringFromTransactions(listOf(transaction))).isEqualTo(
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
                    "startedDateTime": "${Instant.ofEpochMilli(transaction.requestDate!!).atZone(ZoneId.systemDefault()).format(Entry.DATE_FORMAT)}",
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
