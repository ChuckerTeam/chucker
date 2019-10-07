package com.chuckerteam.chucker.api.dsl

import com.chuckerteam.chucker.api.RetentionManager
import com.chuckerteam.chucker.api.config.ErrorsFeature
import com.chuckerteam.chucker.api.config.HttpFeature
import com.chuckerteam.chucker.internal.support.FeatureManager

@DslMarker
annotation class ChuckerConfig

@ChuckerConfig
fun configureChucker(config: ChuckerConfigBuilder.() -> Unit) {
    ChuckerConfigBuilder().apply(config).build()
}

@ChuckerConfig
class ChuckerConfigBuilder {

    private var http: HttpFeature = HttpFeatureBuilder().build()
    private var errors: ErrorsFeature = ErrorsFeatureBuilder().build()

    @ChuckerConfig
    fun http(block: HttpFeatureBuilder.() -> Unit) {
        http = HttpFeatureBuilder().apply(block).build()
    }

    @ChuckerConfig
    fun error(block: ErrorsFeatureBuilder.() -> Unit) {
        errors = ErrorsFeatureBuilder().apply(block).build()
    }

    fun build() {
        FeatureManager.configure(http)
        FeatureManager.configure(errors)
    }
}

@ChuckerConfig
class HttpFeatureBuilder {
    var enabled: Boolean = true
    var showNotification: Boolean = true
    var retentionPeriod: RetentionManager.Period = RetentionManager.Period.ONE_WEEK

    fun build(): HttpFeature =
            HttpFeature(enabled, showNotification, retentionPeriod)
}

@ChuckerConfig
class ErrorsFeatureBuilder {
    var enabled: Boolean = true
    var showNotification: Boolean = true

    fun build(): ErrorsFeature =
            ErrorsFeature(enabled, showNotification)
}