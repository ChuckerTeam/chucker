package com.chuckerteam.chucker.api

import android.content.Context

/**
 * No-op implementation.
 */
@Suppress("UnusedPrivateMember")
public class ChuckerCollector @JvmOverloads constructor(
    context: Context,
    public var showNotification: Boolean = true,
    retentionPeriod: RetentionManager.Period = RetentionManager.Period.ONE_WEEK
) {

    @Deprecated(
        "This fun will be removed in 4.x release as part of Throwable functionality removal.",
        ReplaceWith(""),
        DeprecationLevel.WARNING
    )
    public fun onError(obj: Any?, obj2: Any?) {
        // Empty method for the library-no-op artifact
    }
}
