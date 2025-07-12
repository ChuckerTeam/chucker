package com.chuckerteam.chucker.sample

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.chuckerteam.chucker.api.Chucker
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ExportFormat
import com.chuckerteam.chucker.sample.compose.ChuckerSampleMainScreen
import com.chuckerteam.chucker.sample.compose.theme.ChuckerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val interceptorTypeSelector = InterceptorTypeSelector()

class MainActivity : ComponentActivity() {
    private val client by lazy {
        createOkHttpClient(applicationContext, interceptorTypeSelector)
    }

    private val httpTasks by lazy {
        listOf(HttpBinHttpTask(client), DummyImageHttpTask(client), PostmanEchoHttpTask(client))
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChuckerTheme {
                val windowSize = calculateWindowSizeClass(this)
                var selectedType by remember { mutableStateOf(interceptorTypeSelector.value) }
                ChuckerSampleMainScreen(
                    widthSizeClass = windowSize.widthSizeClass,
                    selectedInterceptorType = selectedType,
                    onInterceptorTypeChange = { newType ->
                        selectedType = newType
                        interceptorTypeSelector.value = newType
                    },
                    onInterceptorTypeLabelClick = ::openUrlInBrowser,
                    onDoHttp = {
                        for (task in httpTasks) {
                            task.run()
                        }
                    },
                    onDoGraphQL = {
                        GraphQlTask(client).run()
                    },
                    onLaunchChucker = {
                        launchChuckerDirectly()
                    },
                    onExportToLogFile = {
                        generateExportFile(ExportFormat.LOG)
                    },
                    onExportToHarFile = {
                        generateExportFile(ExportFormat.HAR)
                    },
                    isChuckerInOpMode = Chucker.isOp,
                )
            }
        }

        StrictMode.setVmPolicy(
            StrictMode.VmPolicy
                .Builder()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build(),
        )

        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy
                .Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .penaltyLog()
                .penaltyDeath()
                .build(),
        )
    }

    private fun launchChuckerDirectly() {
        // Optionally launch Chucker directly from your own app UI
        startActivity(Chucker.getLaunchIntent(this))
    }

    private fun generateExportFile(exportFormat: ExportFormat) {
        lifecycleScope.launch {
            val uri =
                withContext(Dispatchers.IO) {
                    ChuckerCollector(this@MainActivity)
                        .writeTransactions(this@MainActivity, null, exportFormat)
                }
            if (uri == null) {
                Toast
                    .makeText(
                        applicationContext,
                        R.string.export_to_file_failure,
                        Toast.LENGTH_SHORT,
                    ).show()
            } else {
                val successMessage =
                    applicationContext.getString(R.string.export_to_file_success, uri.path)
                Toast.makeText(applicationContext, successMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openUrlInBrowser() {
        val url = getString(R.string.interceptor_url)
        val intent =
            Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

        try {
            startActivity(Intent.createChooser(intent, "Open with"))
        } catch (e: ActivityNotFoundException) {
            Log.e("openUrlInBrowser", "No application can handle this request: ${e.message}", e)
        }
    }
}
