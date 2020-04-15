package com.chuckerteam.chucker.internal.support

import android.content.Context
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.TestTransactionFactory
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class FormatUtilsSharedTextTest {

    private val contextMock = mockk<Context> {
        every { getString(R.string.chucker_url) } returns "URL"
        every { getString(R.string.chucker_method) } returns "Method"
        every { getString(R.string.chucker_protocol) } returns "Protocol"
        every { getString(R.string.chucker_status) } returns "Status"
        every { getString(R.string.chucker_response) } returns "Response"
        every { getString(R.string.chucker_ssl) } returns "SSL"
        every { getString(R.string.chucker_yes) } returns "Yes"
        every { getString(R.string.chucker_no) } returns "No"
        every { getString(R.string.chucker_request_time) } returns "Request time"
        every { getString(R.string.chucker_response_time) } returns "Response time"
        every { getString(R.string.chucker_duration) } returns "Duration"
        every { getString(R.string.chucker_request_size) } returns "Request size"
        every { getString(R.string.chucker_response_size) } returns "Response size"
        every { getString(R.string.chucker_total_size) } returns "Total size"
        every { getString(R.string.chucker_request) } returns "Request"
        every { getString(R.string.chucker_body_omitted) } returns "(encoded or binary body omitted)"
        every { getString(R.string.chucker_body_empty) } returns "(body is empty)"
    }

    @Test
    fun getShareTextForGetTransaction() {
        val shareText = getShareText("GET")
        assertThat(shareText).isEqualTo(TestTransactionFactory.expectedGetHttpTransaction)
    }

    @Test
    fun getShareTextForPostTransaction() {
        val shareText = getShareText("POST")
        assertThat(shareText).isEqualTo(TestTransactionFactory.expectedHttpPostTransaction)
    }

    private fun getShareText(method: String): String {
        return FormatUtils.getShareText(
            contextMock,
            TestTransactionFactory.createTransaction(method),
            false
        )
    }
}
