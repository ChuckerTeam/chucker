package com.chuckerteam.chucker.api

import android.content.Context
import com.chuckerteam.chucker.getResourceFile
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.support.FileFactory
import io.mockk.every
import io.mockk.mockk
import java.io.File
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import okio.ByteString
import okio.GzipSink
import org.junit.Rule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class ChuckerInterceptorTest {
    @get:Rule val server = MockWebServer()
    private val serverUrl = server.url("/") // Starts server implicitly

    private var transaction: HttpTransaction? = null
    private val mockContext = mockk<Context> {
        every { getString(any()) } returns ""
    }
    private val mockCollector = mockk<ChuckerCollector> {
        every { onRequestSent(any()) } returns Unit
        every { onResponseReceived(any()) } answers {
            transaction = args[0] as HttpTransaction
        }
    }

    private lateinit var client: OkHttpClient

    @BeforeEach
    fun setUp(@TempDir tempDir: File) {
        val fileFactory = object : FileFactory {
            override fun create(): File {
                return File(tempDir, "testFile")
            }
        }
        client = OkHttpClient.Builder()
            .addInterceptor(ChuckerInterceptor(mockContext, mockCollector, fileFactory = fileFactory))
            .build()
    }

    @Test
    fun imageResponse_isAvailableToChucker() {
        val image = getResourceFile("sample_image.png")
        server.enqueue(MockResponse().addHeader("Content-Type:image/jpeg").setBody(image))
        val request = Request.Builder().url(serverUrl).build()
        val expectedBody = image.snapshot()

        client.newCall(request).execute().body()?.bytes()

        assertEquals(expectedBody, ByteString.of(*transaction!!.responseImageData!!))
    }

    @Test
    fun imageResponse_isAvailableToTheEndConsumer() {
        val image = getResourceFile("sample_image.png")
        server.enqueue(MockResponse().addHeader("Content-Type:image/jpeg").setBody(image))
        val request = Request.Builder().url(serverUrl).build()
        val expectedBody = image.snapshot()

        val responseBody = client.newCall(request).execute().body()!!.source().readByteString()

        assertEquals(expectedBody, responseBody)
    }

    @Test
    fun gzippedBody_isGunzippedForChucker() {
        val bytes = Buffer().apply { writeUtf8("Hello, world!") }
        val gzippedBytes = Buffer().apply {
            GzipSink(this).use { sink -> sink.write(bytes, bytes.size()) }
        }
        server.enqueue(MockResponse().addHeader("Content-Encoding: gzip").setBody(gzippedBytes))
        val request = Request.Builder().url(serverUrl).build()

        client.newCall(request).execute().body()?.bytes()

        assertTrue(transaction!!.isResponseBodyPlainText)
        assertEquals("Hello, world!", transaction!!.responseBody)
    }

    @Test
    fun gzippedBody_isGunzippedForTheEndConsumer() {
        val bytes = Buffer().apply { writeUtf8("Hello, world!") }
        val gzippedBytes = Buffer().apply {
            GzipSink(this).use { sink -> sink.write(bytes, bytes.size()) }
        }
        server.enqueue(MockResponse().addHeader("Content-Encoding: gzip").setBody(gzippedBytes))
        val request = Request.Builder().url(serverUrl).build()

        val responseBody = client.newCall(request).execute().body()!!.source().readByteString()

        assertEquals("Hello, world!", responseBody.utf8())
    }
}
