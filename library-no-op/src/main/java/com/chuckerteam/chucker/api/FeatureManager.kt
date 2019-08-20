package com.chuckerteam.chucker.api

import android.content.Context

class FeatureManager constructor(context: Context) {
    enum class Feature {
        HTTP_ONLY,
        HTTP_AND_ERROR
    }
}
