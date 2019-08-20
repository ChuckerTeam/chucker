package com.chuckerteam.chucker.api

import android.content.Context
import android.content.SharedPreferences

class FeatureManager constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, 0)

    companion object {
        private const val PREFS_NAME = "chucker_preferences"
        private const val KEY_FEATURE = "key_feature"
    }

    enum class Feature {
        HTTP_ONLY,
        HTTP_AND_ERROR
    }

    internal fun setFeature(feature: Feature) {
        prefs.edit().putInt(KEY_FEATURE, feature.ordinal).apply()
    }

    fun getFeature(): Feature = Feature.values()[prefs.getInt(KEY_FEATURE, Feature.HTTP_AND_ERROR.ordinal)]
}
