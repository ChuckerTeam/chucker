package com.chuckerteam.chucker.internal.data.har

import com.google.common.truth.Truth.assertThat
import okhttp3.HttpUrl
import org.junit.Test

internal class QueryStringTest {
    @Test fun fromUrl_createsCorrectListOfQueryStrings() {
        val url = HttpUrl.get("https://fake.url.com/path?query1=a&query2=b#the-fragment-part")
        val queryStringList = QueryString.fromUrl(url)
        assertThat(queryStringList).hasSize(2)
        assertThat(queryStringList[0]).isEqualTo(QueryString(name = "query1", value = "a"))
        assertThat(queryStringList[1]).isEqualTo(QueryString(name = "query2", value = "b"))
    }

    @Test fun fromUrl_createsCorrectEmptyListWhenNoQueryStringsPresent() {
        val url = HttpUrl.get("https://fake.url.com/path#the-fragment-part")
        val queryStringList = QueryString.fromUrl(url)
        assertThat(queryStringList).isEmpty()
    }
}
