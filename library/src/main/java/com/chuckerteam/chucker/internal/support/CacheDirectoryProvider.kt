package com.chuckerteam.chucker.internal.support

import java.io.File

internal fun interface CacheDirectoryProvider {
    fun provide(): File?
}
