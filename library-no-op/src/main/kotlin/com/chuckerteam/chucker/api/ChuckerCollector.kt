package com.chuckerteam.chucker.api

import android.content.Context
import com.chuckerteam.chucker.api.entity.ManualHttpTransaction

/**
 * No-op implementation.
 */
@Suppress("UnusedPrivateMember", "UNUSED_PARAMETER")
public class ChuckerCollector @JvmOverloads constructor(
    context: Context,
    public var showNotification: Boolean = true,
    retentionPeriod: RetentionManager.Period = RetentionManager.Period.ONE_WEEK
) {

    /**
     * No-op implementation.
     */
    public fun saveTransaction(transaction: ManualHttpTransaction) {
        // Empty method for the library-no-op artifact
    }
}
