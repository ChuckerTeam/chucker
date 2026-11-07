package com.chuckerteam.chucker.internal.data.repository

import androidx.lifecycle.LiveData
import com.chuckerteam.chucker.internal.data.entity.MapRemoteRule
import okhttp3.HttpUrl

/**
 * Finds where a call should be sent instead of its original [HttpUrl].
 */
internal fun interface MapRemoteTargetResolver {
    fun findTarget(
        url: HttpUrl,
        method: String,
    ): HttpUrl?
}

internal interface MapRemoteRuleRepository : MapRemoteTargetResolver {
    fun getRules(): LiveData<List<MapRemoteRule>>

    suspend fun getRule(
        requestUrl: String,
        requestMethod: String,
    ): MapRemoteRule?

    suspend fun saveRule(
        requestUrl: String,
        requestMethod: String,
        targetUrl: String,
    )

    suspend fun deleteRule(
        requestUrl: String,
        requestMethod: String,
    )
}
