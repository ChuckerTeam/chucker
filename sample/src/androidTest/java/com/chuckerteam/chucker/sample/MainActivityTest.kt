package com.chuckerteam.chucker.sample

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @Before
    fun setup() {
        ActivityScenario.launch(MainActivity::class.java)
    }

    @Test
    fun testUIComponentsAreDisplayed() {
        onView(withText(ChuckerUITestLabels.INTERCEPTOR_TYPE_LABEL)).check(matches(isDisplayed()))
        onView(withText(ChuckerUITestLabels.APPLICATION_RADIO_BUTTON)).check(matches(isDisplayed()))
        onView(withText(ChuckerUITestLabels.APPLICATION_RADIO_BUTTON)).perform(click())
        onView(withText(ChuckerUITestLabels.NETWORK_RADIO_BUTTON)).check(matches(isDisplayed()))
        onView(withText(ChuckerUITestLabels.NETWORK_RADIO_BUTTON)).perform(click())
        onView(withText(ChuckerUITestLabels.DO_HTTP_ACTIVITY_BUTTON)).check(matches(isDisplayed()))
        onView(withText(ChuckerUITestLabels.DO_GRAPHQL_ACTIVITY_BUTTON)).check(matches(isDisplayed()))
        onView(withText(ChuckerUITestLabels.LAUNCH_CHUCKER_BUTTON)).check(matches(isDisplayed()))
        onView(withText(ChuckerUITestLabels.EXPORT_LOG_FILE_BUTTON)).check(matches(isDisplayed()))
        onView(withText(ChuckerUITestLabels.EXPORT_HAR_FILE_BUTTON)).check(matches(isDisplayed()))
    }

    @Test
    fun testButtonInteractions() {
        onView(withText(ChuckerUITestLabels.DO_HTTP_ACTIVITY_BUTTON)).perform(click())
    }

    @Test
    fun testRadioButtonSelection() {
        onView(withText(ChuckerUITestLabels.APPLICATION_RADIO_BUTTON))
            .perform(click())
            .check(matches(isChecked()))

        onView(withText(ChuckerUITestLabels.NETWORK_RADIO_BUTTON))
            .perform(click())
            .check(matches(isChecked()))
    }

    @Test
    fun testHttpActivityButtonClick() {
        onView(withText(ChuckerUITestLabels.DO_HTTP_ACTIVITY_BUTTON)).perform(click())
    }

    @Test
    fun testGraphQLActivityButtonClick() {
        onView(withText(ChuckerUITestLabels.DO_GRAPHQL_ACTIVITY_BUTTON)).perform(click())
    }

    @Test
    fun testLaunchChuckerDirectlyButtonClick() {
        onView(withText(ChuckerUITestLabels.LAUNCH_CHUCKER_BUTTON)).perform(click())
    }

    @Test
    fun testExportButtonsVisibility() {
        onView(withText(ChuckerUITestLabels.EXPORT_LOG_FILE_BUTTON)).check(matches(isDisplayed()))
        onView(withText(ChuckerUITestLabels.EXPORT_HAR_FILE_BUTTON)).check(matches(isDisplayed()))
    }

    @Test
    fun testAllButtonClicks() {
        val buttons =
            listOf(
                ChuckerUITestLabels.DO_HTTP_ACTIVITY_BUTTON,
                ChuckerUITestLabels.DO_GRAPHQL_ACTIVITY_BUTTON,
                ChuckerUITestLabels.LAUNCH_CHUCKER_BUTTON,
            )

        buttons.forEach { buttonText ->
            onView(withText(buttonText)).perform(click())
        }
    }

    @Test
    fun testOnlyOneRadioButtonCheckedAtATime() {
        onView(withText(ChuckerUITestLabels.APPLICATION_RADIO_BUTTON)).perform(click())
        onView(withText(ChuckerUITestLabels.APPLICATION_RADIO_BUTTON)).check(matches(isDisplayed()))
        onView(withText(ChuckerUITestLabels.NETWORK_RADIO_BUTTON)).check(matches(not(isChecked())))
        onView(withText(ChuckerUITestLabels.NETWORK_RADIO_BUTTON)).perform(click())
        onView(withText(ChuckerUITestLabels.NETWORK_RADIO_BUTTON)).check(matches(isChecked()))
        onView(withText(ChuckerUITestLabels.APPLICATION_RADIO_BUTTON)).check(matches(not(isChecked())))
    }

    @Test
    fun testRadioButtonSelectionVisualState() {
        onView(withText(ChuckerUITestLabels.APPLICATION_RADIO_BUTTON))
            .check(matches(isDisplayed()))
            .perform(click())
        onView(withText(ChuckerUITestLabels.NETWORK_RADIO_BUTTON))
            .check(matches(isDisplayed()))
            .perform(click())
    }

    @Test
    fun testExportButtonsAreVisibleWithoutScrolling() {
        onView(withText(ChuckerUITestLabels.EXPORT_LOG_FILE_BUTTON)).check(matches(isDisplayed()))
        onView(withText(ChuckerUITestLabels.EXPORT_HAR_FILE_BUTTON)).check(matches(isDisplayed()))
    }
}
