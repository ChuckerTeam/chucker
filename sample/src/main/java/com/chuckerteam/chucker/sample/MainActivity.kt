package com.chuckerteam.chucker.sample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.chuckerteam.chucker.api.Chucker
import com.chuckerteam.chucker.api.newLoggedWebSocket
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class MainActivity : AppCompatActivity() {

    private lateinit var client: HttpBinClient
    private var coinbaseSocket: WebSocket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        client = HttpBinClient(applicationContext)

        do_http.setOnClickListener { client.doHttpActivity() }
        trigger_exception.setOnClickListener { client.recordException() }

        with(launch_chucker_directly) {
            visibility = if (Chucker.isOp) View.VISIBLE else View.GONE
            setOnClickListener { launchChuckerDirectly() }
        }

        coinbaseSocket = coinbaseSocket(okHttpClient).apply {
            // Subscribe to get Bitcoin ticker updates
            send("{ \"type\": \"subscribe\", \"product_ids\": [\"BTC-USD\"], \"channels\": [\"ticker\"] }")
        }

        with(createEchoSocket(okHttpClient, true)) {
            send("")
            send("{}")
            send(Gson().toJson(SampleApiService.Data("message")))
            close(4321, "Because I said so")
        }

        with(createEchoSocket(okHttpClient, false)) {
            send("")
            send("{}")
            send(com.google.gson.Gson().toJson(SampleApiService.Data("message")))
            close(4321, "Because I said so")
        }

        client.initializeCrashHandler()
    }

    override fun onStop() {
        super.onStop()
        if (coinbaseSocket != null) {
            coinbaseSocket?.close(1000, "Thanks!")
            coinbaseSocket = null
        }
    }

    private fun coinbaseSocket(okHttpClient: OkHttpClient): WebSocket {
        return okHttpClient.newLoggedWebSocket(
            Request.Builder().url("wss://ws-feed.pro.coinbase.com").build(),
            object : WebSocketListener() {}
        )
    }
    private fun createEchoSocket(okHttpClient: OkHttpClient, secure: Boolean): WebSocket {
        val scheme = if (secure) "wss:" else "ws:"
        return okHttpClient.newLoggedWebSocket(
            Request.Builder().url("$scheme//echo.websocket.org").build(),
            object : WebSocketListener() {}
        )
    }

    private fun launchChuckerDirectly() {
        // Optionally launch Chucker directly from your own app UI
        startActivity(Chucker.getLaunchIntent(this, Chucker.SCREEN_HTTP))
    }
}



