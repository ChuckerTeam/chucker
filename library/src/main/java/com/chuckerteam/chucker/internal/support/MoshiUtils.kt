package com.chuckerteam.chucker.internal.support

import com.chuckerteam.chucker.internal.data.entity.HttpHeader
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

internal fun Moshi.getHttpHeaderListJsonAdapter(): JsonAdapter<List<HttpHeader>> {
    val type = Types.newParameterizedType(List::class.java, HttpHeader::class.java)
    val jsonAdapter = this.adapter<List<HttpHeader>>(type)
    return jsonAdapter.addConvenienceMethods()
}

internal fun <T> JsonAdapter<T>.addConvenienceMethods(): JsonAdapter<T> {
    return this
        .serializeNulls()
        .indent("  ")
}
