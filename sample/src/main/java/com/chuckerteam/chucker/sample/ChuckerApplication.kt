package com.chuckerteam.chucker.sample

import android.app.Application
import com.chuckerteam.chucker.api.RetentionManager
import com.chuckerteam.chucker.api.dsl.DEFAULT_MAX_CONTENT_LENGTH
import com.chuckerteam.chucker.api.dsl.configureChucker

class ChuckerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        configureChucker {
            http {
                enabled = true
                showNotification = true
                retentionPeriod = RetentionManager.Period.ONE_HOUR
                maxContentLength = DEFAULT_MAX_CONTENT_LENGTH
                headersToRedact = mutableSetOf("Authorization", "Auth-Token", "User-Session")
            }
            error {
                enabled = true
            }
        }
    }
}
