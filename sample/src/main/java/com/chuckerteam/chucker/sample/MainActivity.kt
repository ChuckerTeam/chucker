package com.chuckerteam.chucker.sample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.chuckerteam.chucker.api.Chucker
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var httpBinClient: HttpBinClient
    private lateinit var websocketClient: WebsocketClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ChuckerInterceptorFactory.init(applicationContext)
        httpBinClient = HttpBinClient()
        websocketClient = WebsocketClient()

        do_http.setOnClickListener {
            httpBinClient.doHttpActivity()
            websocketClient.start()
        }

        trigger_exception.setOnClickListener { httpBinClient.recordException() }

        with(launch_chucker_directly) {
            visibility = if (Chucker.isOp) View.VISIBLE else View.GONE
            setOnClickListener { launchChuckerDirectly() }
        }

        httpBinClient.initializeCrashHandler()
    }

    override fun onStop() {
        super.onStop()
        websocketClient.stop()
    }

    private fun launchChuckerDirectly() {
        // Optionally launch Chucker directly from your own app UI
        startActivity(Chucker.getLaunchIntent(this, Chucker.SCREEN_HTTP))
    }
}
