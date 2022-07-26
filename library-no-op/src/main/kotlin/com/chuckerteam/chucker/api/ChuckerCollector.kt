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
)
