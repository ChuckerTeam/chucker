package com.chuckerteam.chucker.internal.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.*

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

    override val time: Long
        get() = receivedDate ?: 0

    val receiveDateString: String?
        get() = receivedDate?.let { Date(it).toString() }

    override fun hasTheSameContent(other: Transaction?): Boolean {
        if (this === other) return true
        if (other == null) return false

        if (other !is EventTransaction) {
            return false
        }

        return (id == other.id) &&
            (title == other.title) &&
            (receivedDate == other.receivedDate) &&
            (payload == other.payload)
    }
}
