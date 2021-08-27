package com.chuckerteam.chucker.internal.support

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.chuckerteam.chucker.api.RetentionManager
import java.lang.StringBuilder

public class PrefUtils private constructor(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILE, 0)

    public fun getRedactedHeaders(): List<String> {
        val csv = prefs.getString(KEY_REDACTED_HEADERS, "")
        return csv.toStringList()
    }

    public fun setRedactedHeaders(input: Set<String>) {
        prefs.edit {
            remove(KEY_REDACTED_HEADERS)
            putString(KEY_REDACTED_HEADERS, input.toCSV())
            apply()
        }
    }

    public fun getRetentionPeriod(): RetentionManager.Period = when (prefs.getString(KEY_RETENTION_PERIOD, "ONE_WEEK")) {
        "ONE_HOUR" -> RetentionManager.Period.ONE_HOUR
        "ONE_DAY" -> RetentionManager.Period.ONE_DAY
        "FOREVER" -> RetentionManager.Period.FOREVER
        else -> RetentionManager.Period.ONE_WEEK
    }

    public fun setRetentionPeriod(retentionPeriod: RetentionManager.Period) {
        prefs.edit {
            remove(KEY_RETENTION_PERIOD)
            putString(KEY_RETENTION_PERIOD, retentionPeriod.toString())
            apply()
        }
    }

    public companion object {
        @Volatile private var INSTANCE: PrefUtils? = null

        private const val PREFS_FILE: String = "chucker_prefs"
        private const val KEY_RETENTION_PERIOD: String = "chucker_retention_period"
        private const val KEY_REDACTED_HEADERS: String = "chucker_saved_redacted_headers"

        public fun getInstance(context: Context): PrefUtils =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: PrefUtils(context).also { INSTANCE = it }
            }
    }
}

private fun String?.toStringList(): List<String> {
    return this?.split(",") ?: emptyList()
}

private fun Set<String>.toCSV(): String {
    val builder = StringBuilder()
    this.forEach { value ->
        builder.append(value)
        builder.append(",")
    }
    builder.delete(builder.length - 1, builder.length)
    return builder.toString()
}
