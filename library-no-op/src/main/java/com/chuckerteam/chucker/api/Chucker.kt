package com.chuckerteam.chucker.api

import android.content.Context
import android.content.Intent

/**
 * No-op implementation.
 */
public object Chucker {

    @Suppress("MayBeConst ") // https://github.com/ChuckerTeam/chucker/pull/169#discussion_r362341353
    public val isOp: Boolean = false

    @JvmStatic
    public fun getLaunchIntent(context: Context): Intent = Intent()

    @JvmStatic
    public fun dismissNotifications(context: Context) {
        // Empty method for the library-no-op artifact
    }
}
