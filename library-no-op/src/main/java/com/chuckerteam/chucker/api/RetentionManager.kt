package com.chuckerteam.chucker.api

import android.content.Context

/**
 * No-op implementation.
 */
class RetentionManager(
    context: Context
) {

    @Synchronized
    fun doMaintenance() {
        // Empty method for the library-no-op artifact
    }

    enum class Period {
        ONE_HOUR,
        ONE_DAY,
        ONE_WEEK,
        FOREVER
    }
}
