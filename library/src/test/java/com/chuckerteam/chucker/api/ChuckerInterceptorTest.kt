package com.chuckerteam.chucker.api

import com.chuckerteam.chucker.ChuckerInterceptorDelegate
import com.chuckerteam.chucker.NoLoggerRule
import com.chuckerteam.chucker.SEGMENT_SIZE
import com.chuckerteam.chucker.getResourceFile
import com.chuckerteam.chucker.readByteStringBody
import com.google.common.collect.Range
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.stream.JsonReader
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import okio.Buffer
import okio.BufferedSink
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8
import okio.GzipSink
import okio.IOException
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
    enum class ClientFactory {
        APPLICATION {
            override fun create(interceptor: Interceptor): OkHttpClient {
                return OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .build()
            }
        },
        NETWORK {
            override fun create(interceptor: Interceptor): OkHttpClient {
                return OkHttpClient.Builder()
                    .addNetworkInterceptor(interceptor)
                    .build()
            }
        };

        abstract fun create(interceptor: Interceptor): OkHttpClient
    }

    @get:Rule val server = MockWebServer()

    private val serverUrl = server.url("/") // Starts server implicitly

    @TempDir lateinit var tempDir: File
    private val chuckerInterceptor = ChuckerInterceptorDelegate(cacheDirectoryProvider = { tempDir })

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun imageResponseBody_isAvailableToChucker(factory: ClientFactory) {
        val image = getResourceFile("sample_image.png")
        server.enqueue(MockResponse().addHeader("Content-Type: image/jpeg").setBody(image))
        val request = Request.Builder().url(serverUrl).build()
        val expectedBody = image.snapshot()

        val client = factory.create(chuckerInterceptor)
        client.newCall(request).execute().readByteStringBody()
        val responseBody = ByteString.of(*chuckerInterceptor.expectTransaction().responseImageData!!)

        assertThat(responseBody).isEqualTo(expectedBody)
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun imageResponseBody_isAvailableToTheEndConsumer(factory: ClientFactory) {
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
    fun gzippedResponseBody_isGunzippedForChucker(factory: ClientFactory) {
        val bytes = Buffer().apply { writeUtf8("Hello, world!") }
        val gzippedBytes = Buffer().apply {
            GzipSink(this).use { sink -> sink.write(bytes, bytes.size) }
        }
        server.enqueue(MockResponse().addHeader("Content-Encoding: gzip").setBody(gzippedBytes))
        val request = Request.Builder().url(serverUrl).build()

        val client = factory.create(chuckerInterceptor)
        client.newCall(request).execute().readByteStringBody()
        val transaction = chuckerInterceptor.expectTransaction()

        assertThat(transaction.isResponseBodyPlainText).isTrue()
        assertThat(transaction.responseBody).isEqualTo("Hello, world!")
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun gzippedResponseBody_isGunzippedForTheEndConsumer(factory: ClientFactory) {
        val bytes = Buffer().apply { writeUtf8("Hello, world!") }
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
    fun gzippedResponseBody_withNoContent_isTransparentForChucker(factory: ClientFactory) {
        server.enqueue(MockResponse().addHeader("Content-Encoding: gzip").setResponseCode(HTTP_NO_CONTENT))
        val request = Request.Builder().url(serverUrl).build()

        val client = factory.create(chuckerInterceptor)
        client.newCall(request).execute().readByteStringBody()
        val transaction = chuckerInterceptor.expectTransaction()

        assertThat(transaction.responseBody).isNull()
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun gzippedResponseBody_withNoContent_isTransparentForEndConsumer(factory: ClientFactory) {
        server.enqueue(MockResponse().addHeader("Content-Encoding: gzip").setResponseCode(HTTP_NO_CONTENT))
        val request = Request.Builder().url(serverUrl).build()

        val client = factory.create(chuckerInterceptor)
        val responseBody = client.newCall(request).execute().readByteStringBody()

        assertThat(responseBody).isNull()
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun plainTextResponseBody_isAvailableForChucker(factory: ClientFactory) {
        val body = Buffer().apply { writeUtf8("Hello, world!") }
        server.enqueue(MockResponse().setBody(body))
        val request = Request.Builder().url(serverUrl).build()

        val client = factory.create(chuckerInterceptor)
        client.newCall(request).execute().readByteStringBody()
        val transaction = chuckerInterceptor.expectTransaction()

        assertThat(transaction.isResponseBodyPlainText).isTrue()
        assertThat(transaction.responseBody).isEqualTo("Hello, world!")
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun plainTextResponseBody_isAvailableForTheEndConsumer(factory: ClientFactory) {
        val body = Buffer().apply { writeUtf8("Hello, world!") }
        server.enqueue(MockResponse().setBody(body))
        val request = Request.Builder().url(serverUrl).build()

        val client = factory.create(chuckerInterceptor)
        val responseBody = client.newCall(request).execute().readByteStringBody()!!

        assertThat(responseBody.utf8()).isEqualTo("Hello, world!")
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun responseBody_withNoContent_isAvailableForChucker(factory: ClientFactory) {
        server.enqueue(MockResponse().setResponseCode(HTTP_NO_CONTENT))
        val request = Request.Builder().url(serverUrl).build()

        val client = factory.create(chuckerInterceptor)
        client.newCall(request).execute().readByteStringBody()
        val transaction = chuckerInterceptor.expectTransaction()

        assertThat(transaction.responseBody).isNull()
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun responseBody_withNoContent_isAvailableForTheEndConsumer(factory: ClientFactory) {
        server.enqueue(MockResponse().setResponseCode(HTTP_NO_CONTENT))
        val request = Request.Builder().url(serverUrl).build()

        val client = factory.create(chuckerInterceptor)
        val responseBody = client.newCall(request).execute().readByteStringBody()

        assertThat(responseBody).isNull()
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun responsePayloadSize_dependsOnTheAmountOfDataDownloaded(factory: ClientFactory) {
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
        assertThat(transaction.responsePayloadSize).isIn(Range.closed(SEGMENT_SIZE, 2 * SEGMENT_SIZE))
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun responseContentLength_isProperlyReadFromHeader_andNotFromAmountOfData(factory: ClientFactory) {
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
    fun responseBody_isLimitedByInterceptorMaxContentLength(factory: ClientFactory) {
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
    fun responsePayloadSize_isNotLimitedByInterceptorMaxContentLength(factory: ClientFactory) {
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
    fun responseReplicationFailure_doesNotAffect_readingByConsumer(factory: ClientFactory) {
        val body = Buffer().apply { writeUtf8("Hello, world!") }
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
    fun responseReplicationFailure_doesNotInvalidate_ChuckerTransaction(factory: ClientFactory) {
        val body = Buffer().apply { writeUtf8("Hello, world!") }
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
    fun missingCache_doesNotInvalidate_ChuckerTransaction(factory: ClientFactory) {
        val body = Buffer().apply { writeUtf8("Hello, world!") }
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
    fun missingCache_doesNotAffect_endConsumer(factory: ClientFactory) {
        val body = Buffer().apply { writeUtf8("Hello, world!") }
        server.enqueue(MockResponse().setBody(body))
        val request = Request.Builder().url(serverUrl).build()

        val chuckerInterceptor = ChuckerInterceptorDelegate(cacheDirectoryProvider = { null })
        val client = factory.create(chuckerInterceptor)
        val responseBody = client.newCall(request).execute().readByteStringBody()

        assertThat(responseBody!!.utf8()).isEqualTo("Hello, world!")
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun alwaysReadResponseBodyFlag_withoutClientConsumingBytes_makesResponseBodyAvailableForChucker(factory: ClientFactory) {
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
    fun alwaysReadResponseBodyFlag_withParsingErrors_makesResponseBodyAvailableForChucker(factory: ClientFactory) {
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
    fun nonPlainTextRequestBody_isRecognizedNotToBePlainText(factory: ClientFactory) {
        server.enqueue(MockResponse())
        val client = factory.create(chuckerInterceptor)

        val request = "\u0080".encodeUtf8().toRequestBody().toServerRequest()
        client.newCall(request).execute().body!!.close()

        val transaction = chuckerInterceptor.expectTransaction()
        assertThat(transaction.isRequestBodyPlainText).isFalse()
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun requestBody_isAvailableToServer(factory: ClientFactory) {
        server.enqueue(MockResponse())
        val client = factory.create(chuckerInterceptor)

        val request = "Hello, world!".toRequestBody().toServerRequest()
        client.newCall(request).execute().readByteStringBody()
        val serverRequestContent = server.takeRequest().body.readByteString()

        assertThat(serverRequestContent.utf8()).isEqualTo("Hello, world!")
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun plainTextRequestBody_isAvailableToChucker(factory: ClientFactory) {
        server.enqueue(MockResponse())
        val client = factory.create(chuckerInterceptor)

        val request = "Hello, world!".toRequestBody().toServerRequest()
        client.newCall(request).execute().readByteStringBody()

        val transaction = chuckerInterceptor.expectTransaction()
        assertThat(transaction.isRequestBodyPlainText).isTrue()
        assertThat(transaction.requestBody).isEqualTo("Hello, world!")
        assertThat(transaction.requestPayloadSize).isEqualTo(request.body!!.contentLength())
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun gzippedRequestBody_isGunzippedForChucker(factory: ClientFactory) {
        server.enqueue(MockResponse())
        val client = factory.create(chuckerInterceptor)

        val gzippedBytes = Buffer().apply {
            GzipSink(this).buffer().use { sink -> sink.writeUtf8("Hello, world!") }
        }.readByteString()
        val request = gzippedBytes.toRequestBody().toServerRequest()
            .newBuilder()
            .header("Content-Encoding", "gzip")
            .build()
        client.newCall(request).execute().readByteStringBody()

        val transaction = chuckerInterceptor.expectTransaction()
        assertThat(transaction.isRequestBodyPlainText).isTrue()
        assertThat(transaction.requestBody).isEqualTo("Hello, world!")
        assertThat(transaction.requestPayloadSize).isEqualTo(request.body!!.contentLength())
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun requestBody_isTruncatedToMaxContentLength(factory: ClientFactory) {
        server.enqueue(MockResponse())
        val chuckerInterceptor = ChuckerInterceptorDelegate(
            maxContentLength = SEGMENT_SIZE,
            cacheDirectoryProvider = { tempDir },
        )
        val client = factory.create(chuckerInterceptor)

        val request = "!".repeat(SEGMENT_SIZE.toInt() * 10).toRequestBody().toServerRequest()
        client.newCall(request).execute().readByteStringBody()

        val transaction = chuckerInterceptor.expectTransaction()
        assertThat(transaction.isRequestBodyPlainText).isTrue()
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
    fun requestHeaders_areRedacted_whenServerFails(factory: ClientFactory) {
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
    fun oneShotRequestBody_isNotAvailableToChucker(factory: ClientFactory) {
        server.enqueue(MockResponse())
        val client = factory.create(chuckerInterceptor)

        val oneShotRequest = object : RequestBody() {
            private val content = Buffer().writeUtf8("Hello, world!")
            override fun isOneShot() = true
            override fun contentType() = "text/plain".toMediaType()
            override fun writeTo(sink: BufferedSink) {
                content.readAll(sink)
            }
        }.toServerRequest()

        client.newCall(oneShotRequest).execute().readByteStringBody()

        val transaction = chuckerInterceptor.expectTransaction()
        assertThat(transaction.requestBody).isNull()
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun oneShotRequestBody_isAvailableToServer(factory: ClientFactory) {
        server.enqueue(MockResponse())
        val client = factory.create(chuckerInterceptor)

        val oneShotRequest = object : RequestBody() {
            private val content = Buffer().writeUtf8("Hello, world!")
            override fun isOneShot() = true
            override fun contentType() = "text/plain".toMediaType()
            override fun writeTo(sink: BufferedSink) {
                content.readAll(sink)
            }
        }.toServerRequest()

        client.newCall(oneShotRequest).execute().readByteStringBody()
        val serverRequestContent = server.takeRequest().body.readByteString()

        assertThat(serverRequestContent.utf8()).isEqualTo("Hello, world!")
    }

    private fun RequestBody.toServerRequest() = Request.Builder().url(serverUrl).post(this).build()

    @ParameterizedTest
    @EnumSource
    fun customBodyDecoder_doesNotChangeRequestBody(factory: ClientFactory) {
        val chuckerInterceptor = ChuckerInterceptorDelegate(
            cacheDirectoryProvider = { tempDir },
            decoders = listOf(ReversingDecoder()),
        )
        val client = factory.create(chuckerInterceptor)
        server.enqueue(MockResponse())

        val request = "Hello, world!".toRequestBody().toServerRequest()
        client.newCall(request).execute().readByteStringBody()
        val serverRequestContent = server.takeRequest().body.readByteString()

        assertThat(serverRequestContent.utf8()).isEqualTo("Hello, world!")
    }

    @ParameterizedTest
    @EnumSource
    fun customBodyDecoder_doesNotChangeResponseBody(factory: ClientFactory) {
        val chuckerInterceptor = ChuckerInterceptorDelegate(
            cacheDirectoryProvider = { tempDir },
            decoders = listOf(ReversingDecoder()),
        )
        val client = factory.create(chuckerInterceptor)

        val body = Buffer().apply { writeUtf8("Hello, world!") }
        server.enqueue(MockResponse().setBody(body))
        val request = Request.Builder().url(serverUrl).build()

        val responseBody = client.newCall(request).execute().readByteStringBody()!!

        assertThat(responseBody.utf8()).isEqualTo("Hello, world!")
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun customBodyDecoder_isUsedForDecoding(factory: ClientFactory) {
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
    fun bodyDecoders_areUsedInAppliedOrder(factory: ClientFactory) {
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
    fun nextBodyDecoder_isUsed_whenPreviousDoesNotDecode(factory: ClientFactory) {
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
    fun bodyDecoder_canThrowIoExceptions(factory: ClientFactory) {
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
    fun bodyDecoders_areAppliedLazily(factory: ClientFactory) {
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
