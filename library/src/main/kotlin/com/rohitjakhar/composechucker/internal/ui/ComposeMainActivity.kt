package com.rohitjakhar.composechucker.internal.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.rohitjakhar.composechucker.internal.ui.navigations.ChuckerNav
import com.rohitjakhar.composechucker.internal.ui.theme.ChuckerTheme

internal class ComposeMainActivity : BaseChuckerComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChuckerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewmodel = viewModel<MainViewModel>()
                    ChuckerNav(navHostController = rememberNavController(), viewmodel)
                }
            }
        }
    }

    companion object {
        const val EXPORT_TXT_FILE_NAME = "transactions.txt"
        const val EXPORT_HAR_FILE_NAME = "transactions.har"
    }
}

@Composable
internal fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
internal fun GreetingPreview() {
    ChuckerTheme {
        Greeting("Android")
    }
}
