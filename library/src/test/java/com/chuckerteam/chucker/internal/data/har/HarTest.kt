package com.chuckerteam.chucker.internal.data.har

import com.chuckerteam.chucker.BuildConfig
import com.chuckerteam.chucker.TestTransactionFactory
import com.chuckerteam.chucker.internal.support.HarUtils
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HarTest {
    @Test fun fromHttpTransactions_createsHarWithCorrectVersion() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val har = HarUtils.fromHttpTransactions(listOf(transaction))

        assertThat(har.log.version).isEqualTo("1.2")
    }

    @Test fun fromHttpTransactions_createsHarWithCorrectCreator() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val har = HarUtils.fromHttpTransactions(listOf(transaction))

        assertThat(har.log.creator).isEqualTo(Creator("com.chuckerteam.chucker", BuildConfig.VERSION_NAME))
    }

    @Test fun fromHttpTransactions_createsHarWithCorrectEntries() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val har = HarUtils.fromHttpTransactions(listOf(transaction))

        assertThat(har.log.entries).hasSize(1)
    }
}
