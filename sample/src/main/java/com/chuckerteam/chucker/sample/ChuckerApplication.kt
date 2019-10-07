package com.chuckerteam.chucker.sample

import android.app.Application
import com.chuckerteam.chucker.api.RetentionManager
import com.chuckerteam.chucker.api.dsl.configureChucker

class ChuckerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        configureChucker {
            http {
                enabled = false
                showNotification = true
                retentionPeriod = RetentionManager.Period.ONE_HOUR
            }
            error {
                enabled = false
            }
        }
    }
}