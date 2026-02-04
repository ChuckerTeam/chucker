package com.chuckerteam.chucker.sample

import android.content.Context
import com.google.android.gms.net.CronetProviderInstaller
import com.google.net.cronet.okhttptransport.CronetInterceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.chromium.net.CronetEngine
import java.net.URI

private val CRONET_BASE_URI = URI("https://cloudflare-quic.com/")
private const val HTTP_CACHE_SIZE = 1 * 1024 * 1024L // 1MB
private const val API_CALL_REPEAT_VALUE = 5
private const val QUIC_PROTOCOL_SERVER_PORT = 443

class CronetHttpTask(
    private val context: Context,
    private val baseClient: OkHttpClient,
) : HttpTask {
    private var cronetClient: OkHttpClient? = null

    init {
        initializeCronetClient()
    }

    override fun run() {
        repeat(API_CALL_REPEAT_VALUE) {
            getQuicRequest()
        }
    }

    private fun initializeCronetClient() {
        CronetProviderInstaller
            .installProvider(context)
            .addOnSuccessListener {
                val cronetEngine =
                    CronetEngine
                        .Builder(context)
                        .enableQuic(true)
                        .addQuicHint(
                            CRONET_BASE_URI.host,
                            QUIC_PROTOCOL_SERVER_PORT,
                            QUIC_PROTOCOL_SERVER_PORT,
                        ).enableHttpCache(CronetEngine.Builder.HTTP_CACHE_IN_MEMORY, HTTP_CACHE_SIZE)
                        .build()
                cronetClient =
                    baseClient
                        .newBuilder()
                        .addInterceptor(CronetInterceptor.newBuilder(cronetEngine).build())
                        .build()
            }.addOnFailureListener {
                cronetClient = baseClient
            }
    }

    private fun getQuicRequest() {
        val request =
            Request
                .Builder()
                .url("https://cloudflare-quic.com/")
                .get()
                .build()
        val client = cronetClient ?: baseClient
        client.newCall(request).enqueue(ReadBytesCallback())
    }
}
