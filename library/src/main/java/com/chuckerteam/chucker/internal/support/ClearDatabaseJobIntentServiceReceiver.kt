package com.chuckerteam.chucker.internal.support

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

internal class ClearDatabaseJobIntentServiceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        ClearDatabaseService.enqueueWork(context, intent)
    }
}
