package com.chuckerteam.chucker.internal.support

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.chuckerteam.chucker.util.TestTransactionFactory
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class TransactionListDetailsSharableTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `create sharable content for multiple transactions`() {
        val transactions = List(10) {
            TestTransactionFactory.createTransaction(getRandomHttpMethod())
        }
        val expectedSharedContent = transactions.joinToString(
            separator = "\n==================\n",
            prefix = "/* Export Start */\n",
            postfix = "\n/* Export End */\n"
        ) { TransactionDetailsSharable(it, encodeUrls = false).toSharableUtf8Content(context) }

        val sharedContent = TransactionListDetailsSharable(
            transactions,
            encodeUrls = false,
        ).toSharableUtf8Content(context)
        assertThat(sharedContent).isEqualTo(expectedSharedContent)
    }

    private val requestMethods = listOf("GET", "POST", "PUT", "DELETE")

    private fun getRandomHttpMethod(): String = requestMethods.random()
}
