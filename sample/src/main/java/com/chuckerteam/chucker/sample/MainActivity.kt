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
        with(createEchoSocket(okHttpClient)) {
            send("")
            send("{}")
            send(com.google.gson.Gson().toJson(SampleApiService.Data("message")))
            close(4321, "Because I said so")
        }

        client.initializeCrashHandler()
    }

    private fun createEchoSocket(okHttpClient: OkHttpClient): WebSocket {
        val listener = object : WebSocketListener() {}

        return okHttpClient.newLoggedWebSocket(
            Request.Builder().url("ws://echo.websocket.org").build(),
            listener
        )
    }

    private fun launchChuckerDirectly() {
        // Optionally launch Chucker directly from your own app UI
        startActivity(Chucker.getLaunchIntent(this, Chucker.SCREEN_HTTP))
    }
}



