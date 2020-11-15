package com.chuckerteam.chucker.internal.support

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.chuckerteam.chucker.TestTransactionFactory
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class TransactionDetailsSharableTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun buildCorrectGetTransactionContent() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val sharableTransaction = TransactionDetailsSharable(transaction, encodeUrls = false)

        val sharedContent = sharableTransaction.toSharableUtf8Content(context)

        assertThat(sharedContent).isEqualTo(TestTransactionFactory.expectedGetHttpTransaction)
    }

    @Test
    fun buildCorrectPostTransactionContent() {
        val transaction = TestTransactionFactory.createTransaction("POST")
        val sharableTransaction = TransactionDetailsSharable(transaction, encodeUrls = false)

        val sharedContent = sharableTransaction.toSharableUtf8Content(context)

        assertThat(sharedContent).isEqualTo(TestTransactionFactory.expectedHttpPostTransaction)
    }
}
