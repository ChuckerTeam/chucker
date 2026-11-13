package com.chuckerteam.chucker.internal.data.repository

import android.database.sqlite.SQLiteException
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.chuckerteam.chucker.internal.data.entity.MapRemoteRule
import com.chuckerteam.chucker.internal.data.entity.toMapRemoteRuleUrl
import com.chuckerteam.chucker.internal.data.room.MapRemoteRuleDatabase
import com.chuckerteam.chucker.internal.support.Logger
import kotlinx.coroutines.flow.catch
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

internal class MapRemoteRuleDatabaseRepository(
    database: MapRemoteRuleDatabase,
) : MapRemoteRuleRepository {
    private val dao = database.mapRemoteRuleDao()
    private val lock = Any()

    /** In-memory copy of the rules, so that calls don't have to query the database. Null until loaded. */
    @Volatile
    private var targets: Map<RuleKey, HttpUrl>? = null

    override fun findTarget(
        url: HttpUrl,
        method: String,
    ): HttpUrl? {
        val targets = loadTargets()
        if (targets.isEmpty()) return null
        val target = targets[RuleKey(url.toMapRemoteRuleUrl(), method.uppercase())] ?: return null
        // A rule matches an endpoint, so the parameters of this call still have to reach it
        // unless the rule asks for specific ones.
        return if (target.encodedQuery == null) {
            target.newBuilder().encodedQuery(url.encodedQuery).build()
        } else {
            target
        }
    }

    private fun loadTargets(): Map<RuleKey, HttpUrl> =
        targets ?: synchronized(lock) {
            targets ?: try {
                dao
                    .getAll()
                    .mapNotNull { rule ->
                        rule.targetUrl.toHttpUrlOrNull()?.let { RuleKey(rule.requestUrl, rule.requestMethod) to it }
                    }.toMap()
                    .also { targets = it }
            } catch (e: SQLiteException) {
                onReadFailed(e)
            } catch (e: IllegalStateException) {
                // What Room throws when it can't open the database, e.g. after a downgrade.
                onReadFailed(e)
            }
        }

    /** Failing to read the rules must not break the calls of the host app. */
    private fun onReadFailed(cause: Exception): Map<RuleKey, HttpUrl> {
        Logger.error("Failed to read Map Remote rules", cause)
        return emptyMap()
    }

    private fun invalidateTargets() {
        synchronized(lock) { targets = null }
    }

    override fun getRules(): LiveData<List<MapRemoteRule>> =
        dao
            .observeAll()
            .catch { cause ->
                Logger.error("Failed to read Map Remote rules", cause)
                emit(emptyList())
            }.asLiveData()

    override suspend fun getRule(
        requestUrl: String,
        requestMethod: String,
    ): MapRemoteRule? = dao.get(requestUrl, requestMethod.uppercase())

    override suspend fun saveRule(
        requestUrl: String,
        requestMethod: String,
        targetUrl: String,
    ) {
        dao.save(
            MapRemoteRule(
                requestUrl = requestUrl,
                requestMethod = requestMethod.uppercase(),
                targetUrl = targetUrl,
                updatedAt = System.currentTimeMillis(),
            ),
        )
        invalidateTargets()
    }

    override suspend fun deleteRule(
        requestUrl: String,
        requestMethod: String,
    ) {
        dao.delete(requestUrl, requestMethod.uppercase())
        invalidateTargets()
    }

    private data class RuleKey(
        val requestUrl: String,
        val requestMethod: String,
    )
}
