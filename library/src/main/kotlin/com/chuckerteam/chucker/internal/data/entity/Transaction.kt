package com.chuckerteam.chucker.internal.data.entity

internal sealed interface Transaction {
    val id: Long
    val notificationText: String
    val time : Long

    fun hasTheSameContent(other: Transaction?): Boolean

    enum class Type {
        Http,
        Event
    }
}
