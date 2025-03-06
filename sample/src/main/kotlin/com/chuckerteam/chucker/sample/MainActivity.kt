package com.chuckerteam.chucker.sample

import android.os.Bundle
import android.os.StrictMode
import android.text.method.LinkMovementMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.chuckerteam.chucker.api.Chucker
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ExportFormat
import com.chuckerteam.chucker.sample.databinding.ActivityMainSampleBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val interceptorTypeSelector = InterceptorTypeSelector()

class MainActivity : AppCompatActivity() {
    private lateinit var mainBinding: ActivityMainSampleBinding

    private val client by lazy {
        createOkHttpClient(applicationContext, interceptorTypeSelector)
    }

    private val httpTasks by lazy {
        listOf(HttpBinHttpTask(client), DummyImageHttpTask(client), PostmanEchoHttpTask(client))
    }

    private val applicationName: CharSequence
        get() = applicationInfo.loadLabel(packageManager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainBinding = ActivityMainSampleBinding.inflate(layoutInflater)

        with(mainBinding) {
            setContentView(root)
            applyInsets()
            setSupportActionBar(toolbar)
            toolbar?.subtitle = applicationName
            doHttp.setOnClickListener {
                for (task in httpTasks) {
                    task.run()
                }
            }
            doGraphql.setOnClickListener {
                GraphQlTask(client).run()
            }

            launchChuckerDirectly.isVisible = Chucker.isOp
            launchChuckerDirectly.setOnClickListener { launchChuckerDirectly() }

            exportToFile.isVisible = Chucker.isOp
            exportToFile.setOnClickListener {
                generateExportFile(ExportFormat.LOG)
            }

            exportToFileHar.isVisible = Chucker.isOp
            exportToFileHar.setOnClickListener {
                generateExportFile(ExportFormat.HAR)
            }

            interceptorTypeLabel.movementMethod = LinkMovementMethod.getInstance()
            useApplicationInterceptor.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    interceptorTypeSelector.value = InterceptorType.APPLICATION
                }
            }
            useNetworkInterceptor.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    interceptorTypeSelector.value = InterceptorType.NETWORK
                }
            }
        }

        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build(),
        )

        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .penaltyLog()
                .penaltyDeath()
                .build(),
        )
    }

    private fun applyInsets() {
        // Set up window insets to properly handle the UI around system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Apply insets to the main content to avoid overlap with system bars
        ViewCompat.setOnApplyWindowInsetsListener(mainBinding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            mainBinding.appBarLayout?.updatePadding(top = insets.top)
            view.updatePadding(bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
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
                Toast.makeText(applicationContext, R.string.export_to_file_failure, Toast.LENGTH_SHORT).show()
            } else {
                val successMessage = applicationContext.getString(R.string.export_to_file_success, uri.path)
                Toast.makeText(applicationContext, successMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
