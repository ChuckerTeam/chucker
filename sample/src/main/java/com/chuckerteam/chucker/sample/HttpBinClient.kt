package com.chuckerteam.chucker.sample

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.chuckerteam.chucker.sample.HttpBinApi.Data
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

private const val BASE_URL = "https://httpbin.org"
private const val SEGMENT_SIZE = 8_192L

class HttpBinClient(
    context: Context
) {

    private val collector = ChuckerCollector(
        context = context,
        showNotification = true,
        retentionPeriod = RetentionManager.Period.ONE_HOUR
    )

    private val chuckerInterceptor = ChuckerInterceptor.Builder(context)
        .collector(collector)
        .maxContentLength(250000L)
        .redactHeaders(emptySet())
        .build()

    private val httpClient =
        OkHttpClient.Builder()
            // Add a ChuckerInterceptor instance to your OkHttp client
            .addInterceptor(chuckerInterceptor)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()

    private val api: HttpBinApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
            .create(HttpBinApi::class.java)
    }

    @Suppress("MagicNumber")
    internal fun doHttpActivity() {
        val cb = object : Callback<Any?> {
            override fun onResponse(call: Call<Any?>, response: Response<Any?>) = Unit

            override fun onFailure(call: Call<Any?>, t: Throwable) {
                t.printStackTrace()
            }
        }

        with(api) {
            get().enqueue(cb)
            post(Data("posted")).enqueue(cb)
            patch(Data("patched")).enqueue(cb)
            put(Data("put")).enqueue(cb)
            delete().enqueue(cb)
            status(201).enqueue(cb)
            status(401).enqueue(cb)
            status(500).enqueue(cb)
            delay(9).enqueue(cb)
            delay(15).enqueue(cb)
            redirectTo("https://http2.akamai.com").enqueue(cb)
            redirect(3).enqueue(cb)
            redirectRelative(2).enqueue(cb)
            redirectAbsolute(4).enqueue(cb)
            stream(500).enqueue(cb)
            streamBytes(2048).enqueue(cb)
            image("image/png").enqueue(cb)
            gzipResponse().enqueue(cb)
            gzipRequest(Data("Some gzip request")).enqueue(cb)
            xml().enqueue(cb)
            utf8().enqueue(cb)
            deflate().enqueue(cb)
            cookieSet("v").enqueue(cb)
            basicAuth("me", "pass").enqueue(cb)
            drip(512, 10, 1, 200).enqueue(cb)
            deny().enqueue(cb)
            cache("Mon").enqueue(cb)
            cache(30).enqueue(cb)
            redirectTo("https://ascii.cl?parameter=%22Click+on+%27URL+Encode%27%21%22").enqueue(cb)
            redirectTo("https://ascii.cl?parameter=\"Click on 'URL Encode'!\"").enqueue(cb)
            postForm("Value 1", "Value with symbols &$%").enqueue(cb)
        }
        downloadSampleImage(colorHex = "fff")
        downloadSampleImage(colorHex = "000")
        getResponsePartially()
    }

    private fun downloadSampleImage(colorHex: String) {
        val request = Request.Builder()
            .url("https://dummyimage.com/200x200/$colorHex/$colorHex.png")
            .get()
            .build()
        httpClient.newCall(request).enqueue(
            object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) = Unit

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    response.body?.source()?.use { it.readByteString() }
                }
            }
        )
    }

    private fun getResponsePartially() {
        val body = LARGE_JSON.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://postman-echo.com/post")
            .post(body)
            .build()
        httpClient.newCall(request).enqueue(
            object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) = Unit

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    response.body?.source()?.use { it.readByteString(SEGMENT_SIZE) }
                }
            }
        )
    }
}
