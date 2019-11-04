@file:JvmName("ChuckerJavaConfig")

package com.chuckerteam.chucker.api.config

import com.chuckerteam.chucker.internal.support.FeatureManager

fun configure(features: List<TabFeature>) {
    features.forEach(FeatureManager::configure)
}
