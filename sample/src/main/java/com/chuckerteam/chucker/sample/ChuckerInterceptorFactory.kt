package com.chuckerteam.chucker.sample

import android.annotation.SuppressLint
import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager

class ChuckerInterceptorFactory {
    companion object {
        val chuckerInterceptor: ChuckerInterceptor get() = instance
        val collector: ChuckerCollector get() = collectorInstance

        @SuppressLint("StaticFieldLeak")
        private lateinit var instance: ChuckerInterceptor
        private lateinit var collectorInstance: ChuckerCollector

        fun init(context: Context) {
            collectorInstance = ChuckerCollector(
                context = context,
                showNotification = true,
                retentionPeriod = RetentionManager.Period.ONE_HOUR
            )
            instance = ChuckerInterceptor(
                context = context,
                collector = collectorInstance,
                maxContentLength = 250000L
            )
        }
    }
}
