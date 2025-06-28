package com.chuckerteam.chucker.sample.compose

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chuckerteam.chucker.internal.ui.theme.AppAccentColor
import com.chuckerteam.chucker.internal.ui.theme.ChuckerTheme
import com.chuckerteam.chucker.sample.InterceptorType
import com.chuckerteam.chucker.sample.R

/**
 * Main screen for the Chucker sample app.
 *
 * @param selectedInterceptorType current interceptor selection.
 * @param onInterceptorTypeChange called when user selects a different interceptor.
 * @param onInterceptorTypeLabelClick called when the interceptor label is clicked.
 * @param onDoHttp performs an HTTP call.
 * @param onDoGraphQL performs a GraphQL call.
 * @param onLaunchChucker launches Chucker UI directly.
 * @param onExportToLogFile exports logs to file.
 * @param onExportToHarFile exports HAR to file.
 * @param showChuckerOperations controls visibility of Chucker-specific operations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChuckerSampleMainScreen(
    selectedInterceptorType: InterceptorType,
    onInterceptorTypeChange: (InterceptorType) -> Unit,
    onInterceptorTypeLabelClick: () -> Unit,
    onDoHttp: () -> Unit,
    onDoGraphQL: () -> Unit,
    onLaunchChucker: () -> Unit,
    onExportToLogFile: () -> Unit,
    onExportToHarFile: () -> Unit,
    showChuckerOperations: Boolean,
) {
    val interceptorTypeLabel = stringResource(R.string.interceptor_type)
    Scaffold(
        topBar = {
            ChuckerSampleTopBar()
        },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            Column(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.intro_body),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .widthIn(max = 500.dp)
                            .fillMaxWidth(),
                )
                Text(
                    text = interceptorTypeLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppAccentColor,
                    textDecoration = TextDecoration.Underline,
                    modifier =
                        Modifier
                            .widthIn(max = 500.dp)
                            .fillMaxWidth()
                            .clearAndSetSemantics {
                                contentDescription =
                                    "$interceptorTypeLabel, opens external link, double tap to activate"
                            }
                            .clickable {
                                onInterceptorTypeLabelClick.invoke()
                            },
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .widthIn(max = 500.dp)
                            .fillMaxWidth(),
                ) {
                    LabeledRadioButton(
                        label = stringResource(R.string.application_type),
                        selected = selectedInterceptorType == InterceptorType.APPLICATION,
                        onClick = { onInterceptorTypeChange(InterceptorType.APPLICATION) },
                        modifier = Modifier.weight(1f),
                        index = 1,
                    )
                    LabeledRadioButton(
                        label = stringResource(R.string.network_type),
                        selected = selectedInterceptorType == InterceptorType.NETWORK,
                        onClick = { onInterceptorTypeChange(InterceptorType.NETWORK) },
                        modifier = Modifier.weight(1f),
                        index = 2,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                listOf(
                    stringResource(R.string.do_http_activity) to onDoHttp,
                    stringResource(R.string.do_graphql_activity) to onDoGraphQL,
                ).forEach { (label, action) ->
                    Button(
                        onClick = action,
                        modifier =
                            Modifier
                                .widthIn(max = 500.dp)
                                .fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text(text = label)
                    }
                }

                if (showChuckerOperations) {
                    Button(
                        onClick = onLaunchChucker,
                        modifier =
                            Modifier
                                .widthIn(max = 500.dp)
                                .fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text(
                            text =
                                stringResource(R.string.launch_chucker_directly),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(24.dp))
                if (showChuckerOperations) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Button(
                            onClick = onExportToLogFile,
                            modifier =
                                Modifier.weight(1f),
                            shape = RoundedCornerShape(4.dp),
                        ) {
                            Text(stringResource(R.string.export_to_file))
                        }
                        Button(
                            onClick = onExportToHarFile,
                            modifier =
                                Modifier
                                    .weight(1f),
                            shape = RoundedCornerShape(4.dp),
                        ) {
                            Text(stringResource(R.string.export_to_file_har))
                        }
                    }
                }
            }
        }
    }
}

@Preview(
    name = "Phone - Light",
    device = Devices.PIXEL_4,
    showSystemUi = true,
    showBackground = true,
    uiMode = Configuration.UI_MODE_TYPE_NORMAL,
)
@Preview(
    name = "Phone - Dark",
    device = Devices.PIXEL_4,
    showSystemUi = true,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(
    name = "Phone Scaled - Light",
    device = Devices.PIXEL_4,
    showSystemUi = true,
    showBackground = true,
    fontScale = 1.5f,
    uiMode = Configuration.UI_MODE_TYPE_NORMAL,
)
@Preview(
    name = "Tablet - Light",
    device = Devices.NEXUS_10,
    showSystemUi = true,
    showBackground = true,
    uiMode = Configuration.UI_MODE_TYPE_NORMAL,
)
@Preview(
    name = "Tablet - Dark",
    device = Devices.NEXUS_10,
    showSystemUi = true,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ChuckerSampleMainScreenPreview() {
    ChuckerTheme {
        ChuckerSampleMainScreen(
            selectedInterceptorType = InterceptorType.APPLICATION,
            onInterceptorTypeChange = {
                // DO Nothing
            },
            onInterceptorTypeLabelClick = {
                // DO Nothing
            },
            onDoHttp = {
                // DO Nothing
            },
            onDoGraphQL = {
                // DO Nothing
            },
            onLaunchChucker = {
                // DO Nothing
            },
            onExportToLogFile = {
                // DO Nothing
            },
            onExportToHarFile = {
                // DO Nothing
            },
            showChuckerOperations = true,
        )
    }
}
