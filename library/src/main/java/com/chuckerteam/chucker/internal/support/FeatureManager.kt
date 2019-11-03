package com.chuckerteam.chucker.internal.support

import com.chuckerteam.chucker.api.config.ErrorsFeature
import com.chuckerteam.chucker.api.config.HttpFeature
import com.chuckerteam.chucker.api.config.TabFeature

internal object FeatureManager {

    private val features: MutableList<TabFeature> = mutableListOf(
        HttpFeature.default(),
        ErrorsFeature.default()
    )

    fun configure(tabFeature: TabFeature) {
        features.removeAll { it.javaClass == tabFeature.javaClass }
        features.add(tabFeature)
    }

    inline fun <reified T : TabFeature> find(): T {
        return features.firstOrNull { it is T } as T
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
