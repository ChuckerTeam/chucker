package com.chuckerteam.chucker.internal.support

import java.io.File

internal fun interface CacheDirectoryFactory {
    fun create(): File?
}
