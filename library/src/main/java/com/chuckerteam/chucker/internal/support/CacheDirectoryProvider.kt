package com.chuckerteam.chucker.internal.support

import java.io.File

/**
 * An interface that returns a reference to a cache directory where temporary files can be
 * saved.
 */
internal fun interface CacheDirectoryProvider {
    fun provide(): File?
}
