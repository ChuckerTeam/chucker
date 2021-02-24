package com.chuckerteam.chucker.sample

import okhttp3.OkHttpClient
import okhttp3.Request

class DummyImageHttpTask(
    private val client: OkHttpClient,
) : HttpTask {
    override fun run() {
        getImage(colorHex = "fff")
        getImage(colorHex = "000")
    }

    private fun getImage(colorHex: String) {
        val request = Request.Builder()
            .url("https://dummyimage.com/200x200/$colorHex/$colorHex.png")
            .get()
            .build()
        client.newCall(request).enqueue(ReadBytesCallback())
    }
}
