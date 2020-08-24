package com.chuckerteam.chucker.internal.data.har

import com.chuckerteam.chucker.TestTransactionFactory
import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class PostDataTest {
    @Test fun responsePostData_createPostDataWithCorrectSize() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val postData = PostData.responsePostData(transaction)

        assertThat(postData?.size).isEqualTo(1000)
    }

    @Test fun responsePostData_createPostDataWithCorrectMimeType() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val postData = PostData.responsePostData(transaction)

        assertThat(postData?.mimeType).isEqualTo("application/json")
    }

    @Test fun responsePostData_createPostDataWithCorrectText() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val postData = PostData.responsePostData(transaction)

        assertThat(postData?.text).isEqualTo("""{"field": "value"}""")
    }

    @Test fun responsePostData_returnsNullWhenPayloadSizeIsNull() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        transaction.responsePayloadSize = null
        val postData = PostData.responsePostData(transaction)

        assertThat(postData).isNull()
    }

    @Test fun requestPostData_createPostDataWithCorrectSize() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val postData = PostData.requestPostData(transaction)

        assertThat(postData?.size).isEqualTo(1000)
    }

    @Test fun requestPostData_createPostDataWithCorrectMimeType() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val postData = PostData.requestPostData(transaction)

        assertThat(postData?.mimeType).isEqualTo("application/json")
    }

    @Test fun requestPostData_createPostDataWithCorrectText() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        val postData = PostData.requestPostData(transaction)

        assertThat(postData?.text).isEqualTo("")
    }

    @Test fun requestPostData_returnsNullWhenPayloadSizeIsNull() {
        val transaction = TestTransactionFactory.createTransaction("GET")
        transaction.requestPayloadSize = null
        val postData = PostData.requestPostData(transaction)

        assertThat(postData).isNull()
    }
}
