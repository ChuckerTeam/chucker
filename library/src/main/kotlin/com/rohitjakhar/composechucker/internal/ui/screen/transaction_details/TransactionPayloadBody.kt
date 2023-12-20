package com.rohitjakhar.composechucker.internal.ui.screen.transaction_details

import androidx.compose.foundation.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asImageBitmap
import com.rohitjakhar.composechucker.internal.ui.transaction.TransactionPayloadItem

@Composable
internal fun TransactionPayloadBody(
    transactionPayloadItem: TransactionPayloadItem?,
) {
    when (transactionPayloadItem) {
        is TransactionPayloadItem.BodyLineItem -> {
            Text(text = transactionPayloadItem.line.toString())
        }
        is TransactionPayloadItem.HeaderItem -> {
            Text(text = transactionPayloadItem.headers.toString())
        }
        is TransactionPayloadItem.ImageItem -> {
            Image(bitmap = transactionPayloadItem.image.asImageBitmap(), contentDescription = "")
        }
        null -> {}
    }
}
