package com.chuckerteam.chucker.api

import android.content.Context

/**
 * No-op implementation.
 */
class ChuckerCollector(
    context: Context
) {
    @Deprecated("This constructor will disappear in a following version.")
    constructor(
        context: Context,
        showNotification: Boolean,
        retentionPeriod: RetentionManager.Period
    ) : this(context)

    fun onError(obj: Any?, obj2: Any?) {
        // Empty method for the library-no-op artifact
    }
}
