package com.chuckerteam.chucker.internal.support

import android.content.Context
import okio.Buffer
import okio.Source

internal class TransactionDetailsHarSharable(
    private val content: String,
) : Sharable {
    override fun toSharableContent(context: Context): Source = Buffer().writeUtf8("$content\n")
}
