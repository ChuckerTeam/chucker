package com.chuckerteam.chucker.api

import com.chuckerteam.chucker.util.ChuckerInterceptorDelegate
import com.chuckerteam.chucker.util.ClientFactory
import com.chuckerteam.chucker.util.NoLoggerRule
import com.chuckerteam.chucker.util.readByteStringBody
import io.mockk.every
import io.mockk.mockk
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.io.File

@ExtendWith(NoLoggerRule::class)
internal class ChuckerInterceptorSkipRequestTest {
    @get:Rule
    val server = MockWebServer()

    @TempDir
    lateinit var tempDir: File

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `chucker skips requests when skipPaths are provided as a regular expression`(factory: ClientFactory) {
        val chuckerInterceptorWithSkipping =
            ChuckerInterceptorDelegate(
                cacheDirectoryProvider = { tempDir },
                skipPathsRegex =
                    listOf(
                        ".*(jpg|jpeg|png|gif|webp)$".toRegex(),
                        ".*path/to/skip.*".toRegex(),
                        ".*skipme.*".toRegex(RegexOption.IGNORE_CASE),
                    ),
            )

        val client = factory.create(chuckerInterceptorWithSkipping)

        executeRequestForEncodedPath(client, "/path/to/image/my-image.jpg", "Ends with jpg")
        chuckerInterceptorWithSkipping.expectNoTransactions()

        executeRequestForEncodedPath(
            client,
            "/path/to/image with space/my-image.jpg",
            "Ends with jpg, has space in path",
        )
        chuckerInterceptorWithSkipping.expectNoTransactions()

        executeRequestForEncodedPath(client, "/path/to/image/my-image.jpeg", "Ends with jpeg")
        chuckerInterceptorWithSkipping.expectNoTransactions()

        executeRequestForEncodedPath(client, "/path/to/image/my-image.png", "Ends with png")
        chuckerInterceptorWithSkipping.expectNoTransactions()

        executeRequestForEncodedPath(client, "/path/to/image/my-image.gif", "Ends with gif")
        chuckerInterceptorWithSkipping.expectNoTransactions()

        executeRequestForEncodedPath(client, "/path/to/image/my-image.webp", "Ends with webp")
        chuckerInterceptorWithSkipping.expectNoTransactions()

        executeRequestForEncodedPath(client, "path/to/skip", "matches path to skip")
        chuckerInterceptorWithSkipping.expectNoTransactions()

        executeRequestForEncodedPath(client, "/path/to/skip/", "matches path to skip")
        chuckerInterceptorWithSkipping.expectNoTransactions()

        executeRequestForEncodedPath(client, "no/path/to/skip/here", "matches path to skip")
        chuckerInterceptorWithSkipping.expectNoTransactions()

        executeRequestForEncodedPath(
            client,
            "/path/to/skip/here",
            "matches path to skip with extra path segment",
        )
        chuckerInterceptorWithSkipping.expectNoTransactions()

        executeRequestForEncodedPath(
            client,
            "more/path/to/skip/here",
            "matches path to skip with extra path segments",
        )
        chuckerInterceptorWithSkipping.expectNoTransactions()

        executeRequestForEncodedPath(
            client,
            "/path/to/skip/my-image.jpg",
            "Matches both skip paths",
        )
        chuckerInterceptorWithSkipping.expectNoTransactions()

        executeRequestForEncodedPath(
            client,
            "/path/to/image/skipme.txt",
            "Has 'skipme' in file name",
        )
        chuckerInterceptorWithSkipping.expectNoTransactions()

        executeRequestForEncodedPath(client, "/path/to/skipme/file.mp4", "Has 'skipme' in path")
        chuckerInterceptorWithSkipping.expectNoTransactions()

        executeRequestForEncodedPath(
            client,
            "/path/to/image/image.skipme/",
            "Has 'skipme' in extension",
        )
        chuckerInterceptorWithSkipping.expectNoTransactions()

        executeRequestForEncodedPath(client, "/path/to/image/SKIPME.txt", "Case sensitive")
        chuckerInterceptorWithSkipping.expectNoTransactions()
    }

    @ParameterizedTest
    @EnumSource(value = ClientFactory::class)
    fun `chucker does not skip requests when skipPaths are provided and doesn't match`(factory: ClientFactory) {
        val chuckerInterceptorWithSkipping =
            ChuckerInterceptorDelegate(
                cacheDirectoryProvider = { tempDir },
                skipPathsRegex =
                    listOf(
                        ".*(jpg|jpeg|png|gif|webp)$".toRegex(),
                        ".*path/to/skip.*".toRegex(),
                        ".*skipme.*".toRegex(),
                    ),
            )

        val client = factory.create(chuckerInterceptorWithSkipping)

        executeRequestForEncodedPath(client, "/path/to/image/my-image.JPEG", "Case sensitive")
        chuckerInterceptorWithSkipping.expectTransaction()
        // Ensure no more pending transactions
        chuckerInterceptorWithSkipping.expectNoTransactions()

        executeRequestForEncodedPath(client, "/valid/path", "Random path to execute")
        chuckerInterceptorWithSkipping.expectTransaction()
        // Ensure no more pending transactions
        chuckerInterceptorWithSkipping.expectNoTransactions()

        executeRequestForEncodedPath(client, "/path/to/image/SKIPME.txt", "Case sensitive")
        chuckerInterceptorWithSkipping.expectTransaction()
        // Ensure no more pending transactions
        chuckerInterceptorWithSkipping.expectNoTransactions()

        executeRequestForEncodedPath(
            client,
            "/path/to/image/my-image.jpg/",
            "Doesn't end with jpg, has trailing slash",
        )
        chuckerInterceptorWithSkipping.expectTransaction()
        // Ensure no more pending transactions
        chuckerInterceptorWithSkipping.expectNoTransactions()

        executeRequestForEncodedPath(client, "***", "Doesn't match any path")
        chuckerInterceptorWithSkipping.expectTransaction()
        // Ensure no more pending transactions
        chuckerInterceptorWithSkipping.expectNoTransactions()
    }

    @Test
    fun `chucker skips requests when skip domains are provided and matches`() {
        val chuckerInterceptorWithSkipping =
            ChuckerInterceptorDelegate(
                cacheDirectoryProvider = { tempDir },
                skipDomains =
                    listOf(
                        "skip-that.com",
                        "sub-domain.skip-this.com",
                        "skip.com",
                    ),
                skipDomainsRegex =
                    listOf(
                        ".*skipme.*".toRegex(),
                        ".*skipper.com".toRegex(),
                    ),
            )

        chuckerInterceptorWithSkipping.intercept(getChainFor("skipme.com"))
        chuckerInterceptorWithSkipping.expectNoTransactions()

        chuckerInterceptorWithSkipping.intercept(getChainFor("SKIPME.com"))
        chuckerInterceptorWithSkipping.expectNoTransactions()

        chuckerInterceptorWithSkipping.intercept(getChainFor("skipme.dev"))
        chuckerInterceptorWithSkipping.expectNoTransactions()

        chuckerInterceptorWithSkipping.intercept(getChainFor("doskipmehere.com"))
        chuckerInterceptorWithSkipping.expectNoTransactions()

        chuckerInterceptorWithSkipping.intercept(getChainFor("subdomain.skipme.com"))
        chuckerInterceptorWithSkipping.expectNoTransactions()

        chuckerInterceptorWithSkipping.intercept(getChainFor("skipme.domain.com"))
        chuckerInterceptorWithSkipping.expectNoTransactions()

        chuckerInterceptorWithSkipping.intercept(getChainFor("skipper.com"))
        chuckerInterceptorWithSkipping.expectNoTransactions()

        chuckerInterceptorWithSkipping.intercept(getChainFor("SkIpPeR.COM"))
        chuckerInterceptorWithSkipping.expectNoTransactions()

        chuckerInterceptorWithSkipping.intercept(getChainFor("prefix-skipper.com"))
        chuckerInterceptorWithSkipping.expectNoTransactions()

        chuckerInterceptorWithSkipping.intercept(getChainFor("skip-that.com"))
        chuckerInterceptorWithSkipping.expectNoTransactions()

        chuckerInterceptorWithSkipping.intercept(getChainFor("SuB-dOmAin.SKIP-this.com"))
        chuckerInterceptorWithSkipping.expectNoTransactions()

        chuckerInterceptorWithSkipping.intercept(getChainFor("skip.com"))
        chuckerInterceptorWithSkipping.expectNoTransactions()
    }

    @Test
    fun `chucker should not skip requests when skip domains are provided and does not match`() {
        val chuckerInterceptorWithSkipping =
            ChuckerInterceptorDelegate(
                cacheDirectoryProvider = { tempDir },
                skipDomains =
                    listOf(
                        "skip-that.com",
                        "sub-domain.skip-this.com",
                        "skip.com",
                    ),
                skipDomainsRegex =
                    listOf(
                        ".*skipme.*".toRegex(),
                        ".*skipper.com".toRegex(),
                    ),
            )

        chuckerInterceptorWithSkipping.intercept(getChainFor("skipper.com.co"))
        chuckerInterceptorWithSkipping.expectTransaction()
        // Ensure no more pending transactions
        chuckerInterceptorWithSkipping.expectNoTransactions()

        chuckerInterceptorWithSkipping.intercept(getChainFor("skipper.domain.com"))
        chuckerInterceptorWithSkipping.expectTransaction()
        // Ensure no more pending transactions
        chuckerInterceptorWithSkipping.expectNoTransactions()

        chuckerInterceptorWithSkipping.intercept(getChainFor("domain.com"))
        chuckerInterceptorWithSkipping.expectTransaction()
        // Ensure no more pending transactions
        chuckerInterceptorWithSkipping.expectNoTransactions()

        chuckerInterceptorWithSkipping.intercept(getChainFor("domain.com", "/skipme/in/path"))
        chuckerInterceptorWithSkipping.expectTransaction()
        // Ensure no more pending transactions
        chuckerInterceptorWithSkipping.expectNoTransactions()

        chuckerInterceptorWithSkipping.intercept(getChainFor("skip-that.co"))
        chuckerInterceptorWithSkipping.expectTransaction()
        // Ensure no more pending transactions
        chuckerInterceptorWithSkipping.expectNoTransactions()

        chuckerInterceptorWithSkipping.intercept(getChainFor("skip-that.co.m"))
        chuckerInterceptorWithSkipping.expectTransaction()
        // Ensure no more pending transactions
        chuckerInterceptorWithSkipping.expectNoTransactions()

        chuckerInterceptorWithSkipping.intercept(getChainFor("sub-domain.skip-that.com"))
        chuckerInterceptorWithSkipping.expectTransaction()
        // Ensure no more pending transactions
        chuckerInterceptorWithSkipping.expectNoTransactions()

        chuckerInterceptorWithSkipping.intercept(getChainFor("inner.sub-domain.skip-this.com"))
        chuckerInterceptorWithSkipping.expectTransaction()
        // Ensure no more pending transactions
        chuckerInterceptorWithSkipping.expectNoTransactions()

        chuckerInterceptorWithSkipping.intercept(getChainFor("skip.ca"))
        chuckerInterceptorWithSkipping.expectTransaction()
        // Ensure no more pending transactions
        chuckerInterceptorWithSkipping.expectNoTransactions()
    }

    private fun getChainFor(
        host: String,
        path: String = "/",
    ) = mockk<Interceptor.Chain> {
        every { request() } returns
            mockk {
                every { url } returns
                    HttpUrl.Builder().scheme("https").host(host)
                        .addEncodedPathSegments(path).build()
                every { headers } returns Headers.Builder().build()
                every { method } returns "GET"
                every { body } returns null
            }
        every { proceed(any<Request>()) } returns
            mockk(relaxed = true) {
                every { headers } returns Headers.Builder().build()
                every { body } returns null
            }
    }

    private fun executeRequestForEncodedPath(
        okHttpClient: OkHttpClient,
        path: String,
        responseBody: String,
    ) {
        val request = Request.Builder().url(server.url(path)).build()
        server.enqueue(MockResponse().setBody(responseBody))
        okHttpClient.newCall(request).execute().readByteStringBody()
    }
}
