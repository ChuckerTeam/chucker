package com.chuckerteam.chucker.internal.support

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.chuckerteam.chucker.internal.data.entity.HttpHeader
import com.chuckerteam.chucker.util.TestTransactionFactory
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class TransactionCurlCommandSharableTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    private val requestMethods = listOf("GET", "POST", "PUT", "DELETE")

    @Test
    fun `create cURL command without headers`() {
        requestMethods.forEach { method ->
            val transaction = TestTransactionFactory.createTransaction(method)
            val sharableTransaction = TransactionCurlCommandSharable(transaction)

            val sharedContent = sharableTransaction.toSharableUtf8Content(context)

            assertThat(sharedContent).isEqualTo("curl -X $method http://localhost/getUsers")
        }
    }

    @Test
    fun `create cURL command with headers`() {
        val headers = List(5) { index -> HttpHeader("name$index", "value$index") }
        val convertedHeaders = JsonConverter.instance.toJson(headers)

        requestMethods.forEach { method ->
            val transaction = TestTransactionFactory.createTransaction(method).apply {
                requestHeaders = convertedHeaders
            }
            val sharableTransaction = TransactionCurlCommandSharable(transaction)
            val expectedCurlCommand = buildString {
                append("curl -X $method")
                for (header in headers) {
                    append(" -H \"${header.name}: ${header.value}\"")
                }
                append(" http://localhost/getUsers")
            }

            val sharedContent = sharableTransaction.toSharableUtf8Content(context)

            assertThat(sharedContent).isEqualTo(expectedCurlCommand)
        }
    }

    @Test
    fun `create cURL command with request bodies for PUT and POST methods`() {
        requestMethods.filter { it in listOf("POST", "PUT") }.forEach { method ->
            val dummyRequestBody = "{thing:put}"
            val transaction = TestTransactionFactory.createTransaction(method).apply {
                requestBody = dummyRequestBody
            }
            val shareableTransaction = TransactionCurlCommandSharable(transaction)
            val expectedCurlCommand =
                "curl -X $method --data $'$dummyRequestBody' http://localhost/getUsers"

            val sharedContent = shareableTransaction.toSharableUtf8Content(context)

            assertThat(sharedContent).isEqualTo(expectedCurlCommand)
        }
    }

    @Test
    fun `create cURL command with gzip header`() {
        val headers = listOf(HttpHeader("Accept-Encoding", "gzip"))
        val convertedHeader = JsonConverter.instance.toJson(headers)

        requestMethods.forEach { method ->
            val transaction = TestTransactionFactory.createTransaction(method).apply {
                requestHeaders = convertedHeader
            }
            val sharableTransaction = TransactionCurlCommandSharable(transaction)

            val sharedContent = sharableTransaction.toSharableUtf8Content(context)

            val expected = "curl -X $method -H \"Accept-Encoding: gzip\" --compressed http://localhost/getUsers"

            assertThat(sharedContent).isEqualTo(expected)
        }
    }

    @Test
    fun `create cURL command with brotli header`() {
        val headers = listOf(HttpHeader("Accept-Encoding", "br"))
        val convertedHeader = JsonConverter.instance.toJson(headers)

        requestMethods.forEach { method ->
            val transaction = TestTransactionFactory.createTransaction(method).apply {
                requestHeaders = convertedHeader
            }
            val sharableTransaction = TransactionCurlCommandSharable(transaction)

            val sharedContent = sharableTransaction.toSharableUtf8Content(context)

            val expected = "curl -X $method -H \"Accept-Encoding: br\" --compressed http://localhost/getUsers"

            assertThat(sharedContent).isEqualTo(expected)
        }
    }
}
