package com.chuckerteam.chucker.api

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.getSystemService
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.internal.support.Logger
import com.chuckerteam.chucker.internal.support.NotificationHelper
import com.chuckerteam.chucker.internal.support.TransactionListDetailsSharable
import com.chuckerteam.chucker.internal.support.writeToFile
import com.chuckerteam.chucker.internal.ui.MainActivity
import kotlinx.coroutines.runBlocking

/**
 * Chucker methods and utilities to interact with the library.
 */
public object Chucker {

    private const val SHORTCUT_ID = "chuckerShortcutId"

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

    /**
     * Create a shortcut to launch Chucker UI.
     * @param context An Android [Context].
     */
    internal fun createShortcut(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            return
        }

        val shortcutManager = context.getSystemService<ShortcutManager>() ?: return
        if (shortcutManager.dynamicShortcuts.any { it.id == SHORTCUT_ID }) {
            return
        }

        val shortcut = ShortcutInfo.Builder(context, SHORTCUT_ID)
            .setShortLabel(context.getString(R.string.chucker_shortcut_label))
            .setLongLabel(context.getString(R.string.chucker_shortcut_label))
            .setIcon(Icon.createWithResource(context, R.mipmap.chucker_ic_launcher))
            .setIntent(getLaunchIntent(context).setAction(Intent.ACTION_VIEW))
            .build()
        try {
            shortcutManager.addDynamicShortcuts(listOf(shortcut))
        } catch (e: IllegalArgumentException) {
            Logger.warn("ShortcutManager addDynamicShortcuts failed ", e)
        } catch (e: IllegalStateException) {
            Logger.warn("ShortcutManager addDynamicShortcuts failed ", e)
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
