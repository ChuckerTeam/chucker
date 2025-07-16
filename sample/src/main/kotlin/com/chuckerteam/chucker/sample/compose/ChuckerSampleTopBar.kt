package com.chuckerteam.chucker.sample.compose

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chuckerteam.chucker.sample.R
import com.chuckerteam.chucker.sample.compose.testtags.ChuckerTestTags
import com.chuckerteam.chucker.sample.compose.theme.ChuckerTheme
import com.chuckerteam.chucker.sample.compose.theme.DarkTopAppBarBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChuckerSampleTopBar() {
    val isDarkTheme = isSystemInDarkTheme()
    val appBarColor =
        if (isDarkTheme) {
            DarkTopAppBarBackground
        } else {
            MaterialTheme.colorScheme.primary
        }

    TopAppBar(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
        title = {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.testTag(ChuckerTestTags.TOP_APP_BAR_TITLE),
            )
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = appBarColor,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White,
                actionIconContentColor = Color.White,
            ),
    )
}

@Composable
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
private fun ChuckerSampleTopBarPreview() {
    ChuckerTheme {
        ChuckerSampleTopBar()
    }
}
