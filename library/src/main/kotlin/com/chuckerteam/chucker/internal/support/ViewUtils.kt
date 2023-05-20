package com.chuckerteam.chucker.internal.support

import android.view.View

internal fun View.visible() {
    if (this.visibility != View.VISIBLE) {
        this.visibility = View.VISIBLE
    }
}

internal fun View.gone() {
    if (this.visibility != View.GONE) {
        this.visibility = View.GONE
    }
}
