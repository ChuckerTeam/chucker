package com.chuckerteam.chucker.internal.support

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.chuckerteam.chucker.TestTransactionFactory
import com.chuckerteam.chucker.internal.data.entity.HttpHeader
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class TransactionCurlCommandSharableTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    private val requestMethods = listOf("GET", "POST", "PUT", "DELETE")

    @Test
    fun curlCommandWithoutHeaders() {
        requestMethods.forEach { method ->
            val transaction = TestTransactionFactory.createTransaction(method)
            val sharableTransaction = TransactionCurlCommandSharable(transaction)

            val sharedContent = sharableTransaction.toSharableUtf8Content(context)

            assertThat(sharedContent).isEqualTo("curl -X $method http://localhost/getUsers")
        }
    }

    @Test
    fun curlCommandWithHeaders() {
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
    fun curlPostAndPutCommandsWithRequestBodies() {
        requestMethods.filter { it in listOf("POST", "PUT") }.forEach { method ->
            val dummyRequestBody = "{thing:put}"
            val transaction = TestTransactionFactory.createTransaction(method).apply {
                requestBody = dummyRequestBody
            }
            val shareableTransaction = TransactionCurlCommandSharable(transaction)
            val expectedCurlCommand = "curl -X $method --data $'$dummyRequestBody' http://localhost/getUsers"

            val sharedContent = shareableTransaction.toSharableUtf8Content(context)

            assertThat(sharedContent).isEqualTo(expectedCurlCommand)
        }
    }
}
