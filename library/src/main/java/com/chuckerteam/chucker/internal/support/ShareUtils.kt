package com.chuckerteam.chucker.internal.support

import android.content.Context
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal object ShareUtils {
    suspend fun getStringFromTransactions(transactions: List<HttpTransaction>, context: Context): String {
        return withContext(Dispatchers.Default) {
            transactions.joinToString(
                separator = "\n${context.getString(R.string.chucker_export_separator)}\n",
                prefix = "${context.getString(R.string.chucker_export_prefix)}\n",
                postfix = "\n${context.getString(R.string.chucker_export_postfix)}\n"
            ) { FormatUtils.getShareText(context, it, false) }
        }
    }
}
