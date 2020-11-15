package com.chuckerteam.chucker.internal.support

import androidx.core.content.FileProvider

/**
 * We need our own subclass so we don't conflict with other [FileProvider]s
 * See: https://github.com/ChuckerTeam/chucker/issues/409
 */
internal class ChuckerFileProvider : FileProvider()
