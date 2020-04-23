package com.chuckerteam.chucker.internal.support

import android.content.Context
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.TestTransactionFactory
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import io.mockk.every
import io.mockk.mockk
import kotlin.random.Random
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
            FormatUtils.getShareText(contextMock, it, false)
        }

        assert(actualStringFromTransactions == expectedStringFromTransactions)
    }

    private fun getRandomHttpMethod(): String = arrayOf("GET", "POST", "PUT", "DELETE")[Random.nextInt(0, 4)]
}
