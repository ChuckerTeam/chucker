package com.chuckerteam.chucker.internal.support

import com.chuckerteam.chucker.api.Chucker
import java.text.SimpleDateFormat
import java.util.*

private const val LOCALISED_DATE_FORMAT = "dd MMM yyyy hh:mm a EEE"
object DateFormatter {
    private val simpleDateFormat = SimpleDateFormat(LOCALISED_DATE_FORMAT, Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }

    fun getFormattedTime(timeStamp: Long): String {
        return if(Chucker.configs?.localiseTime == true){
            simpleDateFormat.format(Date(timeStamp))
        } else {
            Date(timeStamp).toString()
        }
    }

}