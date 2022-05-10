package com.chuckerteam.chucker.internal.ui.transaction.http

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.chuckerteam.chucker.R

internal sealed class HttpProtocolResources(@DrawableRes val icon: Int, @ColorRes val color: Int) {
    class Http : HttpProtocolResources(R.drawable.chucker_ic_http, R.color.chucker_color_error)
    class Https : HttpProtocolResources(R.drawable.chucker_ic_https, R.color.chucker_color_primary)
}
