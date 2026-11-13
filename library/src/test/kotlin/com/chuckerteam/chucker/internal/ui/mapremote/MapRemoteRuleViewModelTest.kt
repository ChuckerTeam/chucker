package com.chuckerteam.chucker.internal.ui.mapremote

import android.database.sqlite.SQLiteException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.chuckerteam.chucker.api.Chucker
import com.chuckerteam.chucker.internal.data.entity.MapRemoteRule
import com.chuckerteam.chucker.internal.data.repository.MapRemoteRuleRepository
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.internal.support.Logger
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
internal class MapRemoteRuleViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<MapRemoteRuleRepository>(relaxUnitFun = true)

    private val defaultLogger = Chucker.logger

    private lateinit var viewModel: MapRemoteRuleViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        Chucker.logger = mockk<Logger>(relaxUnitFun = true)
        every { repository.getRules() } returns MutableLiveData()
        coEvery { repository.getRule(any(), any()) } returns null
        mockkObject(RepositoryProvider)
        every { RepositoryProvider.mapRemoteRule() } returns repository
        viewModel = MapRemoteRuleViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        Chucker.logger = defaultLogger
        unmockkAll()
    }

    private fun save(
        requestUrl: String = REQUEST_URL,
        targetUrl: String = TARGET_URL,
    ) {
        viewModel.saveRule(requestUrl, "POST", targetUrl)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    private fun verifyNothingSaved() {
        coVerify(exactly = 0) { repository.saveRule(any(), any(), any()) }
        assertThat(viewModel.ruleSaved.value).isNull()
    }

    @Test
    fun `valid urls are saved and reported as saved`() {
        save()

        coVerify(exactly = 1) { repository.saveRule(REQUEST_URL, "POST", TARGET_URL) }
        assertThat(viewModel.ruleSaved.value).isTrue()
        assertThat(viewModel.invalidRequestUrl.value).isFalse()
        assertThat(viewModel.invalidTargetUrl.value).isFalse()
    }

    @Test
    fun `surrounding whitespace is ignored`() {
        save(requestUrl = "  $REQUEST_URL ", targetUrl = "\n$TARGET_URL  ")

        coVerify(exactly = 1) { repository.saveRule(REQUEST_URL, "POST", TARGET_URL) }
    }

    @Test
    fun `query and fragment are removed from the request url only`() {
        save(
            requestUrl = "$REQUEST_URL?page=1#top",
            targetUrl = "$TARGET_URL?fixture=empty",
        )

        coVerify(exactly = 1) { repository.saveRule(REQUEST_URL, "POST", "$TARGET_URL?fixture=empty") }
    }

    @Test
    fun `http target is accepted`() {
        save(targetUrl = "http://10.0.2.2:8080/users")

        coVerify(exactly = 1) { repository.saveRule(REQUEST_URL, "POST", "http://10.0.2.2:8080/users") }
    }

    @Test
    fun `invalid request url is reported and nothing is saved`() {
        save(requestUrl = "not a url")

        assertThat(viewModel.invalidRequestUrl.value).isTrue()
        assertThat(viewModel.invalidTargetUrl.value).isFalse()
        verifyNothingSaved()
    }

    @Test
    fun `invalid target url is reported and nothing is saved`() {
        save(targetUrl = "not a url")

        assertThat(viewModel.invalidRequestUrl.value).isFalse()
        assertThat(viewModel.invalidTargetUrl.value).isTrue()
        verifyNothingSaved()
    }

    @Test
    fun `both invalid urls are reported at once`() {
        save(requestUrl = "", targetUrl = "   ")

        assertThat(viewModel.invalidRequestUrl.value).isTrue()
        assertThat(viewModel.invalidTargetUrl.value).isTrue()
        verifyNothingSaved()
    }

    @Test
    fun `target url with a scheme other than http is rejected`() {
        save(targetUrl = "ftp://mock.example.com/users")

        assertThat(viewModel.invalidTargetUrl.value).isTrue()
        verifyNothingSaved()
    }

    @Test
    fun `fixing an invalid url clears the error and saves`() {
        save(targetUrl = "not a url")

        save()

        assertThat(viewModel.invalidTargetUrl.value).isFalse()
        assertThat(viewModel.ruleSaved.value).isTrue()
    }

    @Test
    fun `deleting a rule is reported once it is removed`() {
        viewModel.deleteRule(MapRemoteRule(REQUEST_URL, "POST", TARGET_URL, updatedAt = 0L))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.deleteRule(REQUEST_URL, "POST") }
        assertThat(viewModel.ruleDeleted.value).isTrue()
    }

    @Test
    fun `new rule does not replace another rule`() {
        coEvery { repository.getRule(REQUEST_URL, "POST") } returns EXISTING_RULE

        save()

        assertThat(viewModel.ruleAlreadyExists.value).isTrue()
        verifyNothingSaved()
    }

    @Test
    fun `edited rule replaces itself`() {
        coEvery { repository.getRule(REQUEST_URL, "POST") } returns EXISTING_RULE

        viewModel.saveRule(REQUEST_URL, "POST", TARGET_URL, canReplace = true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.saveRule(REQUEST_URL, "POST", TARGET_URL) }
        assertThat(viewModel.ruleAlreadyExists.value).isFalse()
        assertThat(viewModel.ruleSaved.value).isTrue()
    }

    @Test
    fun `failing to save is reported instead of crashing`() {
        coEvery { repository.saveRule(any(), any(), any()) } throws SQLiteException("disk full")

        save()

        assertThat(viewModel.databaseFailed.value).isTrue()
        assertThat(viewModel.ruleSaved.value).isNull()
    }

    @Test
    fun `failing to open the database while saving is reported instead of crashing`() {
        coEvery { repository.getRule(any(), any()) } throws IllegalStateException("A migration was required")

        save()

        assertThat(viewModel.databaseFailed.value).isTrue()
        verifyNothingSaved()
    }

    @Test
    fun `failing to delete is reported instead of crashing`() {
        coEvery { repository.deleteRule(any(), any()) } throws SQLiteException("disk full")

        viewModel.deleteRule(EXISTING_RULE)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.databaseFailed.value).isTrue()
        assertThat(viewModel.ruleDeleted.value).isNull()
    }

    @Test
    fun `cancellation is not reported as a database failure`() {
        coEvery { repository.saveRule(any(), any(), any()) } throws CancellationException("cancelled")

        save()

        assertThat(viewModel.databaseFailed.value).isNull()
        assertThat(viewModel.ruleSaved.value).isNull()
    }

    private companion object {
        const val REQUEST_URL = "https://example.com/users"
        const val TARGET_URL = "https://mock.example.com/users"
        val EXISTING_RULE = MapRemoteRule(REQUEST_URL, "POST", "https://old.example.com/users", updatedAt = 0L)
    }
}
