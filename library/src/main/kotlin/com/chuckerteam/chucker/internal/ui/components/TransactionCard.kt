package com.chuckerteam.chucker.internal.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import java.text.DateFormat

@Composable
internal fun TransactionCard(transactionTuple: HttpTransactionTuple, modifier: Modifier) {
    Card (modifier = modifier
        .padding(4.dp)
    ){
        Row (modifier=Modifier.padding(8.dp)){
            Text(text = transactionTuple.responseCode.toString(), modifier = Modifier.padding(horizontal = 2.dp))
            Column {
                Text(text = transactionTuple.path.toString())
                Text(text = transactionTuple.method.toString())
                Text(text = transactionTuple.host.toString())
                Row (horizontalArrangement = Arrangement.SpaceEvenly){
                    Text(text = DateFormat.getTimeInstance().format(transactionTuple.requestDate))
                    Text(text = transactionTuple.durationString.toString())
                    Text(text = transactionTuple.totalSizeString)
                }
            }
        }
    }
}

@Preview
@Composable
internal fun TransactionCardPreview(){
    TransactionCard(HttpTransactionTuple(
        id = 3458,
        requestDate = null,
        tookMs = null,
        protocol = null,
        method = null,
        host = null,
        path = null,
        scheme = null,
        responseCode = null,
        requestPayloadSize = null,
        responsePayloadSize = null,
        error = null,
        graphQlDetected = false,
        graphQlOperationName = null
    ), modifier = Modifier)
}
