package com.chuckerteam.chucker.internal.data.har

import com.chuckerteam.chucker.internal.data.har.log.entry.request.PostData
import com.chuckerteam.chucker.internal.data.har.log.entry.response.Content
import com.chuckerteam.chucker.util.TestTransactionFactory
import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class PostDataTest {
    @Test
    fun responseContent_createContentWithCorrectSize() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val postData = Content(transaction)

        assertThat(postData.size).isEqualTo(1000)
    }

    @Test
    fun responseContent_createContentWithCorrectMimeType() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val postData = Content(transaction)

        assertThat(postData?.mimeType).isEqualTo("application/json")
    }

    @Test
    fun responseContent_createContentWithCorrectText() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val postData = Content(transaction)

        assertThat(postData?.text).isEqualTo("""{"field": "value"}""")
    }

    @Test
    fun requestPostData_createPostDataWithCorrectMimeType() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val postData = PostData(transaction)

        assertThat(postData.mimeType).isEqualTo("application/json")
    }

    @Test
    fun requestPostData_createPostDataWithCorrectText() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val postData = PostData(transaction)

        assertThat(postData.text).isNull()
    }
}
