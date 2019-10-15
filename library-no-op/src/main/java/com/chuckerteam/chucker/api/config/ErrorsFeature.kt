package com.chuckerteam.chucker.api.config

import android.content.Context
import com.chuckerteam.chucker.internal.notImplemented

class ErrorsFeature(
    override val enabled: Boolean,
    val showNotification: Boolean
) : Feature {
    override val name: Int = notImplemented()

    override val tag: Int = notImplemented()

    override fun newFragment() = notImplemented()

    override fun dismissNotification(context: Context) = notImplemented()
}
