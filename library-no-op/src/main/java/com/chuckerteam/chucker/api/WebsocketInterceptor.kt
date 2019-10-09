package com.chuckerteam.chucker.api

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

/**
 * No-op implementation.
 */
fun OkHttpClient.newLoggedWebSocket(request: Request, listener: WebSocketListener): WebSocket =
    newWebSocket(request, listener)
