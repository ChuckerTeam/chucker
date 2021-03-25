package com.chuckerteam.chucker.api

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.support.Logger
import com.chuckerteam.chucker.internal.support.NotificationHelper
import com.chuckerteam.chucker.internal.ui.MainActivity

/**
 * Chucker methods and utilities to interact with the library.
 */
public object Chucker {

    /**
     * Check if this instance is the operation one or no-op.
     * @return `true` if this is the operation instance.
     */
    @Suppress("MayBeConst ") // https://github.com/ChuckerTeam/chucker/pull/169#discussion_r362341353
    public val isOp: Boolean = true

    /**
     * Get an Intent to launch the Chucker UI directly.
     * @param context An Android [Context].
     * @return An Intent for the main Chucker Activity that can be started with [Context.startActivity].
     */
    @JvmStatic
    public fun getLaunchIntent(context: Context): Intent {
        return Intent(context, MainActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    public fun Context.createShortcut() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            getSystemService(ShortcutManager::class.java)?.let {
                val shortcut = ShortcutInfo.Builder(this, "id1")
                    .setShortLabel("Open chucker activity")
                    .setLongLabel("Open chucker activity")
                    .setIcon(Icon.createWithResource(this, R.mipmap.chucker_ic_launcher_round))
                    .setIntent(getLaunchIntent(this).setAction(Intent.ACTION_VIEW))
                    .build()
                val allShortcuts = it.dynamicShortcuts.toMutableList()
                allShortcuts.add(shortcut)
                it.dynamicShortcuts = allShortcuts
            }
        }
    }

    /**
     * Dismisses all previous Chucker notifications.
     */
    @JvmStatic
    public fun dismissNotifications(context: Context) {
        NotificationHelper(context).dismissNotifications()
    }

    internal var logger: Logger = object : Logger {
        val TAG = "Chucker"

        override fun info(message: String, throwable: Throwable?) {
            Log.i(TAG, message, throwable)
        }

        override fun warn(message: String, throwable: Throwable?) {
            Log.w(TAG, message, throwable)
        }

        override fun error(message: String, throwable: Throwable?) {
            Log.e(TAG, message, throwable)
        }
    }
}
