package com.chuckerteam.chucker.sample

import com.chuckerteam.chucker.sample.HttpBinHttpTask.Api.Data
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

class HttpBinHttpTask(
    client: OkHttpClient,
) : HttpTask {
    private val api = Retrofit.Builder()
        .baseUrl("https://httpbin.org")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
        .create<Api>()

    private val noOpCallback = object : Callback<Any?> {
        override fun onResponse(call: Call<Any?>, response: Response<Any?>) = Unit

        override fun onFailure(call: Call<Any?>, t: Throwable) {
            t.printStackTrace()
        }
    }

    @Suppress("MagicNumber")
    override fun run() = with(api) {
        get().enqueue(noOpCallback)
        post(Data("posted")).enqueue(noOpCallback)
        patch(Data("patched")).enqueue(noOpCallback)
        put(Data("put")).enqueue(noOpCallback)
        delete().enqueue(noOpCallback)
        status(201).enqueue(noOpCallback)
        status(401).enqueue(noOpCallback)
        status(500).enqueue(noOpCallback)
        delay(9).enqueue(noOpCallback)
        delay(15).enqueue(noOpCallback)
        redirectTo("https://http2.akamai.com").enqueue(noOpCallback)
        redirect(3).enqueue(noOpCallback)
        redirectRelative(2).enqueue(noOpCallback)
        redirectAbsolute(4).enqueue(noOpCallback)
        stream(500).enqueue(noOpCallback)
        streamBytes(2048).enqueue(noOpCallback)
        image("image/png").enqueue(noOpCallback)
        brotliResponse().enqueue(noOpCallback)
        gzipResponse().enqueue(noOpCallback)
        gzipRequest(Data("Some gzip request")).enqueue(noOpCallback)
        xml().enqueue(noOpCallback)
        utf8().enqueue(noOpCallback)
        deflate().enqueue(noOpCallback)
        cookieSet("v").enqueue(noOpCallback)
        basicAuth("me", "pass").enqueue(noOpCallback)
        drip(512, 10, 1, 200).enqueue(noOpCallback)
        deny().enqueue(noOpCallback)
        cache("Mon").enqueue(noOpCallback)
        cache(30).enqueue(noOpCallback)
        redirectTo("https://ascii.cl?parameter=%22Click+on+%27URL+Encode%27%21%22").enqueue(noOpCallback)
        redirectTo("https://ascii.cl?parameter=\"Click on 'URL Encode'!\"").enqueue(noOpCallback)
        postForm("Value 1", "Value with symbols &$%").enqueue(noOpCallback)
        postRawRequestBody(oneShotRequestBody()).enqueue(noOpCallback)
    }

    private fun oneShotRequestBody() = object : RequestBody() {
        private val content = Buffer().writeUtf8("Hello, world!")
        override fun isOneShot() = true
        override fun contentType() = "text/plain".toMediaType()
        override fun writeTo(sink: BufferedSink) {
            content.readAll(sink)
        }
    }

    @Suppress("TooManyFunctions")
    private interface Api {
        @GET("/get")
        fun get(): Call<Any?>

        @POST("/post")
        fun post(@Body body: Data): Call<Any?>

        @PATCH("/patch")
        fun patch(@Body body: Data): Call<Any?>

        @PUT("/put")
        fun put(@Body body: Data): Call<Any?>

        @DELETE("/delete")
        fun delete(): Call<Any?>

        @GET("/status/{code}")
        fun status(@Path("code") code: Int): Call<Any?>

        @GET("/stream/{lines}")
        fun stream(@Path("lines") lines: Int): Call<Any?>

        @GET("/stream-bytes/{bytes}")
        fun streamBytes(@Path("bytes") bytes: Int): Call<Any?>

        @GET("/delay/{seconds}")
        fun delay(@Path("seconds") seconds: Int): Call<Any?>

        @GET("/bearer")
        fun bearer(@Header("Authorization") token: String): Call<Any?>

        @GET("/redirect-to")
        fun redirectTo(@Query("url") url: String): Call<Any?>

        @GET("/redirect/{times}")
        fun redirect(@Path("times") times: Int): Call<Any?>

        @GET("/relative-redirect/{times}")
        fun redirectRelative(@Path("times") times: Int): Call<Any?>

        @GET("/absolute-redirect/{times}")
        fun redirectAbsolute(@Path("times") times: Int): Call<Any?>

        @GET("/image")
        fun image(@Header("Accept") accept: String): Call<Any?>

        @GET("/brotli")
        @Headers("Accept-Encoding: br")
        fun brotliResponse(): Call<Any?>

        @GET("/gzip")
        @Headers("Accept-Encoding: gzip")
        fun gzipResponse(): Call<Any?>

        @POST("/post")
        @Headers("Content-Encoding: gzip")
        fun gzipRequest(@Body body: Data): Call<Any?>

        @GET("/xml")
        fun xml(): Call<Any?>

        @GET("/encoding/utf8")
        fun utf8(): Call<Any?>

        @GET("/deflate")
        fun deflate(): Call<Any?>

        @GET("/cookies/set")
        fun cookieSet(@Query("k1") value: String): Call<Any?>

        @GET("/basic-auth/{user}/{passwd}")
        fun basicAuth(
            @Path("user") user: String,
            @Path("passwd") passwd: String
        ): Call<Any?>

        @GET("/drip")
        fun drip(
            @Query("numbytes") bytes: Int,
            @Query("duration") seconds: Int,
            @Query("delay") delay: Int,
            @Query("code") code: Int
        ): Call<Any?>

        @GET("/deny")
        fun deny(): Call<Any?>

        @GET("/cache")
        fun cache(@Header("If-Modified-Since") ifModifiedSince: String): Call<Any?>

        @GET("/cache/{seconds}")
        fun cache(@Path("seconds") seconds: Int): Call<Any?>

        @FormUrlEncoded
        @POST("/post")
        fun postForm(@Field("key1") value1: String, @Field("key2") value2: String): Call<Any?>

        @POST("/post")
        fun postRawRequestBody(@Body body: RequestBody): Call<Any?>

        class Data(val thing: String)
    }
}
