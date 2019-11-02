package com.chuckerteam.chucker.api.config

import android.content.Context
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

interface TabFeature {
    @get:StringRes
    val name: Int
    val id: Int
    val enabled: Boolean
    fun newFragment(): Fragment
    fun dismissNotification(context: Context)
}
