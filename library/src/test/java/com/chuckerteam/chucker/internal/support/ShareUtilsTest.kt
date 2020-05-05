package com.chuckerteam.chucker.internal.support

import android.content.Context
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.TestTransactionFactory
import com.chuckerteam.chucker.internal.data.entity.HttpHeader
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test

class ShareUtilsTest {

    private val contextMock = mockk<Context> {
        every { getString(R.string.chucker_url) } returns "URL"
        every { getString(R.string.chucker_method) } returns "Method"
        every { getString(R.string.chucker_protocol) } returns "Protocol"
        every { getString(R.string.chucker_status) } returns "Status"
        every { getString(R.string.chucker_response) } returns "Response"
        every { getString(R.string.chucker_ssl) } returns "SSL"
        every { getString(R.string.chucker_yes) } returns "Yes"
        every { getString(R.string.chucker_no) } returns "No"
        every { getString(R.string.chucker_request_time) } returns "Request time"
        every { getString(R.string.chucker_response_time) } returns "Response time"
        every { getString(R.string.chucker_duration) } returns "Duration"
        every { getString(R.string.chucker_request_size) } returns "Request size"
        every { getString(R.string.chucker_response_size) } returns "Response size"
        every { getString(R.string.chucker_total_size) } returns "Total size"
        every { getString(R.string.chucker_request) } returns "Request"
        every { getString(R.string.chucker_body_omitted) } returns "(encoded or binary body omitted)"
        every { getString(R.string.chucker_export_separator) } returns "=================="
        every { getString(R.string.chucker_export_prefix) } returns "/* Export Start */"
        every { getString(R.string.chucker_export_postfix) } returns "/*  Export End  */"
        every { getString(R.string.chucker_body_empty) } returns "(body is empty)"
    }

    private val requestMethods = arrayOf("GET", "POST", "PUT", "DELETE")

    private fun getRandomHttpMethod(): String = requestMethods.random()

    @Test
    fun isStringFromTransactionsListValid() {
        val transactionList = ArrayList<HttpTransaction>()

        repeat(10) {
            transactionList.add(TestTransactionFactory.createTransaction(getRandomHttpMethod()))
        }

        val actualStringFromTransactions = runBlocking {
            ShareUtils.getStringFromTransactions(transactionList, contextMock)
        }
        val expectedStringFromTransactions = transactionList.joinToString(
            separator = "\n==================\n",
            prefix = "/* Export Start */\n",
            postfix = "\n/*  Export End  */\n"
        ) {
            ShareUtils.getShareText(contextMock, it, false)
        }

        assertThat(actualStringFromTransactions).isEqualTo(expectedStringFromTransactions)
    }

    @Test
    fun testCurlCommandWithoutHeaders() {
        requestMethods.forEach { method ->
            val transaction = TestTransactionFactory.createTransaction(method)
            val curlCommand = ShareUtils.getShareCurlCommand(transaction)
            val expectedCurlCommand = "curl -X $method http://localhost/getUsers"
            assertThat(curlCommand).isEqualTo(expectedCurlCommand)
        }
    }

    @Test
    fun testCurlCommandWithHeaders() {
        val httpHeaders = ArrayList<HttpHeader>()
        for (i in 0 until 5) {
            httpHeaders.add(HttpHeader("name$i", "value$i"))
        }
        val dummyHeaders = JsonConverter.instance.toJson(httpHeaders)

        requestMethods.forEach { method ->
            val transaction = TestTransactionFactory.createTransaction(method)
            transaction.requestHeaders = dummyHeaders
            val curlCommand = ShareUtils.getShareCurlCommand(transaction)
            var expectedCurlCommand = "curl -X $method"
            httpHeaders.forEach { header ->
                expectedCurlCommand += " -H \"${header.name}: ${header.value}\""
            }
            expectedCurlCommand += " http://localhost/getUsers"
            assertThat(curlCommand).isEqualTo(expectedCurlCommand)
        }
    }

    @Test
    fun testCurlPostAndPutCommandWithRequestBody() {
        requestMethods.filter { method ->
            method == "POST" || method == "PUT"
        }.forEach { method ->
            val dummyRequestBody = "{thing:put}"
            val transaction = TestTransactionFactory.createTransaction(method)
            transaction.requestBody = dummyRequestBody
            val curlCommand = ShareUtils.getShareCurlCommand(transaction)
            val expectedCurlCommand = "curl -X $method --data $'$dummyRequestBody' http://localhost/getUsers"
            assertThat(curlCommand).isEqualTo(expectedCurlCommand)
        }
    }

    @Test
    fun getShareTextForGetTransaction() {
        val shareText = getShareText("GET")
        assertThat(shareText).isEqualTo(TestTransactionFactory.expectedGetHttpTransaction)
    }

    @Test
    fun getShareTextForPostTransaction() {
        val shareText = getShareText("POST")
        assertThat(shareText).isEqualTo(TestTransactionFactory.expectedHttpPostTransaction)
    }

    private fun getShareText(method: String): String {
        return ShareUtils.getShareText(
            contextMock,
            TestTransactionFactory.createTransaction(method),
            false
        )
    }
}
