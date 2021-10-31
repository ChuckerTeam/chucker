package com.chuckerteam.chucker.internal.data.har

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.har.log.Creator
import com.chuckerteam.chucker.util.HarTestUtils.createSingleTransactionHar
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
public class HarTest {
    private lateinit var context: Context

    @Before
    public fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    public fun `har is created correctly with har version`() {
        val har = context.createSingleTransactionHar("GET")

        assertThat(har.log.version).isEqualTo("1.2")
    }

    @Test
    public fun `har is created correctly with creator`() {
        val har = context.createSingleTransactionHar("GET")
        val creator = Creator(
            context.getString(R.string.chucker_name),
            context.getString(R.string.chucker_version),
        )

        assertThat(har.log.creator).isEqualTo(creator)
    }

    @Test
    public fun `har is created correctly with entries`() {
        val har = context.createSingleTransactionHar("GET")

        assertThat(har.log.entries).hasSize(1)
    }
}
