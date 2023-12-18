package com.chuckerteam.chucker.internal.extension

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity

internal fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}
