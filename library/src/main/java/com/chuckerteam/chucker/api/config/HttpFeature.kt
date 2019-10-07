package com.chuckerteam.chucker.api.config

import com.chuckerteam.chucker.api.RetentionManager

class HttpFeature(
        val enabled: Boolean,
        val showNotification: Boolean,
        val retentionPeriod: RetentionManager.Period
) : Feature
