package com.chuckerteam.chucker.sample

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import okio.IOException

const val SEGMENT_SIZE = 8_192L

fun createOkHttpClient(
    context: Context,
    interceptorTypeProvider: InterceptorType.Provider,
): OkHttpClient {
    val collector = ChuckerCollector(
        context = context,
        showNotification = true,
        retentionPeriod = RetentionManager.Period.ONE_HOUR
    )

    @Suppress("MagicNumber")
    val chuckerInterceptor = ChuckerInterceptor.Builder(context)
        .collector(collector)
        .maxContentLength(250_000L)
        .redactHeaders(emptySet())
        .alwaysReadResponseBody(false)
        .addBodyDecoder(PokemonProtoBodyDecoder())
        .build()

    return OkHttpClient.Builder()
        // Add a ChuckerInterceptor instance to your OkHttp client as an application or a network interceptor.
        // Learn more about interceptor types here – https://square.github.io/okhttp/interceptors.
        // "activeForType" is needed only in this sample to control it from the UI.
        .addInterceptor(chuckerInterceptor.activeForType(InterceptorType.APPLICATION, interceptorTypeProvider))
        .addNetworkInterceptor(chuckerInterceptor.activeForType(InterceptorType.NETWORK, interceptorTypeProvider))
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .build()
}

class ReadBytesCallback(
    private val byteCount: Long? = null,
) : Callback {
    override fun onFailure(call: Call, e: IOException) {
        e.printStackTrace()
    }

    override fun onResponse(call: Call, response: Response) {
        response.body?.source()?.use {
            try {
                if (byteCount == null) {
                    it.readByteString()
                } else {
                    it.readByteString(byteCount)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
