package com.chuckerteam.chucker.internal.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import okhttp3.HttpUrl

/**
 * Maps future calls of [requestMethod] on [requestUrl] to [targetUrl].
 *
 * [requestUrl] is stored in the form returned by [toMapRemoteRuleUrl], so a rule matches an
 * endpoint regardless of the query parameters of a particular call.
 */
@Entity(tableName = "map_remote_rules", primaryKeys = ["requestUrl", "requestMethod"])
internal data class MapRemoteRule(
    @ColumnInfo(name = "requestUrl") val requestUrl: String,
    @ColumnInfo(name = "requestMethod") val requestMethod: String,
    @ColumnInfo(name = "targetUrl") val targetUrl: String,
    @ColumnInfo(name = "updatedAt") val updatedAt: Long,
)

internal fun HttpUrl.toMapRemoteRuleUrl(): String =
    newBuilder()
        .query(null)
        .fragment(null)
        .build()
        .toString()
