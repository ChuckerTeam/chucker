package com.chuckerteam.chucker.api

import com.chuckerteam.chucker.util.ChuckerInterceptorDelegate
import com.chuckerteam.chucker.util.ClientFactory
import com.chuckerteam.chucker.util.NoLoggerRule
import com.chuckerteam.chucker.util.SEGMENT_SIZE
import com.chuckerteam.chucker.util.getResourceFile
import com.chuckerteam.chucker.util.readByteStringBody
import com.chuckerteam.chucker.util.toServerRequest
import com.google.common.collect.Range
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.stream.JsonReader
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import okio.Buffer
import okio.BufferedSink
import okio.ByteString
import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.encodeUtf8
import okio.GzipSink
import okio.buffer
import org.junit.Rule
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.io.File
import java.net.HttpURLConnection.HTTP_NO_CONTENT

@ExtendWith(NoLoggerRule::class)
internal class ChuckerInterceptorTest {
    @get:Rule
    val server = MockWebServer()

    private val serverUrl = server.url("/") // Starts server implicitly

    @TempDir
    lateinit var tempDir: File
    private val chuckerInterceptor =
        ChuckerInterceptorDelegate(cacheDirectoryProvider = { tempDir })

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `image response body is available to Chucker`(factory: ClientFactory) {
        val image = getResourceFile("sample_image.png")
        server.enqueue(MockResponse().addHeader("Content-Type: image/jpeg").setBody(image))
        val request = Request.Builder().url(serverUrl).build()
        val expectedBody = image.snapshot()

        val client = factory.create(chuckerInterceptor)
        client.newCall(request).execute().readByteStringBody()
        val responseBody =
            ByteString.of(*chuckerInterceptor.expectTransaction().responseImageData!!)

        assertThat(responseBody).isEqualTo(expectedBody)
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `image response body is available to consumer`(factory: ClientFactory) {
        val image = getResourceFile("sample_image.png")
        server.enqueue(MockResponse().addHeader("Content-Type: image/jpeg").setBody(image))
        val request = Request.Builder().url(serverUrl).build()
        val expectedBody = image.snapshot()

        val client = factory.create(chuckerInterceptor)
        val responseBody = client.newCall(request).execute().readByteStringBody()!!

        assertThat(responseBody).isEqualTo(expectedBody)
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `gzipped response body is gunzipped for Chucker`(factory: ClientFactory) {
        val bytes = Buffer().writeUtf8("Hello, world!")
        val gzippedBytes = Buffer().apply {
            GzipSink(this).use { sink -> sink.write(bytes, bytes.size) }
        }
        server.enqueue(MockResponse().addHeader("Content-Encoding: gzip").setBody(gzippedBytes))
        val request = Request.Builder().url(serverUrl).build()

        val client = factory.create(chuckerInterceptor)
        client.newCall(request).execute().readByteStringBody()
        val transaction = chuckerInterceptor.expectTransaction()

        assertThat(transaction.isRequestBodyEncoded).isFalse()
        assertThat(transaction.responseBody).isEqualTo("Hello, world!")
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `gzipped response body is gunzipped for consumer`(factory: ClientFactory) {
        val bytes = Buffer().writeUtf8("Hello, world!")
        val gzippedBytes = Buffer().apply {
            GzipSink(this).use { sink -> sink.write(bytes, bytes.size) }
        }
        server.enqueue(MockResponse().addHeader("Content-Encoding: gzip").setBody(gzippedBytes))
        val request = Request.Builder().url(serverUrl).build()

        val client = factory.create(chuckerInterceptor)
        val responseBody = client.newCall(request).execute().readByteStringBody()!!

        assertThat(responseBody.utf8()).isEqualTo("Hello, world!")
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `compressed response body without content is transparent to Chucker`(factory: ClientFactory) {
        server.enqueue(
            MockResponse().addHeader("Content-Encoding: gzip").setResponseCode(HTTP_NO_CONTENT)
        )
        val request = Request.Builder().url(serverUrl).build()

        val client = factory.create(chuckerInterceptor)
        client.newCall(request).execute().readByteStringBody()
        val transaction = chuckerInterceptor.expectTransaction()

        assertThat(transaction.responseBody).isNull()
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `compressed response body without content is transparent to consumer`(factory: ClientFactory) {
        server.enqueue(
            MockResponse().addHeader("Content-Encoding: br").setResponseCode(HTTP_NO_CONTENT)
        )
        val request = Request.Builder().url(serverUrl).build()

        val client = factory.create(chuckerInterceptor)
        val responseBody = client.newCall(request).execute().readByteStringBody()

        assertThat(responseBody).isNull()
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `brotli response body is uncompressed for Chucker`(factory: ClientFactory) {
        val brotliEncodedString =
            "1bce00009c05ceb9f028d14e416230f718960a537b0922d2f7b6adef56532c08dff44551516690131494db" +
                "6021c7e3616c82c1bc2416abb919aaa06e8d30d82cc2981c2f5c900bfb8ee29d5c03deb1c0dacff80e" +
                "abe82ba64ed250a497162006824684db917963ecebe041b352a3e62d629cc97b95cac24265b175171e" +
                "5cb384cd0912aeb5b5dd9555f2dd1a9b20688201"

        val brotliSource = Buffer().write(brotliEncodedString.decodeHex())

        server.enqueue(MockResponse().addHeader("Content-Encoding: br").setBody(brotliSource))
        val request = Request.Builder().url(serverUrl).build()

        val client = factory.create(chuckerInterceptor)
        client.newCall(request).execute().readByteStringBody()
        val transaction = chuckerInterceptor.expectTransaction()

        assertThat(transaction.isRequestBodyEncoded).isFalse()
        assertThat(transaction.responseBody).contains("\"brotli\": true")
        assertThat(transaction.responseBody).contains("\"Accept-Encoding\": \"br\"")
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `brotli response body is not changed for consumer`(factory: ClientFactory) {
        val brotliEncodedString =
            "1bce00009c05ceb9f028d14e416230f718960a537b0922d2f7b6adef56532c08dff44551516690131494db" +
                "6021c7e3616c82c1bc2416abb919aaa06e8d30d82cc2981c2f5c900bfb8ee29d5c03deb1c0dacff80e" +
                "abe82ba64ed250a497162006824684db917963ecebe041b352a3e62d629cc97b95cac24265b175171e" +
                "5cb384cd0912aeb5b5dd9555f2dd1a9b20688201"

        val brotliSource = Buffer().write(brotliEncodedString.decodeHex())

        server.enqueue(MockResponse().addHeader("Content-Encoding: br").setBody(brotliSource))
        val request = Request.Builder().url(serverUrl).build()

        val client = factory.create(chuckerInterceptor)
        val responseBody = client.newCall(request).execute().readByteStringBody()!!

        assertThat(responseBody.hex()).isEqualTo(brotliEncodedString)
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `plain text response body is available to Chucker`(factory: ClientFactory) {
        val body = Buffer().writeUtf8("Hello, world!")
        server.enqueue(MockResponse().setBody(body))
        val request = Request.Builder().url(serverUrl).build()

        val client = factory.create(chuckerInterceptor)
        client.newCall(request).execute().readByteStringBody()
        val transaction = chuckerInterceptor.expectTransaction()

        assertThat(transaction.isRequestBodyEncoded).isFalse()
        assertThat(transaction.responseBody).isEqualTo("Hello, world!")
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `plain text response body is available to consumer`(factory: ClientFactory) {
        val body = Buffer().writeUtf8("Hello, world!")
        server.enqueue(MockResponse().setBody(body))
        val request = Request.Builder().url(serverUrl).build()

        val client = factory.create(chuckerInterceptor)
        val responseBody = client.newCall(request).execute().readByteStringBody()!!

        assertThat(responseBody.utf8()).isEqualTo("Hello, world!")
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `response body without content is transparent to Chucker`(factory: ClientFactory) {
        server.enqueue(MockResponse().setResponseCode(HTTP_NO_CONTENT))
        val request = Request.Builder().url(serverUrl).build()

        val client = factory.create(chuckerInterceptor)
        client.newCall(request).execute().readByteStringBody()
        val transaction = chuckerInterceptor.expectTransaction()

        assertThat(transaction.responseBody).isNull()
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `response body without content is transparent to consumer`(factory: ClientFactory) {
        server.enqueue(MockResponse().setResponseCode(HTTP_NO_CONTENT))
        val request = Request.Builder().url(serverUrl).build()

        val client = factory.create(chuckerInterceptor)
        val responseBody = client.newCall(request).execute().readByteStringBody()

        assertThat(responseBody).isNull()
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `response payload size depends on downloaded byte count`(factory: ClientFactory) {
        val body = Buffer().apply {
            repeat(10 * SEGMENT_SIZE.toInt()) { writeUtf8("!") }
        }
        server.enqueue(MockResponse().setBody(body))
        val request = Request.Builder().url(serverUrl).build()

        val client = factory.create(chuckerInterceptor)
        client.newCall(request).execute().readByteStringBody(SEGMENT_SIZE)

        val transaction = chuckerInterceptor.expectTransaction()
        // We cannot expect exact amount of data as there are no guarantees that client
        // will read from the source exact amount of data that we requested.
        //
        // It is only best effort attempt and if we download less than 8KiB reading will continue
        // in 8KiB batches until at least 8KiB is downloaded.
        assertThat(transaction.responsePayloadSize).isIn(
            Range.closed(
                SEGMENT_SIZE,
                2 * SEGMENT_SIZE
            )
        )
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `response content length is read from a header`(factory: ClientFactory) {
        val body = Buffer().apply {
            repeat(10 * SEGMENT_SIZE.toInt()) { writeUtf8("!") }
        }
        server.enqueue(MockResponse().setBody(body))
        val request = Request.Builder().url(serverUrl).build()

        val client = factory.create(chuckerInterceptor)
        client.newCall(request).execute().readByteStringBody(SEGMENT_SIZE)

        val transaction = chuckerInterceptor.expectTransaction()
        assertThat(transaction.responseHeaders).contains(
            """
            |  {
            |    "name": "Content-Length",
            |    "value": "${body.size}"
            |  }
            """.trimMargin()
        )
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `response body length is limited by interceptor's max content length`(factory: ClientFactory) {
        val body = Buffer().apply {
            repeat(10_000) { writeUtf8("!") }
        }
        server.enqueue(MockResponse().setBody(body))
        val request = Request.Builder().url(serverUrl).build()

        val chuckerInterceptor = ChuckerInterceptorDelegate(
            cacheDirectoryProvider = { tempDir },
            maxContentLength = 1_000
        )
        val client = factory.create(chuckerInterceptor)
        client.newCall(request).execute().readByteStringBody()

        val transaction = chuckerInterceptor.expectTransaction()
        assertThat(transaction.responseBody?.length).isEqualTo(1_000)
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `response payload size is not limited by interceptor's max content length`(factory: ClientFactory) {
        val body = Buffer().apply {
            repeat(10_000) { writeUtf8("!") }
        }
        server.enqueue(MockResponse().setBody(body))
        val request = Request.Builder().url(serverUrl).build()

        val chuckerInterceptor = ChuckerInterceptorDelegate(
            cacheDirectoryProvider = { tempDir },
            maxContentLength = 1_000
        )
        val client = factory.create(chuckerInterceptor)
        client.newCall(request).execute().readByteStringBody()

        val transaction = chuckerInterceptor.expectTransaction()
        assertThat(transaction.responsePayloadSize).isEqualTo(body.size)
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `response is available to consumer if Chucker fails to save data`(factory: ClientFactory) {
        val body = Buffer().writeUtf8("Hello, world!")
        server.enqueue(MockResponse().setBody(body))
        val request = Request.Builder().url(serverUrl).build()

        val chuckerInterceptor = ChuckerInterceptorDelegate(cacheDirectoryProvider = { tempDir })
        val client = factory.create(chuckerInterceptor)
        val response = client.newCall(request).execute()

        assertThat(tempDir.deleteRecursively()).isTrue()

        val responseBody = response.readByteStringBody()!!

        assertThat(responseBody.utf8()).isEqualTo("Hello, world!")
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `transaction is available to Chucker if it fails to save data`(factory: ClientFactory) {
        val body = Buffer().writeUtf8("Hello, world!")
        server.enqueue(MockResponse().setBody(body))
        val request = Request.Builder().url(serverUrl).build()

        val chuckerInterceptor = ChuckerInterceptorDelegate(cacheDirectoryProvider = { tempDir })
        val client = factory.create(chuckerInterceptor)
        val response = client.newCall(request).execute()

        assertThat(tempDir.deleteRecursively()).isTrue()

        response.readByteStringBody()

        val transaction = chuckerInterceptor.expectTransaction()
        assertThat(transaction.responseBody).isNull()
        assertThat(transaction.responsePayloadSize).isEqualTo(body.size)
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `transaction is available to Chucker if it has no data cache`(factory: ClientFactory) {
        val body = Buffer().writeUtf8("Hello, world!")
        server.enqueue(MockResponse().setBody(body))
        val request = Request.Builder().url(serverUrl).build()

        val chuckerInterceptor = ChuckerInterceptorDelegate(cacheDirectoryProvider = { null })
        val client = factory.create(chuckerInterceptor)
        client.newCall(request).execute().readByteStringBody()

        val transaction = chuckerInterceptor.expectTransaction()
        assertThat(transaction.responseBody).isNull()
        assertThat(transaction.responsePayloadSize).isEqualTo(body.size)
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `response is available to consumer if Chucker has no data cache`(factory: ClientFactory) {
        val body = Buffer().writeUtf8("Hello, world!")
        server.enqueue(MockResponse().setBody(body))
        val request = Request.Builder().url(serverUrl).build()

        val chuckerInterceptor = ChuckerInterceptorDelegate(cacheDirectoryProvider = { null })
        val client = factory.create(chuckerInterceptor)
        val responseBody = client.newCall(request).execute().readByteStringBody()

        assertThat(responseBody!!.utf8()).isEqualTo("Hello, world!")
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `response body is available to Chucker if consumer does not read it`(factory: ClientFactory) {
        val body = Buffer().writeUtf8("Hello, world!")
        server.enqueue(MockResponse().setBody(body))
        val request = Request.Builder().url(serverUrl).build()

        val chuckerInterceptor = ChuckerInterceptorDelegate(
            cacheDirectoryProvider = { tempDir },
            alwaysReadResponseBody = true
        )
        val client = factory.create(chuckerInterceptor)
        client.newCall(request).execute().body!!.close()

        val transaction = chuckerInterceptor.expectTransaction()
        assertThat(transaction.responseBody).isEqualTo("Hello, world!")
        assertThat(transaction.responsePayloadSize).isEqualTo(body.size)
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `response body is available to Chucker if there are parsing errors`(factory: ClientFactory) {
        val providedJson =
            """
            {
              "string": "${"!".repeat(SEGMENT_SIZE.toInt())}",
              "boolean": 100,
              "secondString": "${"?".repeat(3 * SEGMENT_SIZE.toInt())}"
            }
            """.trimIndent()
        val body = Buffer().writeUtf8(providedJson)
        server.enqueue(MockResponse().setBody(body))
        val request = Request.Builder().url(serverUrl).build()

        val chuckerInterceptor = ChuckerInterceptorDelegate(
            cacheDirectoryProvider = { tempDir },
            alwaysReadResponseBody = true
        )
        val client = factory.create(chuckerInterceptor)
        val responseBody = client.newCall(request).execute().body!!

        val jsonAdapter = Gson().getAdapter(Expected::class.java)
        val jsonReader = JsonReader(responseBody.charStream())

        assertThrows<JsonParseException> {
            jsonAdapter.read(jsonReader)
        }
        responseBody.close()

        val transaction = chuckerInterceptor.expectTransaction()
        assertThat(transaction.responseBody).isEqualTo(providedJson)
        assertThat(transaction.responsePayloadSize).isEqualTo(body.size)
    }

    private data class Expected(val string: String, val boolean: Boolean, val secondString: String)

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `non plain text body is recognized`(factory: ClientFactory) {
        server.enqueue(MockResponse())
        val client = factory.create(chuckerInterceptor)

        val request = "\u0080".encodeUtf8().toRequestBody().toServerRequest(serverUrl)
        client.newCall(request).execute().body!!.close()

        val transaction = chuckerInterceptor.expectTransaction()
        assertThat(transaction.isRequestBodyEncoded).isTrue()
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `request body is available to server`(factory: ClientFactory) {
        server.enqueue(MockResponse())
        val client = factory.create(chuckerInterceptor)

        val request = "Hello, world!".toRequestBody().toServerRequest(serverUrl)
        client.newCall(request).execute().readByteStringBody()
        val serverRequestContent = server.takeRequest().body.readByteString()

        assertThat(serverRequestContent.utf8()).isEqualTo("Hello, world!")
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `plain text request body is available to Chucker`(factory: ClientFactory) {
        server.enqueue(MockResponse())
        val client = factory.create(chuckerInterceptor)

        val request = "Hello, world!".toRequestBody().toServerRequest(serverUrl)
        client.newCall(request).execute().readByteStringBody()

        val transaction = chuckerInterceptor.expectTransaction()
        assertThat(transaction.isRequestBodyEncoded).isFalse()
        assertThat(transaction.requestBody).isEqualTo("Hello, world!")
        assertThat(transaction.requestPayloadSize).isEqualTo(request.body!!.contentLength())
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `gzipped request body is gunzipped for Chucker`(factory: ClientFactory) {
        server.enqueue(MockResponse())
        val client = factory.create(chuckerInterceptor)

        val gzippedBytes = Buffer().apply {
            GzipSink(this).buffer().use { sink -> sink.writeUtf8("Hello, world!") }
        }.readByteString()
        val request = gzippedBytes.toRequestBody().toServerRequest(serverUrl)
            .newBuilder()
            .header("Content-Encoding", "gzip")
            .build()
        client.newCall(request).execute().readByteStringBody()

        val transaction = chuckerInterceptor.expectTransaction()
        assertThat(transaction.isRequestBodyEncoded).isFalse()
        assertThat(transaction.requestBody).isEqualTo("Hello, world!")
        assertThat(transaction.requestPayloadSize).isEqualTo(request.body!!.contentLength())
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `request body is limited by interceptor's max content length`(factory: ClientFactory) {
        server.enqueue(MockResponse())
        val chuckerInterceptor = ChuckerInterceptorDelegate(
            maxContentLength = SEGMENT_SIZE,
            cacheDirectoryProvider = { tempDir },
        )
        val client = factory.create(chuckerInterceptor)

        val request =
            "!".repeat(SEGMENT_SIZE.toInt() * 10).toRequestBody().toServerRequest(serverUrl)
        client.newCall(request).execute().readByteStringBody()

        val transaction = chuckerInterceptor.expectTransaction()
        assertThat(transaction.isRequestBodyEncoded).isFalse()
        assertThat(transaction.requestBody).isEqualTo(
            """
            ${"!".repeat(SEGMENT_SIZE.toInt())}

            --- Content truncated ---
            """.trimIndent()
        )
        assertThat(transaction.requestPayloadSize).isEqualTo(request.body!!.contentLength())
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `request headers are redacted in case of server failures`(factory: ClientFactory) {
        val chuckerInterceptor = ChuckerInterceptorDelegate(
            maxContentLength = SEGMENT_SIZE,
            headersToRedact = setOf("Header-To-Redact"),
            cacheDirectoryProvider = { tempDir },
        )
        val client = factory.create(chuckerInterceptor)

        val request = Request.Builder().url(serverUrl).header("Header-To-Redact", "Hello").build()
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))
        runCatching { client.newCall(request).execute() }

        val transaction = chuckerInterceptor.expectTransaction()
        assertThat(transaction.requestHeaders).contains(
            """
            |  {
            |    "name": "Header-To-Redact",
            |    "value": "**"
            |  }
            """.trimMargin()
        )
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `header sizes are computed before being redacted`(factory: ClientFactory) {
        val chuckerInterceptor = ChuckerInterceptorDelegate(
            maxContentLength = SEGMENT_SIZE,
            headersToRedact = setOf("Header-To-Redact"),
            cacheDirectoryProvider = { tempDir },
        )
        val client = factory.create(chuckerInterceptor)

        val request = Request.Builder().url(serverUrl).header("Header-To-Redact", "Hello").build()
        val call = client.newCall(request)

        server.enqueue(
            MockResponse().addHeader("Header-To-Redact", "Goodbye").setResponseCode(HTTP_NO_CONTENT)
        )
        val response = call.execute()

        val transaction = chuckerInterceptor.expectTransaction()
        val expectedResponse = when (factory) {
            ClientFactory.APPLICATION -> response
            ClientFactory.NETWORK -> response.networkResponse!!
        }
        assertThat(transaction.requestHeadersSize).isEqualTo(expectedResponse.request.headers.byteCount())
        assertThat(transaction.responseHeadersSize).isEqualTo(expectedResponse.headers.byteCount())
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `one shot request body is not available to Chucker`(factory: ClientFactory) {
        server.enqueue(MockResponse())
        val client = factory.create(chuckerInterceptor)

        val oneShotRequest = object : RequestBody() {
            private val content = Buffer().writeUtf8("Hello, world!")
            override fun isOneShot() = true
            override fun contentType() = "text/plain".toMediaType()
            override fun writeTo(sink: BufferedSink) {
                content.readAll(sink)
            }
        }.toServerRequest(serverUrl)

        client.newCall(oneShotRequest).execute().readByteStringBody()

        val transaction = chuckerInterceptor.expectTransaction()
        assertThat(transaction.requestBody).isNull()
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `one shot request body is available to server`(factory: ClientFactory) {
        server.enqueue(MockResponse())
        val client = factory.create(chuckerInterceptor)

        val oneShotRequest = object : RequestBody() {
            private val content = Buffer().writeUtf8("Hello, world!")
            override fun isOneShot() = true
            override fun contentType() = "text/plain".toMediaType()
            override fun writeTo(sink: BufferedSink) {
                content.readAll(sink)
            }
        }.toServerRequest(serverUrl)

        client.newCall(oneShotRequest).execute().readByteStringBody()
        val serverRequestContent = server.takeRequest().body.readByteString()

        assertThat(serverRequestContent.utf8()).isEqualTo("Hello, world!")
    }
}
