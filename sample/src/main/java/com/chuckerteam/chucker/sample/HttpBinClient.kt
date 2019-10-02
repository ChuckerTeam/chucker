package com.chuckerteam.chucker.sample

import com.chuckerteam.chucker.api.Chucker
import com.chuckerteam.chucker.sample.ChuckerInterceptorFactory.Companion.collector
import com.chuckerteam.chucker.sample.HttpBinApi.Data
import com.chuckerteam.chucker.sample.OkHttpClientFactory.httpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://httpbin.org"

class HttpBinClient {
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
        val cb = object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                // no-op
            }
            override fun onFailure(call: Call<Void>, t: Throwable) { t.printStackTrace() }
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
            gzip().enqueue(cb)
            xml().enqueue(cb)
            utf8().enqueue(cb)
            deflate().enqueue(cb)
            cookieSet("v").enqueue(cb)
            basicAuth("me", "pass").enqueue(cb)
            drip(512, 5, 1, 200).enqueue(cb)
            deny().enqueue(cb)
            cache("Mon").enqueue(cb)
            cache(30).enqueue(cb)
        }
    }

    internal fun initializeCrashHandler() {
        Chucker.registerDefaultCrashHandler(collector)
    }

    internal fun recordException() {
        collector.onError("Example button pressed", RuntimeException("User triggered the button"))
        // You can also throw exception, it will be caught thanks to "Chucker.registerDefaultCrashHandler"
        // throw new RuntimeException("User triggered the button");
    }
}
