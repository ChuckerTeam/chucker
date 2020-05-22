package com.chuckerteam.chucker.sample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.chuckerteam.chucker.api.Chucker
import kotlinx.android.synthetic.main.activity_main_sample.*

class MainActivity : AppCompatActivity() {

    private val client: HttpBinClient by lazy {
        HttpBinClient(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_sample)

        do_http.setOnClickListener { client.doHttpActivity() }
        trigger_exception.setOnClickListener { client.recordException() }

        with(launch_chucker_directly) {
            visibility = if (Chucker.isOp) View.VISIBLE else View.GONE
            setOnClickListener { launchChuckerDirectly() }
        }

        client.initializeCrashHandler()
    }

    private fun launchChuckerDirectly() {
        // Optionally launch Chucker directly from your own app UI
        startActivity(Chucker.getLaunchIntent(this, Chucker.SCREEN_HTTP))
    }
}
