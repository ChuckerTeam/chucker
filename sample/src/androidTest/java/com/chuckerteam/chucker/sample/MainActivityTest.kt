package com.chuckerteam.chucker.sample

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chuckerteam.chucker.sample.compose.testtags.ChuckerTestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appBarTitle_shouldBeVisible() {
        composeRule.onNodeWithTag(ChuckerTestTags.TOP_APP_BAR_TITLE).assertIsDisplayed()
    }

    @Test
    fun introText_shouldBeVisible_inCompactLayout() {
        composeRule.onNodeWithTag(ChuckerTestTags.INTRO_BODY_TEXT_COMPACT).assertIsDisplayed()
    }

    @Test
    fun interceptorTypeLabel_shouldBeVisible_andClickable() {
        composeRule
            .onNodeWithTag(ChuckerTestTags.CONTROLS_INTERCEPTOR_TYPE_LABEL)
            .assertIsDisplayed()
            .performClick()
    }

    @Test
    fun interceptorRadioButtons_shouldBeDisplayed() {
        val applicationLabel = "Application"
        val networkLabel = "Network"

        composeRule
            .onNodeWithTag(
                ChuckerTestTags.LABELED_RADIO_BUTTON_LABEL_TEXT + "_" + applicationLabel,
                useUnmergedTree = true,
            ).assertIsDisplayed()
        composeRule
            .onNodeWithTag(
                ChuckerTestTags.LABELED_RADIO_BUTTON_LABEL_TEXT + "_" + networkLabel,
                useUnmergedTree = true,
            ).assertIsDisplayed()
    }

    @Test
    fun interceptorRadioButtons_shouldBeSelectable() {
        val applicationLabel = "Application"
        val networkLabel = "Network"

        composeRule
            .onNodeWithTag(ChuckerTestTags.LABELED_RADIO_BUTTON_ROW + "_" + networkLabel)
            .performClick()

        composeRule
            .onNodeWithTag(ChuckerTestTags.LABELED_RADIO_BUTTON_ROW + "_" + applicationLabel)
            .performClick()
    }

    @Test
    fun doHttpButton_shouldBeClickable() {
        composeRule
            .onNodeWithTag(ChuckerTestTags.CONTROLS_DO_HTTP_BUTTON)
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun doGraphQLButton_shouldBeClickable() {
        composeRule
            .onNodeWithTag(ChuckerTestTags.CONTROLS_DO_GRAPHQL_BUTTON)
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun launchChuckerButton_shouldBeVisibleAndClickable() {
        composeRule
            .onNodeWithTag(ChuckerTestTags.CONTROLS_LAUNCH_CHUCKER_BUTTON)
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun exportButtons_shouldBeVisibleAndClickable() {
        composeRule
            .onNodeWithTag(ChuckerTestTags.CONTROLS_EXPORT_LOG_BUTTON)
            .assertIsDisplayed()
            .assertHasClickAction()

        composeRule
            .onNodeWithTag(ChuckerTestTags.CONTROLS_EXPORT_HAR_BUTTON)
            .assertIsDisplayed()
            .assertHasClickAction()
    }
}
