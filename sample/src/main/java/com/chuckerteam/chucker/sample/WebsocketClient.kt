package com.chuckerteam.chucker.sample

import com.chuckerteam.chucker.api.newLoggedWebSocket
import com.chuckerteam.chucker.sample.OkHttpClientFactory.httpClient
import com.google.gson.Gson
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class WebsocketClient {
    companion object {
        const val TERMINATION_CODE = 4321
    }

    private var coinbaseSocket: WebSocket? = null

    fun start() {
        stop()

        coinbaseSocket = coinbaseSocket().also {
            // Subscribe to get Bitcoin ticker updates
            it.send("{ \"type\": \"subscribe\", \"product_ids\": [\"BTC-USD\"], \"channels\": [\"ticker\"] }")
        }

        val gson = Gson()
        with(createEchoSocket(true)) {
            send("")
            send("{}")
            send(gson.toJson(HttpBinApi.Data("message")))
            close(TERMINATION_CODE, "Because I said so")
        }

        with(createEchoSocket(false)) {
            send("")
            send("{}")
            send(gson.toJson(HttpBinApi.Data("message")))
            close(TERMINATION_CODE, "Because I said so")
        }
    }

    fun stop() {
        if (coinbaseSocket != null) {
            coinbaseSocket?.close(TERMINATION_CODE, "Thanks!")
            coinbaseSocket = null
        }
    }

    private fun coinbaseSocket(): WebSocket {
        return httpClient.newLoggedWebSocket(
            Request.Builder().url("wss://ws-feed.pro.coinbase.com").build(),
            object : WebSocketListener() {}
        )
    }

    private fun createEchoSocket(secure: Boolean): WebSocket {
        val scheme = if (secure) "wss:" else "ws:"
        return httpClient.newLoggedWebSocket(
            Request.Builder().url("$scheme//echo.websocket.org").build(),
            object : WebSocketListener() {}
        )
    }
}
