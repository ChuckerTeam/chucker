package com.chuckerteam.chucker.internal.support

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.text.DateFormat
import java.util.Date
import java.util.Locale

class DateJsonAdapter {

    private val dateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US)

    @ToJson
    fun toJson(date: Date): String = dateFormat.format(date)

    @FromJson
    fun fromJson(date: String): Date = dateFormat.parse(date)!!
}
