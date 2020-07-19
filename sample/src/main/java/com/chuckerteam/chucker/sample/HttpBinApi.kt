package com.chuckerteam.chucker.sample

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

@Suppress("TooManyFunctions")
internal interface HttpBinApi {
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

    @GET("/gzip")
    fun gzip(): Call<Any?>

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

    class Data(val thing: String)
}
