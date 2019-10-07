package com.chuckerteam.chucker.api

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.chuckerteam.chucker.api.Chucker.LOG_TAG
import com.chuckerteam.chucker.api.config.HttpFeature
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.internal.support.FeatureManager
import java.util.concurrent.TimeUnit

/**
 * Class responsible of holding the logic for the retention of your HTTP transactions
 * and your throwable. You can customize how long data should be stored here.
 * @param context An Android Context
 */
@Suppress("MagicNumber")
class RetentionManager(
    context: Context
) {

    private val httpFeature: HttpFeature = FeatureManager.find()

    // The actual retention period in milliseconds (default to ONE_WEEK)
    private val period: Long = toMillis(httpFeature.retentionPeriod)
    // How often the cleanup should happen
    private val cleanupFrequency: Long
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, 0)

    init {
        cleanupFrequency = if (httpFeature.retentionPeriod == Period.ONE_HOUR)
            TimeUnit.MINUTES.toMillis(30)
        else
            TimeUnit.HOURS.toMillis(2)
    }

    /**
     * Call this function to check and eventually trigger a cleanup.
     * Please note that this method is not forcing a cleanup.
     */
    @Synchronized
    internal fun doMaintenance() {
        if (period > 0) {
            val now = System.currentTimeMillis()
            if (isCleanupDue(now)) {
                Log.i(LOG_TAG, "Performing data retention maintenance...")
                deleteSince(getThreshold(now))
                updateLastCleanup(now)
            }
        }
    }

    private fun getLastCleanup(fallback: Long): Long {
        if (lastCleanup == 0L) {
            lastCleanup = prefs.getLong(KEY_LAST_CLEANUP, fallback)
        }
        return lastCleanup
    }

    private fun updateLastCleanup(time: Long) {
        lastCleanup = time
        prefs.edit().putLong(KEY_LAST_CLEANUP, time).apply()
    }

    private fun deleteSince(threshold: Long) {
        RepositoryProvider.transaction().deleteOldTransactions(threshold)
        RepositoryProvider.throwable().deleteOldThrowables(threshold)
    }

    private fun isCleanupDue(now: Long) = now - getLastCleanup(now) > cleanupFrequency

    private fun getThreshold(now: Long) = if (period == 0L) now else now - period

    private fun toMillis(period: Period): Long {
        return when (period) {
            Period.ONE_HOUR -> TimeUnit.HOURS.toMillis(1)
            Period.ONE_DAY -> TimeUnit.DAYS.toMillis(1)
            Period.ONE_WEEK -> TimeUnit.DAYS.toMillis(7)
            Period.FOREVER -> 0
        }
    }

    enum class Period {
        /** Retain data for the last hour. */
        ONE_HOUR,
        /** Retain data for the last day. */
        ONE_DAY,
        /** Retain data for the last week. */
        ONE_WEEK,
        /** Retain data forever. */
        FOREVER
    }

    companion object {
        private const val PREFS_NAME = "chucker_preferences"
        private const val KEY_LAST_CLEANUP = "last_cleanup"
        private var lastCleanup: Long = 0
    }
}
