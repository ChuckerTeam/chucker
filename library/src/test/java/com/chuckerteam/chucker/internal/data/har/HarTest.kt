package com.chuckerteam.chucker.internal.data.har

import com.chuckerteam.chucker.internal.data.har.log.Creator
import com.chuckerteam.chucker.internal.support.HarUtils
import com.chuckerteam.chucker.util.TestTransactionFactory
import com.google.common.truth.Truth.assertThat
import org.junit.Test

public class HarTest {
    @Test
    public fun fromHttpTransactions_createsHarWithCorrectVersion() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val creator = Creator("Chucker", "3.5.2")
        val har = HarUtils.fromHttpTransactions(listOf(transaction), creator)

        assertThat(har.log.version).isEqualTo("1.2")
    }

    @Test
    public fun fromHttpTransactions_createsHarWithCorrectCreator() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val creator = Creator("Chucker", "3.5.2")
        val har = HarUtils.fromHttpTransactions(listOf(transaction), creator)

        assertThat(har.log.creator).isEqualTo(Creator("Chucker", "3.5.2"))
    }

    @Test
    public fun fromHttpTransactions_createsHarWithCorrectEntries() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val creator = Creator("Chucker", "3.5.2")
        val har = HarUtils.fromHttpTransactions(listOf(transaction), creator)

        assertThat(har.log.entries).hasSize(1)
    }
}
