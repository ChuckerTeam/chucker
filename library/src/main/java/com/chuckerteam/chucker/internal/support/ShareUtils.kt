package com.chuckerteam.chucker.internal.support

import android.content.Context
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction

internal object ShareUtils {
    fun getStringFromTransactions(transactions: List<HttpTransaction>, context: Context): String {
        return transactions.joinToString(
            separator = "\n${context.getString(R.string.chucker_export_separator)}\n",
            prefix = "${context.getString(R.string.chucker_export_prefix)}\n",
            postfix = "\n${context.getString(R.string.chucker_export_postfix)}\n"
        ) { FormatUtils.getShareText(context, it, false) }
    }
}
