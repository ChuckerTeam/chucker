package com.rohitjakhar.composechucker.internal.ui.screen.transaction_list

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.asFlow
import androidx.navigation.NavHostController
import com.rohitjakhar.composechucker.R
import com.rohitjakhar.composechucker.internal.extension.getActivity
import com.rohitjakhar.composechucker.internal.support.TransactionListDetailsSharable
import com.rohitjakhar.composechucker.internal.support.shareAsFile
import com.rohitjakhar.composechucker.internal.ui.ComposeMainActivity
import com.rohitjakhar.composechucker.internal.ui.MainViewModel
import com.rohitjakhar.composechucker.internal.ui.components.TransactionCard
import com.rohitjakhar.composechucker.internal.ui.theme.Typography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TransactionList(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    val scope = rememberCoroutineScope()
    val transactions = remember {
        mutableStateOf(viewModel.transactions.value)
    }
    var isLoading by remember {
        mutableStateOf(true)
    }
    var showDeleteDialog by remember {
        mutableStateOf(false)
    }
    var showShareDialog by remember {
        mutableStateOf(false)
    }
    val shareDialogProperties = remember {
        DialogProperties()
    }
    val context = LocalContext.current
    val applicationName = context.applicationInfo.loadLabel(context.packageManager).toString()
    LaunchedEffect(key1 = true) {
        viewModel.transactions.asFlow().collectLatest {
            transactions.value = it
            delay(400)
            isLoading = false
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = applicationName, style = Typography.headlineSmall)
                },
                actions = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "",
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clickable {
                                Toast
                                    .makeText(context, "search clicked", Toast.LENGTH_SHORT)
                                    .show()
                            }
                    )
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = "share",
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clickable {
                                showShareDialog = true
                            }
                    )
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "delete",
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clickable {
                                showDeleteDialog = true
                            }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Cyan)
            )
        }
    ) { paddingValues ->
        AnimatedVisibility(showDeleteDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                },
                title = {
                    Text(text = "Clear")
                },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            viewModel.clearTransactions()
                        }
                        showDeleteDialog = false
                    }) {
                        Text(text = "Clear")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                    }) {
                        Text(text = "Cancel")
                    }
                },
                text = {
                    Text(text = "Do you want to clear complete network calls history?")
                },
                properties = shareDialogProperties
            )
        }
        AnimatedVisibility(visible = showShareDialog) {
            AlertDialog(
                onDismissRequest = {
                    showShareDialog = false
                },
                title = {
                    Text(text = "Share All Transaction")
                },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            val allTransactions = viewModel.getAllTransactions()
                            if (allTransactions.isEmpty()) {
                                Toast.makeText(context, "Transcation empty", Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                            val sharableTransactions = TransactionListDetailsSharable(allTransactions, encodeUrls = false)
                            val shareIntent = withContext(Dispatchers.IO) {
                                sharableTransactions.shareAsFile(
                                    activity = context.getActivity() as Activity,
                                    fileName = ComposeMainActivity.EXPORT_TXT_FILE_NAME,
                                    intentTitle = context.getString(R.string.chucker_share_all_transactions_title),
                                    intentSubject = context.getString(R.string.chucker_share_all_transactions_subject),
                                    clipDataLabel = "transactions"
                                )
                            }
                            if (shareIntent != null) {
                                context.startActivity(shareIntent)
                            } else {
                                Toast.makeText(context, "Share intent null", Toast.LENGTH_SHORT).show()
                            }
                        }
                        showShareDialog = false
                    }) {
                        Text(text = "Share")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showShareDialog = false
                    }) {
                        Text(text = "Cancel")
                    }
                },
                properties = DialogProperties()
            )
        }
        Box(modifier = Modifier.padding(paddingValues)) {
            AnimatedVisibility(visible = isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            AnimatedVisibility(visible = (isLoading.not() && transactions.value.isNullOrEmpty())) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp), contentAlignment = Alignment.Center) {
                    Column {
                        Text(text = context.getString(R.string.chucker_setup), style = Typography.headlineMedium)
                        Text(text = context.getString(R.string.chucker_network_tutorial))
                        Text(text = context.getString(R.string.chucker_check_readme), color = Color.Cyan,
                            modifier = Modifier.clickable {

                        })
                    }
                }
            }
            AnimatedVisibility(visible = isLoading.not()) {
                LazyColumn(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxSize()
                ) {
                    items(transactions.value ?: emptyList(), key = { it.id }) {
                        TransactionCard(transaction = it, modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("transaction_details/${it.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}
