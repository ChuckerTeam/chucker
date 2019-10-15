package com.chuckerteam.chucker.api.config

import android.content.Context

interface Feature {
    val name: Int
    val tag: Int
    val enabled: Boolean
    fun newFragment(): Nothing
    fun dismissNotification(context: Context)
}
