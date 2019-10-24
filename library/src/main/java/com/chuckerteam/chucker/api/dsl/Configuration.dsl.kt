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
    /**
     * Control whether a notification is shown while HTTP activity is recorded.
     * The default is true.
     */
    var showNotification: Boolean = true

    /**
     * Set the retention period for HTTP transaction data captured by this collector.
     * The default is one week.
     */
    var retentionPeriod: RetentionManager.Period = RetentionManager.Period.ONE_WEEK

    /**
     * The maximum length for request and response content before they are truncated.
     * Warning: setting this value too high may cause unexpected results.
     */
    var maxContentLength: Long = 250000L

    /**
     * List of headers that you want to redact. They will be not be shown in
     * the ChuckerUI but will be replaced with a `**`.
     */
    var headersToRedact: MutableSet<String> = mutableSetOf()

    fun build(): HttpFeature =
        HttpFeature(enabled, showNotification, retentionPeriod, maxContentLength, headersToRedact)
}

@ChuckerConfig
class ErrorsFeatureBuilder {
    var enabled: Boolean = true
    var showNotification: Boolean = true

    fun build(): ErrorsFeature =
        ErrorsFeature(enabled, showNotification)
}
