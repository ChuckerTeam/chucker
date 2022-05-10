package com.chuckerteam.chucker.internal.data.entity

internal sealed interface Transaction {
    val id: Long
    val notificationText: String
}
