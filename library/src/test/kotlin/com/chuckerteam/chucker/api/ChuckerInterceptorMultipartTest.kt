package com.chuckerteam.chucker.api

import com.chuckerteam.chucker.util.ChuckerInterceptorDelegate
import com.chuckerteam.chucker.util.ClientFactory
import com.chuckerteam.chucker.util.NoLoggerRule
import com.chuckerteam.chucker.util.readByteStringBody
import com.google.common.truth.Truth.assertThat
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.io.File

@ExtendWith(NoLoggerRule::class)
internal class ChuckerInterceptorMultipartTest {
    @get:Rule
    val server = MockWebServer()

    private val serverUrl = server.url("/")

    @TempDir
    lateinit var tempDir: File

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `multipart body is formatted correctly`(factory: ClientFactory) {
        val chuckerInterceptor =
            ChuckerInterceptorDelegate(
                cacheDirectoryProvider = { tempDir },
            )
        val client = factory.create(chuckerInterceptor)

        val binaryData = byteArrayOf(0x00, 0x01, 0x02, 0x03)
        val multipartBody =
            MultipartBody
                .Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("title", "Square Logo")
                .addFormDataPart(
                    "image",
                    "logo.png",
                    binaryData.toRequestBody("image/png".toMediaType()),
                ).build()

        val request =
            Request
                .Builder()
                .url(serverUrl)
                .post(multipartBody)
                .build()
        server.enqueue(MockResponse().setBody("OK"))

        client.newCall(request).execute().readByteStringBody()

        val transaction = chuckerInterceptor.expectTransaction()

        // This assertion is what we WANT to see after the fix.
        // Current behavior will likely fail this.
        assertThat(transaction.requestBody).contains("Content-Disposition: form-data; name=\"title\"")
        assertThat(transaction.requestBody).contains("Square Logo")
        assertThat(transaction.requestBody).contains("Content-Disposition: form-data; name=\"image\"")
        assertThat(transaction.requestBody).contains("filename=\"logo.png\"")
        assertThat(transaction.requestBody).contains("Content-Type: image/png")
        // Binary content should be replaced with placeholder
        assertThat(transaction.requestBody).contains("(binary: 4 bytes)")
    }
}
