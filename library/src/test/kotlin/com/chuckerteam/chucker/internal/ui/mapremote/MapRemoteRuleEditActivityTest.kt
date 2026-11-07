package com.chuckerteam.chucker.internal.ui.mapremote

import android.app.Application
import android.os.Looper
import android.view.View
import android.widget.Button
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.MapRemoteRule
import com.chuckerteam.chucker.internal.data.repository.MapRemoteRuleRepository
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.google.android.material.textfield.TextInputLayout
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
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
internal class MapRemoteRuleEditActivityTest {
    private val repository = mockk<MapRemoteRuleRepository>(relaxUnitFun = true)

    private val invalidUrlMessage: String
        get() =
            ApplicationProvider
                .getApplicationContext<Application>()
                .getString(R.string.chucker_map_remote_invalid_url)

    @Before
    fun setUp() {
        every { repository.getRules() } returns MutableLiveData()
        coEvery { repository.getRule(any(), any()) } returns null
        mockkObject(RepositoryProvider)
        every { RepositoryProvider.initialize(any()) } just runs
        every { RepositoryProvider.mapRemoteRule() } returns repository
    }

    @After
    fun tearDown() = unmockkAll()

    private fun launch(isNetworkInterceptor: Boolean = false): MapRemoteRuleEditActivity {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val intent =
            MapRemoteRuleEditActivity.createIntent(application, "$REQUEST_URL?page=1", "POST", isNetworkInterceptor)
        return Robolectric
            .buildActivity(MapRemoteRuleEditActivity::class.java, intent)
            .setup()
            .get()
            .also { idle() }
    }

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    private fun MapRemoteRuleEditActivity.layout(id: Int) = findViewById<TextInputLayout>(id)

    private fun TextInputLayout.input() = editText?.text.toString()

    private fun TextInputLayout.setInput(text: String) {
        editText?.setText(text)
    }

    private fun MapRemoteRuleEditActivity.typeTargetAndSave(target: String) {
        layout(R.id.targetUrlLayout).setInput(target)
        findViewById<Button>(R.id.save).performClick()
        idle()
    }

    @Test
    fun `request url is prefilled without its query`() {
        val activity = launch()

        assertThat(activity.layout(R.id.requestUrlLayout).input()).isEqualTo(REQUEST_URL)
        assertThat(activity.layout(R.id.requestUrlLayout).isEnabled).isTrue()
    }

    @Test
    fun `call of an application interceptor can be mapped`() {
        val activity = launch()

        assertThat(activity.findViewById<View>(R.id.networkInterceptorNotice).visibility).isEqualTo(View.GONE)
        assertThat(activity.findViewById<Button>(R.id.save).isEnabled).isTrue()
    }

    @Test
    fun `call of a network interceptor shows a notice and cannot be saved`() {
        val activity = launch(isNetworkInterceptor = true)

        assertThat(activity.findViewById<View>(R.id.networkInterceptorNotice).visibility).isEqualTo(View.VISIBLE)
        assertThat(activity.findViewById<Button>(R.id.save).isEnabled).isFalse()
    }

    @Test
    fun `invalid target shows an error on its field and keeps the screen open`() {
        val activity = launch()

        activity.typeTargetAndSave("not a url")

        assertThat(activity.layout(R.id.targetUrlLayout).error.toString()).isEqualTo(invalidUrlMessage)
        assertThat(activity.layout(R.id.requestUrlLayout).error).isNull()
        assertThat(activity.isFinishing).isFalse()
        coVerify(exactly = 0) { repository.saveRule(any(), any(), any()) }
    }

    @Test
    fun `invalid request url shows an error on its field`() {
        val activity = launch()
        activity.layout(R.id.requestUrlLayout).setInput("not a url")

        activity.typeTargetAndSave(TARGET_URL)

        assertThat(activity.layout(R.id.requestUrlLayout).error.toString()).isEqualTo(invalidUrlMessage)
        assertThat(activity.layout(R.id.targetUrlLayout).error).isNull()
        assertThat(activity.isFinishing).isFalse()
    }

    @Test
    fun `valid target saves the rule and closes the screen`() {
        val activity = launch()

        activity.typeTargetAndSave(TARGET_URL)

        coVerify(exactly = 1) { repository.saveRule(REQUEST_URL, "POST", TARGET_URL) }
        assertThat(activity.isFinishing).isTrue()
    }

    @Test
    fun `fixing an invalid target clears the error`() {
        val activity = launch()
        activity.typeTargetAndSave("not a url")

        activity.typeTargetAndSave(TARGET_URL)

        assertThat(activity.layout(R.id.targetUrlLayout).error).isNull()
        assertThat(activity.isFinishing).isTrue()
    }

    @Test
    fun `existing rule is loaded and its request url cannot be changed`() {
        coEvery { repository.getRule(REQUEST_URL, "POST") } returns MapRemoteRule(REQUEST_URL, "POST", TARGET_URL, 0L)

        val activity = launch()

        assertThat(activity.layout(R.id.targetUrlLayout).input()).isEqualTo(TARGET_URL)
        assertThat(activity.layout(R.id.requestUrlLayout).isEnabled).isFalse()
    }

    @Test
    fun `request url edited to the one of another rule shows an error and saves nothing`() {
        val otherUrl = "https://example.com/other"
        coEvery { repository.getRule(otherUrl, "POST") } returns MapRemoteRule(otherUrl, "POST", TARGET_URL, 0L)
        val activity = launch()
        activity.layout(R.id.requestUrlLayout).setInput(otherUrl)

        activity.typeTargetAndSave(TARGET_URL)

        assertThat(activity.layout(R.id.requestUrlLayout).error.toString())
            .isEqualTo(activity.getString(R.string.chucker_map_remote_already_exists))
        assertThat(activity.isFinishing).isFalse()
        coVerify(exactly = 0) { repository.saveRule(any(), any(), any()) }
    }

    private companion object {
        const val REQUEST_URL = "https://example.com/users"
        const val TARGET_URL = "https://mock.example.com/users"
    }
}
