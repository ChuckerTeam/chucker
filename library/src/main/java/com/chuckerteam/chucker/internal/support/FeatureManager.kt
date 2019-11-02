package com.chuckerteam.chucker.internal.support

import com.chuckerteam.chucker.api.config.TabFeature

internal object FeatureManager {

    private val features = mutableListOf<TabFeature>()

    fun configure(tabFeature: TabFeature) {
        features.add(tabFeature)
    }

    inline fun <reified T : TabFeature> find(): T {
        return features.first { it is T } as T
    }

    fun countEnabledFeatures(): Int {
        return features.count { it.enabled }
    }

    fun getAt(position: Int): TabFeature {
        return features.filter { it.enabled }[position]
    }

    fun getPositionOf(screenToShow: Int): Int {
        return features.filter { it.enabled }.indexOfFirst { it.id == screenToShow }
    }
}
