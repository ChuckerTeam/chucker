package com.chuckerteam.chucker.internal.ui.transaction

import android.os.Looper
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commitNow
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.job
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowPausedLooper
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

@RunWith(RobolectricTestRunner::class)
internal class TransactionPayloadFragmentTest {
    private val bodyRequested = CountDownLatch(1)
    private val releaseBody = CountDownLatch(1)
    private val uncaughtExceptions = CopyOnWriteArrayList<Throwable>()
    private val defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()

    @Before
    fun setUp() {
        val transaction =
            mockk<HttpTransaction>(relaxed = true) {
                every { responseImageBitmap } returns null
                every { isResponseBodyEncoded } returns false
                every { getSpannedResponseBody(any()) } answers {
                    bodyRequested.countDown()
                    releaseBody.await()
                    "body"
                }
            }
        mockkObject(RepositoryProvider)
        every { RepositoryProvider.transaction() } returns
            mockk { every { getTransaction(any()) } returns MutableLiveData(transaction) }
        Thread.setDefaultUncaughtExceptionHandler { _, throwable -> uncaughtExceptions += throwable }
    }

    @After
    fun tearDown() {
        releaseBody.countDown()
        Thread.setDefaultUncaughtExceptionHandler(defaultUncaughtExceptionHandler)
        unmockkAll()
    }

    @Test
    fun `payload processing does not crash when the fragment is detached before it finishes`() {
        val activity =
            Robolectric
                .buildActivity(FragmentActivity::class.java)
                .apply { get().setTheme(R.style.Chucker_Theme) }
                .setup()
                .get()
        val fragment = TransactionPayloadFragment.newInstance(PayloadType.RESPONSE)
        activity.supportFragmentManager.commitNow { add(android.R.id.content, fragment) }
        assertThat(bodyRequested.await(5, TimeUnit.SECONDS)).isTrue()
        val payloadJobs =
            fragment.lifecycleScope.coroutineContext.job.children
                .toList()
        assertThat(payloadJobs).isNotEmpty()

        activity.supportFragmentManager.commitNow { remove(fragment) }
        releaseBody.countDown()
        val mainLooper = Shadow.extract<ShadowPausedLooper>(Looper.getMainLooper())
        val deadline = TimeSource.Monotonic.markNow() + 5.seconds
        while (payloadJobs.any { !it.isCompleted } && deadline.hasNotPassedNow()) {
            mainLooper.poll(100)
            mainLooper.idle()
        }

        assertThat(payloadJobs.all { it.isCompleted }).isTrue()
        assertThat(uncaughtExceptions).isEmpty()
    }
}
