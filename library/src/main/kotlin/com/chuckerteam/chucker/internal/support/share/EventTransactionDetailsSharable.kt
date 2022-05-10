package com.chuckerteam.chucker.internal.support.share

import android.content.Context
import com.chuckerteam.chucker.internal.data.entity.EventTransaction
import com.chuckerteam.chucker.internal.support.Sharable
import okio.Source

internal class EventTransactionDetailsSharable(
    transaction: EventTransaction
) : Sharable {
    override fun toSharableContent(context: Context): Source {
        TODO("Not yet implemented")
    }
}
