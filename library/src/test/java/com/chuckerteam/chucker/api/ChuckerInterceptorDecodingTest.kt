package com.chuckerteam.chucker.api

import com.chuckerteam.chucker.util.ChuckerInterceptorDelegate
import com.chuckerteam.chucker.util.ClientFactory
import com.chuckerteam.chucker.util.NoLoggerRule
import com.chuckerteam.chucker.util.readByteStringBody
import com.chuckerteam.chucker.util.toServerRequest
import com.google.common.truth.Truth.assertThat
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import okio.ByteString
import okio.IOException
import org.junit.Rule
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.io.File

@ExtendWith(NoLoggerRule::class)
internal class ChuckerInterceptorDecodingTest {
    @get:Rule val server = MockWebServer()

    private val serverUrl = server.url("/") // Starts server implicitly

    @TempDir lateinit var tempDir: File

    @ParameterizedTest
    @EnumSource
    fun `custom body decoder does not change request body`(factory: ClientFactory) {
        val chuckerInterceptor = ChuckerInterceptorDelegate(
            cacheDirectoryProvider = { tempDir },
            decoders = listOf(ReversingDecoder()),
        )
        val client = factory.create(chuckerInterceptor)
        server.enqueue(MockResponse())

        val request = "Hello, world!".toRequestBody().toServerRequest(serverUrl)
        client.newCall(request).execute().readByteStringBody()
        val serverRequestContent = server.takeRequest().body.readByteString()

        assertThat(serverRequestContent.utf8()).isEqualTo("Hello, world!")
    }

    @ParameterizedTest
    @EnumSource
    fun `custom body decoder does not change response body`(factory: ClientFactory) {
        val chuckerInterceptor = ChuckerInterceptorDelegate(
            cacheDirectoryProvider = { tempDir },
            decoders = listOf(ReversingDecoder()),
        )
        val client = factory.create(chuckerInterceptor)

        val body = Buffer().writeUtf8("Hello, world!")
        server.enqueue(MockResponse().setBody(body))
        val request = Request.Builder().url(serverUrl).build()

        val responseBody = client.newCall(request).execute().readByteStringBody()!!

        assertThat(responseBody.utf8()).isEqualTo("Hello, world!")
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `custom body decoder is used for decoding`(factory: ClientFactory) {
        val chuckerInterceptor = ChuckerInterceptorDelegate(
            cacheDirectoryProvider = { tempDir },
            decoders = listOf(LiteralBodyDecoder()),
        )
        val client = factory.create(chuckerInterceptor)
        val request = Request.Builder().url(serverUrl)
            .post("Hello".toRequestBody())
            .build()
        server.enqueue(MockResponse().setBody("Goodbye"))

        client.newCall(request).execute().readByteStringBody()

        val transaction = chuckerInterceptor.expectTransaction()

        assertThat(transaction.requestBody).isEqualTo("Request")
        assertThat(transaction.responseBody).isEqualTo("Response")
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `body decoders are used in the applied order`(factory: ClientFactory) {
        val chuckerInterceptor = ChuckerInterceptorDelegate(
            cacheDirectoryProvider = { tempDir },
            decoders = listOf(ReversingDecoder(), LiteralBodyDecoder()),
        )
        val client = factory.create(chuckerInterceptor)
        val request = Request.Builder().url(serverUrl)
            .post("Hello".toRequestBody())
            .build()
        server.enqueue(MockResponse().setBody("Goodbye"))

        client.newCall(request).execute().readByteStringBody()

        val transaction = chuckerInterceptor.expectTransaction()

        assertThat(transaction.requestBody).isEqualTo("olleH")
        assertThat(transaction.responseBody).isEqualTo("eybdooG")
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `next body decoder is used when previous one does not decode`(factory: ClientFactory) {
        val chuckerInterceptor = ChuckerInterceptorDelegate(
            cacheDirectoryProvider = { tempDir },
            decoders = listOf(NoOpDecoder(), LiteralBodyDecoder()),
        )
        val client = factory.create(chuckerInterceptor)
        val request = Request.Builder().url(serverUrl)
            .post("Hello".toRequestBody())
            .build()
        server.enqueue(MockResponse().setBody("Goodbye"))

        client.newCall(request).execute().readByteStringBody()

        val transaction = chuckerInterceptor.expectTransaction()

        assertThat(transaction.requestBody).isEqualTo("Request")
        assertThat(transaction.responseBody).isEqualTo("Response")
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `body decoder can throw IO exception`(factory: ClientFactory) {
        val chuckerInterceptor = ChuckerInterceptorDelegate(
            cacheDirectoryProvider = { tempDir },
            decoders = listOf(IoThrowingDecoder(), LiteralBodyDecoder()),
        )
        val client = factory.create(chuckerInterceptor)
        val request = Request.Builder().url(serverUrl)
            .post("Hello".toRequestBody())
            .build()
        server.enqueue(MockResponse().setBody("Goodbye"))

        client.newCall(request).execute().readByteStringBody()

        val transaction = chuckerInterceptor.expectTransaction()

        assertThat(transaction.requestBody).isEqualTo("Request")
        assertThat(transaction.responseBody).isEqualTo("Response")
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `body decoders are applied lazily`(factory: ClientFactory) {
        val statefulDecoder = StatefulDecoder()
        val chuckerInterceptor = ChuckerInterceptorDelegate(
            cacheDirectoryProvider = { tempDir },
            decoders = listOf(LiteralBodyDecoder(), statefulDecoder),
        )
        val client = factory.create(chuckerInterceptor)
        val request = Request.Builder().url(serverUrl)
            .post("Hello".toRequestBody())
            .build()
        server.enqueue(MockResponse().setBody("Goodbye"))

        client.newCall(request).execute().readByteStringBody()

        assertThat(statefulDecoder.didDecodeRequest).isFalse()
        assertThat(statefulDecoder.didDecodeResponse).isFalse()
    }

    private class LiteralBodyDecoder : BodyDecoder {
        override fun decodeRequest(request: Request, body: ByteString) = "Request"
        override fun decodeResponse(response: Response, body: ByteString) = "Response"
    }

    private class ReversingDecoder : BodyDecoder {
        override fun decodeRequest(request: Request, body: ByteString) = body.utf8().reversed()
        override fun decodeResponse(response: Response, body: ByteString) = body.utf8().reversed()
    }

    private class NoOpDecoder : BodyDecoder {
        override fun decodeRequest(request: Request, body: ByteString): String? = null
        override fun decodeResponse(response: Response, body: ByteString): String? = null
    }

    private class IoThrowingDecoder : BodyDecoder {
        override fun decodeRequest(request: Request, body: ByteString) = throw IOException("Request")
        override fun decodeResponse(response: Response, body: ByteString) = throw IOException("Response")
    }

    private class StatefulDecoder : BodyDecoder {
        var didDecodeRequest = false

        override fun decodeRequest(request: Request, body: ByteString): String {
            didDecodeRequest = true
            return ""
        }

        var didDecodeResponse = false

        override fun decodeResponse(response: Response, body: ByteString): String {
            didDecodeResponse = true
            return ""
        }
    }
}
