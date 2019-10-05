package com.chuckerteam.chucker.internal.data.entity

import android.content.Context

interface NotificationTextProducer {
    fun notificationId(): Long

    fun notificationText(context: Context): String
}
