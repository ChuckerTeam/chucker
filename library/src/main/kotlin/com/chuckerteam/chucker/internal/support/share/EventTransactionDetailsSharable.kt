package com.chuckerteam.chucker.internal.support.share

import android.content.Context
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.EventTransaction
import com.chuckerteam.chucker.internal.support.FormatUtils
import com.chuckerteam.chucker.internal.support.Sharable
import okio.Buffer
import okio.Source

internal class EventTransactionDetailsSharable(
    private val transaction: EventTransaction
) : Sharable {
    override fun toSharableContent(context: Context): Source = Buffer().apply {
        writeUtf8("${context.getString(R.string.chucker_title)}: ${transaction.title}\n")
        writeUtf8("${context.getString(R.string.chucker_payload)}: ${transaction.payload}\n")
        writeUtf8("${context.getString(R.string.chucker_receive_time)}: ${transaction.receiveDateString}\n")
    }
}
