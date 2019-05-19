package com.chuckerteam.chucker.api

import android.content.Context

/**
 * No-op implementation.
 */
class ChuckerCollector @JvmOverloads constructor(
        context: Context,
        var showNotification: Boolean = true,
        var retentionManager: Any? = null
) {

    fun onError(obj: Any?, obj2: Any?) {}

}
