package com.chuckerteam.chucker.api

import android.content.Context

/**
 * No-op implementation.
 */
class ChuckerCollector @JvmOverloads constructor(
    context: Context,
    var showNotification: Boolean = true,
    var retentionPeriod: RetentionManager.Period = RetentionManager.Period.ONE_WEEK
) {

    @Deprecated(
        "This fun will be removed in 4.x release as part of Throwable functionality removal.",
        ReplaceWith(""),
        DeprecationLevel.WARNING
    )
    fun onError(obj: Any?, obj2: Any?) {
        // Empty method for the library-no-op artifact
    }
}
