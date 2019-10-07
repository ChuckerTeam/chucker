package com.chuckerteam.chucker.internal.support

import com.chuckerteam.chucker.api.config.Feature

internal object FeatureManager {

    private val features = mutableListOf<Feature>()

    fun configure(feature: Feature) {
        features.add(feature)
    }

    inline fun <reified T : Feature> find(): T {
        return features.first { it is T } as T
    }
}