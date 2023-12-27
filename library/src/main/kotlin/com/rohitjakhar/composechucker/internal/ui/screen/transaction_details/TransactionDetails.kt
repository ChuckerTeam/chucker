package com.rohitjakhar.composechucker.internal.ui.screen.transaction_details

import android.content.Context
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rohitjakhar.composechucker.R
import com.rohitjakhar.composechucker.internal.data.entity.HttpTransaction
import com.rohitjakhar.composechucker.internal.support.calculateLuminance
import com.rohitjakhar.composechucker.internal.ui.transaction.PayloadType
import com.rohitjakhar.composechucker.internal.ui.transaction.TransactionPayloadItem
import com.rohitjakhar.composechucker.internal.ui.transaction.TransactionViewModel
import com.rohitjakhar.composechucker.internal.ui.transaction.TransactionViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun TransactionDetails(
    transactionId: Long
) {
    val context = LocalContext.current
    val viewmodel =
        viewModel<TransactionViewModel>(factory = TransactionViewModelFactory(transactionId = transactionId))
    val scope = rememberCoroutineScope()
    val horizontalState = rememberPagerState {
        3
    }
    val transactionsData = viewmodel.transaction.asFlow().collectAsState(initial = null)
    val payloadItems = remember {
        mutableStateListOf<TransactionPayloadItem>()
    }
    var showDeleteDialog by remember {
        mutableStateOf(false)
    }
    var showShareDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(key1 = horizontalState.currentPage, block = {
        Log.d("testrohit", "TransactionDetails: current: ${horizontalState.currentPage}")
        Log.d("testrohit", "TransactionDetails: target: ${horizontalState.targetPage}")
        when (horizontalState.currentPage) {
            1 -> {
                val items = processPayload(
                    type = PayloadType.REQUEST,
                    transactionsData.value,
                    true,
                    context
                )
                payloadItems.clear()
                payloadItems.addAll(
                    items
                )
            }

            2 -> {
                val items = processPayload(
                    type = PayloadType.RESPONSE,
                    transactionsData.value,
                    true,
                    context
                )
                payloadItems.clear()
                payloadItems.addAll(
                    items
                )
            }
        }
    })

    val titles = arrayOf(
        context.getString(R.string.chucker_overview),
        context.getString(R.string.chucker_request),
        context.getString(R.string.chucker_response)
    )
    Scaffold(
        topBar = {
            TopAppBar(title = {
                AnimatedContent(targetState = horizontalState.currentPage, label = "") {
                    Text(text = titles[it])
                }
            }, actions = {
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
            })
        }
    ) { paddingValues ->
        AnimatedVisibility(visible = showDeleteDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                },
                text = {
                    Text(text = stringResource(id = R.string.chucker_clear_http_transaction_confirmation))
                },
                confirmButton = {
                    Text(text = stringResource(id = R.string.chucker_yes),
                        Modifier.clickable {
                            showDeleteDialog = false
                        })
                },
                dismissButton = {
                    Text(text = stringResource(id = R.string.chucker_cancel), Modifier.clickable {
                        showDeleteDialog = false
                    })
                }
            )
        }
        AnimatedVisibility(visible = showShareDialog) {
            AlertDialog(
                onDismissRequest = {
                    showShareDialog = false
                },
                text = {
                    Text(text = context.getString(R.string.chucker_share_as_curl))
                },
                confirmButton = {
                    Text(text = stringResource(id = R.string.chucker_yes), Modifier.clickable {
                        showShareDialog = false
                    })
                },
                dismissButton = {
                    Text(text = stringResource(id = R.string.chucker_cancel), Modifier.clickable {
                        showShareDialog = false
                    })
                }
            )
        }
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            TabRow(selectedTabIndex = horizontalState.currentPage,
                divider = {
                    Divider()
                }) {
                titles.forEachIndexed { index, s ->
                    Tab(
                        selected = index == horizontalState.currentPage,
                        onClick = {
                            scope.launch {
                                horizontalState.animateScrollToPage(index)
                            }
                        },
                    ) {
                        Text(text = s, modifier = Modifier.padding(12.dp))
                    }
                }
            }
            HorizontalPager(
                state = horizontalState,
                userScrollEnabled = true,
            ) { page: Int ->
                when (page) {
                    1, 2 -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(payloadItems) {
                                TransactionPayloadBody(transactionPayloadItem = it)
                            }
                        }
                    }

                    else -> {
                        TransactionOverview(
                            httpTransaction = transactionsData.value,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

            }


        }
    }

}

private suspend fun processPayload(
    type: PayloadType,
    transaction: HttpTransaction?,
    formatRequestBody: Boolean,
    context: Context
): MutableList<TransactionPayloadItem> {
    if (transaction == null) {
        return mutableListOf()
    }
    return withContext(Dispatchers.Default) {
        val result = mutableListOf<TransactionPayloadItem>()
        val headersString: String
        val isBodyEncoded: Boolean
        val bodyString: CharSequence

        if (type == PayloadType.REQUEST) {
            headersString = transaction.getRequestHeadersString(true)
            isBodyEncoded = transaction.isRequestBodyEncoded
            bodyString = if (formatRequestBody) {
                transaction.getSpannedRequestBody(context)
            } else {
                transaction.requestBody ?: ""
            }
        } else {
            headersString = transaction.getResponseHeadersString(true)
            isBodyEncoded = transaction.isResponseBodyEncoded
            bodyString = transaction.getSpannedResponseBody(context)
        }
        if (headersString.isNotBlank()) {
            result.add(
                TransactionPayloadItem.HeaderItem(
                    HtmlCompat.fromHtml(
                        headersString,
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                )
            )
        }

        // The body could either be an image, plain text, decoded binary or not decoded binary.
        val responseBitmap = transaction.responseImageBitmap

        if (type == PayloadType.RESPONSE && responseBitmap != null) {
            val bitmapLuminance = responseBitmap.calculateLuminance()
            result.add(TransactionPayloadItem.ImageItem(responseBitmap, bitmapLuminance))
            return@withContext result
        }

        when {
            isBodyEncoded -> {
                val text = context.getString(R.string.chucker_body_omitted)
                result.add(TransactionPayloadItem.BodyLineItem(SpannableStringBuilder.valueOf(text)))
            }

            bodyString.isBlank() -> {
                val text = context.getString(R.string.chucker_body_empty)
                result.add(TransactionPayloadItem.BodyLineItem(SpannableStringBuilder.valueOf(text)))
            }

            else -> bodyString.lines().forEach {
                result.add(
                    TransactionPayloadItem.BodyLineItem(
                        SpannableStringBuilder.valueOf(it)
                    )
                )
            }
        }
        return@withContext result
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
internal fun TransactionDetailsPreview() {
    TransactionDetails(0L)
}
