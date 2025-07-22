package com.chuckerteam.chucker.sample.compose

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chuckerteam.chucker.sample.InterceptorType
import com.chuckerteam.chucker.sample.R
import com.chuckerteam.chucker.sample.compose.theme.AppAccentColor
import com.chuckerteam.chucker.sample.compose.theme.ChuckerTheme

/**
 * A container for the main controls and actions in the Chucker sample app.
 *
 * @param selectedInterceptorType current interceptor selection.
 * @param onInterceptorTypeChange called when user selects a different interceptor.
 * @param onInterceptorTypeLabelClick called when the interceptor label is clicked.
 * @param onDoHttp performs an HTTP call.
 * @param onDoGraphQL performs a GraphQL call.
 * @param onDoFlutterHttp performs a Flutter HTTP call.
 * @param onLaunchChucker launches Chucker UI directly.
 * @param onExportToLogFile exports logs to file.
 * @param onExportToHarFile exports HAR to file.
 * @param isChuckerInOpMode controls visibility of Chucker-specific operations.
 */
@Composable
internal fun ChuckerSampleControls(
        selectedInterceptorType: InterceptorType,
        onInterceptorTypeChange: (InterceptorType) -> Unit,
        onInterceptorTypeLabelClick: () -> Unit,
        onDoHttp: () -> Unit,
        onDoGraphQL: () -> Unit,
        onDoFlutterHttp: () -> Unit,
        onLaunchChucker: () -> Unit,
        onExportToLogFile: () -> Unit,
        onExportToHarFile: () -> Unit,
        isChuckerInOpMode: Boolean,
        isExpandedWidth: Boolean = false,
) {
    val modifier =
            if (isExpandedWidth) {
                Modifier.fillMaxWidth()
            } else {
                Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxWidth()
            }
    val interceptorTypeLabel = stringResource(R.string.interceptor_type)
    Text(
            text = interceptorTypeLabel,
            style = MaterialTheme.typography.bodyLarge,
            color = AppAccentColor,
            textDecoration = TextDecoration.Underline,
            modifier =
                    modifier
                        .clearAndSetSemantics {
                            contentDescription =
                                "$interceptorTypeLabel, opens external link, double tap to activate"
                        }
                        .clickable { onInterceptorTypeLabelClick.invoke() },
    )

    Spacer(modifier = Modifier.height(4.dp))

    Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier,
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
        stringResource(R.string.do_flutter_http_activity) to onDoFlutterHttp,
            )
            .forEach { (label, action) ->
                Button(
                        onClick = action,
                        modifier = modifier,
                        shape = RoundedCornerShape(4.dp),
                ) { Text(text = label) }
            }

    if (isChuckerInOpMode) {
        Button(
                onClick = onLaunchChucker,
                modifier = modifier,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
        ) {
            Text(
                    text = stringResource(R.string.launch_chucker_directly),
            )
        }
    }

    Spacer(modifier = Modifier.width(24.dp))
    if (isChuckerInOpMode) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Button(
                    onClick = onExportToLogFile,
                    modifier = Modifier.weight(1f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
            ) { Text(stringResource(R.string.export_to_file)) }
            Button(
                    onClick = onExportToHarFile,
                    modifier = Modifier.weight(1f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
            ) { Text(stringResource(R.string.export_to_file_har)) }
        }
    }
}

@Preview(
        name = "Phone - Light",
        device = Devices.PIXEL_4,
        showBackground = true,
        uiMode = Configuration.UI_MODE_TYPE_NORMAL,
)
@Preview(
        name = "Phone - Dark",
        device = Devices.PIXEL_4,
        showBackground = true,
        uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(
        name = "Phone Scaled - Light",
        device = Devices.PIXEL_4,
        showBackground = true,
        fontScale = 1.5f,
        uiMode = Configuration.UI_MODE_TYPE_NORMAL,
)
@Preview(
        name = "Phone – Light (Landscape)",
        device = Devices.AUTOMOTIVE_1024p,
        widthDp = 1024,
        showBackground = true,
        uiMode = Configuration.UI_MODE_TYPE_NORMAL,
)
@Composable
private fun ChuckerSampleControlsPreview() {
    ChuckerTheme {
        Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ChuckerSampleControls(
                    selectedInterceptorType = InterceptorType.NETWORK,
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
                    onDoFlutterHttp = {
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
                    isChuckerInOpMode = true,
            )
        }
    }
}

@Preview(
        name = "Tablet - Light",
        device = Devices.NEXUS_10,
        showBackground = true,
        uiMode = Configuration.UI_MODE_TYPE_NORMAL,
)
@Preview(
        name = "Tablet - Dark",
        device = Devices.NEXUS_10,
        showBackground = true,
        uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ChuckerSampleControlsTabletPreview() {
    ChuckerTheme {
        Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ChuckerSampleControls(
                    selectedInterceptorType = InterceptorType.NETWORK,
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
                    onDoFlutterHttp = {
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
                    isChuckerInOpMode = true,
            )
        }
    }
}
