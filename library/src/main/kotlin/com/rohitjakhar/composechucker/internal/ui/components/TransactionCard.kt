package com.rohitjakhar.composechucker.internal.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rohitjakhar.composechucker.internal.data.entity.HttpTransaction
import com.rohitjakhar.composechucker.internal.data.entity.HttpTransactionTuple
import java.text.DateFormat
import javax.net.ssl.HttpsURLConnection

@Composable
internal fun TransactionCard(transaction: HttpTransactionTuple, modifier: Modifier) {
    val statusColor: Color = when {
        transaction.status == HttpTransaction.Status.Failed -> {
            Color(0xFFF44336)
        }

        transaction.status == HttpTransaction.Status.Requested -> {
            Color(0xFF9E9E9E)
        }

        transaction.responseCode == null -> {
            Color.Unspecified
        }

        transaction.responseCode!! >= HttpsURLConnection.HTTP_INTERNAL_ERROR -> {
            Color(0xFFB71C1C)
        }

        transaction.responseCode!! >= HttpsURLConnection.HTTP_BAD_REQUEST -> {
            Color(0xFFFF9800)
        }

        transaction.responseCode!! >= HttpsURLConnection.HTTP_MULT_CHOICE -> {
            Color(0xFF0D47A1)
        }

        else -> {
            Color.Unspecified
        }
    }
    Card(modifier = modifier.padding(4.dp)) {
        Row(modifier = Modifier.padding(8.dp)) {
            Text(text = when (transaction.status) {
                HttpTransaction.Status.Complete -> transaction.responseCode.toString()
                HttpTransaction.Status.Failed -> "!!!"
                else -> ""
            }, modifier = Modifier.padding(horizontal = 2.dp), color = statusColor)
            Column(modifier.fillMaxWidth()) {
                Text(text = "${transaction.method.toString()} ${transaction.path.toString()}", color = statusColor,)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector =
                    if (transaction.isSsl)
                        Icons.Outlined.Lock
                    else
                        Icons.Outlined.LockOpen, contentDescription = "ssl", Modifier.size(18.dp))
                    Text(text = transaction.host.toString())
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(text = DateFormat.getTimeInstance().format(transaction.requestDate))
                    if (transaction.status == HttpTransaction.Status.Complete) {
                        Text(text = transaction.durationString.toString())
                        Text(text = transaction.totalSizeString)
                    }

                }
            }
        }
    }
}

@Preview
@Composable
internal fun TransactionCardPreview() {
    TransactionCard(HttpTransactionTuple(id = 3458, requestDate = null, tookMs = null, protocol = null, method = null, host = null, path = null, scheme = null, responseCode = null, requestPayloadSize = null, responsePayloadSize = null, error = null, graphQlDetected = false, graphQlOperationName = null), modifier = Modifier)
}
