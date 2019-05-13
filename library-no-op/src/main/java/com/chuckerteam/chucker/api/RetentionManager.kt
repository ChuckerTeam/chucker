package com.chuckerteam.chucker.api

import android.content.Context

/**
 * No-op implementation.
 */
class RetentionManager @JvmOverloads constructor(
        context: Context?,
        retentionPeriod: Any? = null
) {

    @Synchronized
    fun doMaintenance() {
    }

    enum class Period {
        ONE_HOUR,
        ONE_DAY,
        ONE_WEEK,
        FOREVER
    }
}
