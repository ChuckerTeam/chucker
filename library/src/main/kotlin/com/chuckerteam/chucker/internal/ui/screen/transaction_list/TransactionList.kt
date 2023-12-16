package com.chuckerteam.chucker.internal.ui.screen.transaction_list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asFlow
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import com.chuckerteam.chucker.internal.ui.MainViewModel
import com.chuckerteam.chucker.internal.ui.components.TransactionCard
import kotlinx.coroutines.delay
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
    var isLoading by remember {
        mutableStateOf(true)
    }
    LaunchedEffect(key1 = true, block = {
        viewModel.transactions.asFlow().collectLatest {
            transactions.value = it
            delay(400)
            isLoading = it.isEmpty()
        }
    })
    Scaffold(topBar = {
        TopAppBar(title = {
            Text(text = "All Transactions ${transactions.value?.size}")
        })
    }) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            AnimatedVisibility(visible = isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            AnimatedVisibility(visible = isLoading.not()) {
                LazyColumn(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxSize()
                ) {
                    items(transactions.value ?: emptyList(), key = { it.id }) {
                        TransactionCard(transactionTuple = it, modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("transaction_details/${it.id}")
                            })
                    }
                }
            }
        }
    }

}
