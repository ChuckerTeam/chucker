package com.chuckerteam.chucker.api.internal.data.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey

import com.chuckerteam.chucker.api.internal.support.FormatUtils

/**
 * Represent a Throwable that was fired from an App.
 */
@Entity(tableName = "throwables")
internal data class RecordedThrowable(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long? = 0,
        @ColumnInfo(name = "tag") var tag: String?,
        @ColumnInfo(name = "date") var date: Long?,
        @ColumnInfo(name = "clazz") var clazz: String?,
        @ColumnInfo(name = "message") var message: String?,
        @ColumnInfo(name = "content") var content: String?
) {
    @Ignore
    constructor(tag: String, throwable: Throwable) : this(null, null, null, null, null, null) {
        this.tag = tag
        this.date = System.currentTimeMillis()
        this.clazz = throwable.javaClass.name
        this.message = throwable.message
        this.content = FormatUtils.formatThrowable(throwable)
    }
}
