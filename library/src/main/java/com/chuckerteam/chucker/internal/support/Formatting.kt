package com.chuckerteam.chucker.internal.support

fun Long.formatBytes(): String = FormatUtils.formatByteCount(this, true)

fun Int.formatBytes(): String = FormatUtils.formatByteCount(this.toLong(), true)
