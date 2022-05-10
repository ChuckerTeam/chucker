package com.chuckerteam.chucker.internal.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Suppress("LongParameterList")
@Entity(tableName = "event_transactions")
internal class EventTransaction(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id")
    override var id: Long = 0,
    @ColumnInfo(name = "receivedDate") var receivedDate: Long?,
    @ColumnInfo(name = "title") var title: String?,
    @ColumnInfo(name = "payload") var payload: String?,
) : Transaction {
    @Ignore
    constructor() : this(
        title = null,
        receivedDate = 0,
        payload = null,
    )

    override val notificationText: String
        get() = title ?: ""
}
