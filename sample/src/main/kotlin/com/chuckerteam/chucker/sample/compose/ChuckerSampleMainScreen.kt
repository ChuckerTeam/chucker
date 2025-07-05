package com.chuckerteam.chucker.sample.compose

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chuckerteam.chucker.internal.ui.theme.ChuckerTheme
import com.chuckerteam.chucker.sample.InterceptorType
import com.chuckerteam.chucker.sample.R

/**
 * Main screen for the Chucker sample app, showing introduction text and controls.
 *
 * - In compact width (phone), it uses a vertical layout with intro text above the controls.
 * - In expanded width (tablet or landscape), it splits into two columns: intro text on the left,
 *   controls on the right.
 *
 * @param widthSizeClass Indicates the current window width size class to switch layouts.
 * @param selectedInterceptorType Currently selected interceptor type (HTTP or GraphQL).
 * @param onInterceptorTypeChange Callback when a new interceptor type is selected by the user.
 * @param onInterceptorTypeLabelClick Callback when the interceptor type label is clicked.
 * @param onDoHttp Called to perform a sample HTTP request.
 * @param onDoGraphQL Called to perform a sample GraphQL request.
 * @param onLaunchChucker Called to open the Chucker transaction list UI.
 * @param onExportToLogFile Called to export network logs to a plaintext file.
 * @param onExportToHarFile Called to export network logs to a HAR (HTTP Archive) file.
 * @param showChuckerOperations If true, displays the Chucker-specific operation buttons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChuckerSampleMainScreen(
    widthSizeClass: WindowWidthSizeClass,
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
    val isExpandedWidth = widthSizeClass == WindowWidthSizeClass.Expanded

    if (isExpandedWidth) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize(),
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
                Row {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.intro_title),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.intro_body),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        ChuckerSampleControls(
                            selectedInterceptorType = selectedInterceptorType,
                            onInterceptorTypeChange = onInterceptorTypeChange,
                            onInterceptorTypeLabelClick = onInterceptorTypeLabelClick,
                            onDoHttp = onDoHttp,
                            onDoGraphQL = onDoGraphQL,
                            onLaunchChucker = onLaunchChucker,
                            onExportToLogFile = onExportToLogFile,
                            onExportToHarFile = onExportToHarFile,
                            showChuckerOperations = showChuckerOperations,
                            isExpandedWidth = true,
                        )
                    }
                }
            }
        }
    } else {
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
                    ChuckerSampleControls(
                        selectedInterceptorType = selectedInterceptorType,
                        onInterceptorTypeChange = onInterceptorTypeChange,
                        onInterceptorTypeLabelClick = onInterceptorTypeLabelClick,
                        onDoHttp = onDoHttp,
                        onDoGraphQL = onDoGraphQL,
                        onLaunchChucker = onLaunchChucker,
                        onExportToLogFile = onExportToLogFile,
                        onExportToHarFile = onExportToHarFile,
                        showChuckerOperations = showChuckerOperations,
                    )
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
    name = "Phone – Light (Landscape)",
    device = Devices.AUTOMOTIVE_1024p,
    widthDp = 1024,
    showSystemUi = true,
    showBackground = true,
    uiMode = Configuration.UI_MODE_TYPE_NORMAL,
)
@Composable
private fun ChuckerSampleMainScreenPreview() {
    ChuckerTheme {
        ChuckerSampleMainScreen(
            widthSizeClass = WindowWidthSizeClass.Compact,
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
private fun ChuckerSampleMainScreenTabletPreview() {
    ChuckerTheme {
        ChuckerSampleMainScreen(
            widthSizeClass = WindowWidthSizeClass.Expanded,
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
