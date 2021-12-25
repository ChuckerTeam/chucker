package com.chuckerteam.chucker.internal.data.har

import com.chuckerteam.chucker.util.HarTestUtils
import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class ContentTest {
    @Test
    fun `content is created correctly with size`() {
        val content = HarTestUtils.createContent("GET")

        assertThat(content?.size).isEqualTo(1000)
    }

    @Test
    fun `content is created correctly with mime type`() {
        val content = HarTestUtils.createContent("GET")

        assertThat(content?.mimeType).isEqualTo("application/json")
    }

    @Test
    fun `content is created correctly with fields and values`() {
        val content = HarTestUtils.createContent("GET")

        assertThat(content?.text).isEqualTo("""{"field": "value"}""")
    }
}
