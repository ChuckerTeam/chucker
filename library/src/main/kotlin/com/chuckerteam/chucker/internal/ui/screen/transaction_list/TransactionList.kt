package com.chuckerteam.chucker.internal.ui.screen.transaction_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asFlow
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import com.chuckerteam.chucker.internal.ui.MainViewModel
import com.chuckerteam.chucker.internal.ui.components.TransactionCard
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TransactionList(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    val transactions = remember {
        mutableStateOf(viewModel.transactions.value)
    }
    LaunchedEffect(key1 = true, block = {
        viewModel.transactions.asFlow().collectLatest {
            transactions.value = it
        }
    })
    Scaffold(topBar = {
        TopAppBar(title = {
            Text(text = "All Transactions ${transactions.value?.size}")
        })
    }) {
        Column(modifier = Modifier.padding(it)) {
            LazyColumn(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxSize()
            ) {
                items(transactions.value ?: emptyList()) {
                    TransactionCard(transactionTuple = it, modifier = Modifier.fillMaxWidth().clickable {
                        navController.navigate("transaction_details/${it.id}")
                    })
                }
            }
        }
    }

}
