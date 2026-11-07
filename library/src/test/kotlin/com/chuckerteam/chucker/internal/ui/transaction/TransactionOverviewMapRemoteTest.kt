package com.chuckerteam.chucker.internal.ui.transaction

import android.app.Application
import android.content.Intent
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.createRequest
import com.chuckerteam.chucker.internal.data.repository.HttpTransactionRepository
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
internal class TransactionOverviewMapRemoteTest {
    private val repository = mockk<HttpTransactionRepository>()

    @Before
    fun setUp() {
        mockkObject(RepositoryProvider)
        every { RepositoryProvider.initialize(any()) } just runs
        every { RepositoryProvider.transaction() } returns repository
    }

    @After
    fun tearDown() = unmockkAll()

    private fun launchWith(transaction: HttpTransaction): TransactionActivity {
        every { repository.getTransaction(any()) } returns MutableLiveData(transaction)
        val application = ApplicationProvider.getApplicationContext<Application>()
        val activity =
            Robolectric
                .buildActivity(TransactionActivity::class.java, Intent(application, TransactionActivity::class.java))
                .setup()
                .get()
        shadowOf(Looper.getMainLooper()).idle()
        return activity
    }

    private fun TransactionActivity.mappedRow() = findViewById<TextView>(R.id.mappedUrl)

    @Test
    fun `row is hidden when no rule matched`() {
        val activity = launchWith(createRequest())

        assertThat(activity.mappedRow().visibility).isEqualTo(View.GONE)
    }

    @Test
    fun `row shows where the call was mapped to`() {
        val activity = launchWith(createRequest().apply { mappedUrl = "https://mock.example.com/" })

        assertThat(activity.mappedRow().visibility).isEqualTo(View.VISIBLE)
        assertThat(activity.mappedRow().text.toString()).isEqualTo("https://mock.example.com/")
    }

    @Test
    fun `row tells that a matching rule was not applied`() {
        val activity = launchWith(createRequest().apply { isMapRemoteIgnored = true })

        assertThat(activity.mappedRow().visibility).isEqualTo(View.VISIBLE)
        assertThat(activity.mappedRow().text.toString())
            .isEqualTo("Not applied: Map Remote requires addInterceptor")
    }
}
