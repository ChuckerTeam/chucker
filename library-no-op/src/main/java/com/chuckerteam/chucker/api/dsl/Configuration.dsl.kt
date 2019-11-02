package com.chuckerteam.chucker.api.dsl

import com.chuckerteam.chucker.api.RetentionManager

const val DEFAULT_MAX_CONTENT_LENGTH = 250000L

@DslMarker
annotation class ChuckerConfig

@ChuckerConfig
fun configureChucker(config: ChuckerConfigBuilder.() -> Unit) = Unit

@ChuckerConfig
class ChuckerConfigBuilder {

    @ChuckerConfig
    fun http(block: HttpFeatureBuilder.() -> Unit) = Unit

    @ChuckerConfig
    fun error(block: ErrorsFeatureBuilder.() -> Unit) = Unit

    fun build() = Unit
}

@ChuckerConfig
class HttpFeatureBuilder {
    var enabled: Boolean = true
    var showNotification: Boolean = true
    var retentionPeriod: RetentionManager.Period = RetentionManager.Period.ONE_WEEK
    var maxContentLength: Long = 0
    var headersToRedact: MutableSet<String> = mutableSetOf()
}

@ChuckerConfig
class ErrorsFeatureBuilder {
    var enabled: Boolean = true
    var showNotification: Boolean = true
}
