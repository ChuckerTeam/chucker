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

    fun countEnabledFeatures(): Int {
        return features.count { it.enabled }
    }

    fun getAt(position: Int): Feature {
        return features.filter { it.enabled }[position]
    }

    fun getPositionOf(screenToShow: Int): Int {
        return features.filter { it.enabled }.indexOfFirst { it.tag == screenToShow }
    }
}
