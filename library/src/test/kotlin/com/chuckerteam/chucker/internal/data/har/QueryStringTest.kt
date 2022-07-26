package com.chuckerteam.chucker.internal.data.har

import com.chuckerteam.chucker.internal.data.har.log.entry.request.QueryString
import com.google.common.truth.Truth.assertThat
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Test

internal class QueryStringTest {
    @Test
    fun `query string list is created correctly with url with queries`() {
        val url = "https://fake.url.com/path?query1=a&query2=b#the-fragment-part".toHttpUrl()
        val queryStringList = QueryString.fromUrl(url)
        assertThat(queryStringList).hasSize(2)
        assertThat(queryStringList[0]).isEqualTo(QueryString(name = "query1", value = "a"))
        assertThat(queryStringList[1]).isEqualTo(QueryString(name = "query2", value = "b"))
    }

    @Test
    fun `query string list is created correctly with url with no query`() {
        val url = "https://fake.url.com/path#the-fragment-part".toHttpUrl()
        val queryStringList = QueryString.fromUrl(url)
        assertThat(queryStringList).isEmpty()
    }
}
