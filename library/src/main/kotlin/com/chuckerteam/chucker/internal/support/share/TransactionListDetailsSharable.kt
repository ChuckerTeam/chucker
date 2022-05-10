package com.chuckerteam.chucker.internal.support.share

import android.content.Context
import com.chuckerteam.chucker.R.string
import com.chuckerteam.chucker.internal.data.entity.EventTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import com.chuckerteam.chucker.internal.data.entity.Transaction
import com.chuckerteam.chucker.internal.support.Sharable
import com.chuckerteam.chucker.internal.support.toSharableUtf8Content
import okio.Buffer
import okio.Source

internal class TransactionListDetailsSharable(
    transactions: List<Transaction>,
    encodeUrls: Boolean,
) : Sharable {
    private val transactions = transactions.map {
        return@map when (it) {
            is HttpTransaction -> HttpTransactionDetailsSharable(it, encodeUrls)
            is EventTransaction -> EventTransactionDetailsSharable(it)
            is HttpTransactionTuple -> TODO("NOT SUPPORTED")
        }
    }

    override fun toSharableContent(context: Context): Source = Buffer().writeUtf8(
        transactions.joinToString(
            separator = "\n${context.getString(string.chucker_export_separator)}\n",
            prefix = "${context.getString(string.chucker_export_prefix)}\n",
            postfix = "\n${context.getString(string.chucker_export_postfix)}\n",
        ) { it.toSharableUtf8Content(context) }
    )
}
