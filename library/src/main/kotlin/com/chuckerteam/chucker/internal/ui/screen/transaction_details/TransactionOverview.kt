package com.chuckerteam.chucker.internal.ui.screen.transaction_details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction

@Composable
internal fun TransactionOverview(
    httpTransaction: HttpTransaction?,
    modifier: Modifier
) {
    Column(modifier = modifier) {
        Row(modifier = Modifier.padding(4.dp)) {
            Text(text = "Url", modifier = Modifier.weight(1.0f))
            Text(text = httpTransaction?.url.toString())
        }
        Row() {
            Text(text = "Method")
            Text(text = httpTransaction?.method.toString())
        }
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(text = "Protocol")
            Text(text = httpTransaction?.protocol.toString())
        }
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(text = "Status")
            Text(text = httpTransaction?.status.toString())
        }
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(text = "Response")
            Text(text = httpTransaction?.responseSummaryText.toString())
        }
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(text = "SSL")
            Text(text = httpTransaction?.isSsl.toString())
        }
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(text = "TLS version")
            Text(text = httpTransaction?.responseTlsVersion.toString())
        }
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(text = "Cipher Suite")
            Text(text = httpTransaction?.responseCipherSuite.toString())
        }
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(text = "Request time")
            Text(text = httpTransaction?.requestDateString.toString())
        }
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(text = "Response time")
            Text(text = httpTransaction?.responseDateString.toString())
        }
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(text = "Duration")
            Text(text = httpTransaction?.durationString.toString())
        }
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(text = "Request size")
            Text(text = httpTransaction?.requestPayloadSize.toString())
        }
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(text = "Response size")
            Text(text = httpTransaction?.responseSizeString.toString())
        }
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(text = "Total size")
            Text(text = httpTransaction?.totalSizeString.toString())
        }
    }
}
