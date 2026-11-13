package com.chuckerteam.chucker.internal.ui.mapremote

import android.database.sqlite.SQLiteException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.chuckerteam.chucker.internal.data.entity.MapRemoteRule
import com.chuckerteam.chucker.internal.data.entity.toMapRemoteRuleUrl
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.internal.support.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

internal class MapRemoteRuleViewModel : ViewModel() {
    private val repository = RepositoryProvider.mapRemoteRule()

    private val mutableInvalidRequestUrl = MutableLiveData<Boolean>()
    private val mutableInvalidTargetUrl = MutableLiveData<Boolean>()
    private val mutableRuleSaved = MutableLiveData<Boolean>()
    private val mutableRuleDeleted = MutableLiveData<Boolean>()
    private val mutableRuleAlreadyExists = MutableLiveData<Boolean>()
    private val mutableDatabaseFailed = MutableLiveData<Boolean>()

    val rules: LiveData<List<MapRemoteRule>> = repository.getRules()

    /** Whether the request URL of the last attempt to save was rejected. */
    val invalidRequestUrl: LiveData<Boolean> = mutableInvalidRequestUrl

    /** Whether the target URL of the last attempt to save was rejected. */
    val invalidTargetUrl: LiveData<Boolean> = mutableInvalidTargetUrl

    /** Emits once the rule is stored, so that the screen doesn't close before it takes effect. */
    val ruleSaved: LiveData<Boolean> = mutableRuleSaved

    /** Emits once the rule is removed, so that the screen doesn't close before it takes effect. */
    val ruleDeleted: LiveData<Boolean> = mutableRuleDeleted

    /** Whether the last attempt to save was rejected, as it would have replaced another rule. */
    val ruleAlreadyExists: LiveData<Boolean> = mutableRuleAlreadyExists

    /** Emits when the rules couldn't be read or changed. */
    val databaseFailed: LiveData<Boolean> = mutableDatabaseFailed

    fun getRule(
        requestUrl: String,
        requestMethod: String,
    ): LiveData<MapRemoteRule?> =
        liveData {
            emit(attempt { repository.getRule(requestUrl, requestMethod) })
        }

    /**
     * Saves the rule if both URLs are valid http(s) URLs, otherwise reports the invalid ones
     * through [invalidRequestUrl] and [invalidTargetUrl].
     *
     * @param canReplace whether the rule is being edited, so that saving is expected to replace it.
     * Otherwise, finding a rule with the same method and URL is reported through [ruleAlreadyExists].
     */
    fun saveRule(
        requestUrl: String,
        requestMethod: String,
        targetUrl: String,
        canReplace: Boolean = false,
    ) {
        val request = requestUrl.trim().toHttpUrlOrNull()
        val target = targetUrl.trim().toHttpUrlOrNull()
        mutableInvalidRequestUrl.value = request == null
        mutableInvalidTargetUrl.value = target == null
        mutableRuleAlreadyExists.value = false
        if (request == null || target == null) return

        val ruleUrl = request.toMapRemoteRuleUrl()
        viewModelScope.launch {
            val saved =
                attempt {
                    val canSave = canReplace || repository.getRule(ruleUrl, requestMethod) == null
                    if (canSave) repository.saveRule(ruleUrl, requestMethod, target.toString())
                    canSave
                }
            when (saved) {
                true -> mutableRuleSaved.value = true
                false -> mutableRuleAlreadyExists.value = true
                null -> Unit
            }
        }
    }

    fun deleteRule(rule: MapRemoteRule) {
        viewModelScope.launch {
            if (attempt { repository.deleteRule(rule.requestUrl, rule.requestMethod) } != null) {
                mutableRuleDeleted.value = true
            }
        }
    }

    /** Runs [action], returning null and reporting through [databaseFailed] if the database fails. */
    private suspend fun <T : Any> attempt(action: suspend () -> T?): T? =
        try {
            action()
        } catch (e: CancellationException) {
            // Is an IllegalStateException, and has to keep cancelling the coroutine.
            throw e
        } catch (e: SQLiteException) {
            onDatabaseFailed(e)
        } catch (e: IllegalStateException) {
            // What Room throws when it can't open the database, e.g. after a downgrade.
            onDatabaseFailed(e)
        }

    private fun onDatabaseFailed(cause: Exception): Nothing? {
        Logger.error("Failed to access Map Remote rules", cause)
        mutableDatabaseFailed.postValue(true)
        return null
    }
}
