package com.chuckerteam.chucker.api

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.support.CacheDirectoryProvider
import com.chuckerteam.chucker.internal.support.PlainTextDecoder
import com.chuckerteam.chucker.internal.support.RequestProcessor
import com.chuckerteam.chucker.internal.support.ResponseProcessor
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.regex.PatternSyntaxException

/**
 * An OkHttp Interceptor which persists and displays HTTP activity
 * in your application for later inspection.
 */
public class ChuckerInterceptor private constructor(
    builder: Builder,
) : Interceptor {
    /**
     * An OkHttp Interceptor which persists and displays HTTP activity
     * in your application for later inspection.
     *
     * This constructor  is a shorthand for `ChuckerInterceptor.Builder(context).build()`.
     *
     * @param context An Android [Context]
     * @see ChuckerInterceptor.Builder
     */
    public constructor(context: Context) : this(Builder(context))

    private val headersToRedact = builder.headersToRedact.toMutableSet()

    private val decoders = builder.decoders + BUILT_IN_DECODERS

    private val collector = builder.collector ?: ChuckerCollector(builder.context)

    private val requestProcessor =
        RequestProcessor(
            builder.context,
            collector,
            builder.maxContentLength,
            headersToRedact,
            decoders,
        )

    private val responseProcessor =
        ResponseProcessor(
            collector,
            builder.cacheDirectoryProvider ?: CacheDirectoryProvider { builder.context.filesDir },
            builder.maxContentLength,
            headersToRedact,
            builder.alwaysReadResponseBody,
            decoders,
        )

    private val skipPaths = builder.skipPaths.toSet()
    private val skipPathsRegex = builder.skipPathsRegex.toSet()
    private val skipDomainRegex = builder.skipDomainRegex.toSet()

    init {
        if (builder.createShortcut) {
            Chucker.createShortcut(builder.context)
        }
    }

    /** Adds [headerName] into [headersToRedact] */
    public fun redactHeader(vararg headerName: String) {
        headersToRedact.addAll(headerName)
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val transaction = HttpTransaction()
        val request = chain.request()
        val path = request.url.encodedPath
        val shouldSkipPath = skipPaths.contains(path) || skipPathsRegex.any { it.matches(path) }
        val shouldSkipDomain = skipDomainRegex.any { it.matches(request.url.host) }
        val shouldProcessTheRequest = !(shouldSkipPath || shouldSkipDomain)

        if (shouldProcessTheRequest) {
            requestProcessor.process(request, transaction)
        }
        val response =
            try {
                chain.proceed(request)
            } catch (e: IOException) {
                transaction.error = e.toString()
                collector.onResponseReceived(transaction)
                throw e
            }
        return if (shouldProcessTheRequest) {
            responseProcessor.process(response, transaction)
        } else {
            response
        }
    }

    /**
     * Assembles a new [ChuckerInterceptor].
     *
     * @param context An Android [Context].
     */
    @Suppress("TooManyFunctions")
    public class Builder(internal var context: Context) {
        internal var collector: ChuckerCollector? = null
        internal var maxContentLength = MAX_CONTENT_LENGTH
        internal var cacheDirectoryProvider: CacheDirectoryProvider? = null
        internal var alwaysReadResponseBody = false
        internal var headersToRedact = emptySet<String>()
        internal var decoders = emptyList<BodyDecoder>()
        internal var createShortcut = true
        internal val skipPaths = mutableSetOf<String>()
        internal val skipPathsRegex = mutableSetOf<Regex>()
        internal val skipDomainRegex = mutableSetOf<Regex>()

        /**
         * Sets the [ChuckerCollector] to customize data retention.
         */
        public fun collector(collector: ChuckerCollector): Builder =
            apply {
                this.collector = collector
            }

        /**
         * Sets the maximum length for requests and responses content before their truncation.
         *
         * Warning: setting this value too high may cause unexpected results.
         */
        public fun maxContentLength(length: Long): Builder =
            apply {
                this.maxContentLength = length
            }

        /**
         * Sets headers that will be redacted if their names match.
         * They will be replaced with the `**` symbols in the Chucker UI.
         */
        public fun redactHeaders(headerNames: Iterable<String>): Builder =
            apply {
                this.headersToRedact = headerNames.toSet()
            }

        /**
         * Sets headers that will be redacted if their names match.
         * They will be replaced with the `**` symbols in the Chucker UI.
         */
        public fun redactHeaders(vararg headerNames: String): Builder =
            apply {
                this.headersToRedact = headerNames.toSet()
            }

        /**
         * If set to `true` [ChuckerInterceptor] will read full content of response
         * bodies even in case of parsing errors or closing the response body without reading it.
         *
         * Warning: enabling this feature may potentially cause different behaviour from the
         * production application.
         */
        public fun alwaysReadResponseBody(enable: Boolean): Builder =
            apply {
                this.alwaysReadResponseBody = enable
            }

        /**
         * Adds a [decoder] into Chucker's processing pipeline. Decoders are applied in an order they were added in.
         * Request and response bodies are set to the first non–null value returned by any of the decoders.
         */
        public fun addBodyDecoder(decoder: BodyDecoder): Builder =
            apply {
                this.decoders += decoder
            }

        /**
         * If set to `true`, [ChuckerInterceptor] will create a shortcut for your app
         * to access list of transaction in Chucker.
         */
        public fun createShortcut(enable: Boolean): Builder =
            apply {
                this.createShortcut = enable
            }

        /**
         * Sets provider of a directory where Chucker will save temporary responses
         * before processing them.
         */
        @VisibleForTesting
        internal fun cacheDirectorProvider(provider: CacheDirectoryProvider): Builder =
            apply {
                this.cacheDirectoryProvider = provider
            }

        /**
         * Sets a list of [String] to skip paths. When any of the [String] matches
         * a request path, the request will be skipped.
         */
        public fun skipPaths(vararg skipPaths: String): Builder =
            apply {
                skipPaths.forEach { candidatePath ->
                    val httpUrl =
                        HttpUrl.Builder()
                            .scheme("https")
                            .host("example.com")
                            .addPathSegment(candidatePath).build()
                    this@Builder.skipPaths.add(httpUrl.encodedPath)
                }
            }

        /**
         * Sets a list of [Regex] to skip paths. When any of the [Regex] matches a
         * request path, the request will be skipped. Include [RegexOption] where
         * necessary.
         *
         * ```
         * ".*(jpg|jpeg|png|gif|webp|svg|bmp|ico)$".toRegex(), // Ignore all image requests
         *  ".*iGnOrE.*"toRegex(RegexOption.IGNORE_CASE), // Case insensitive
         *  ".*path/to/skip.*".toRegex(),
         *  ".*path/ends/with/dev$".toRegex(),
         * ```
         */
        public fun skipPaths(skipPaths: Regex): Builder =
            apply {
                this.skipPathsRegex.add(skipPaths)
            }

        /**
         * Sets a list of [String] to skip domains. Domain names are evaluated in
         * lowercase format. When any of the [String] matches a request domain,
         * the request will be skipped.
         * ```
         * example.com, subdomain.example.com, exam-ple.com, eXaMpLe.CoM, example.co.uk
         * ```
         * @throws PatternSyntaxException if the domain pattern is invalid.
         */
        @Throws(PatternSyntaxException::class)
        public fun skipDomains(vararg skipDomains: String): Builder =
            apply {
                skipDomains.forEach { domain ->
                    this.skipDomainRegex.add("^${domain}\$".toRegex(RegexOption.IGNORE_CASE))
                }
            }

        /**
         * Sets a list of [Regex] to skip domains. Domain names are evaluated in
         * lowercase format. When any of the [Regex] matches a request domain,
         * the request will be skipped. Include [RegexOption] where necessary.
         *
         * ```
         *  ".*iGnOrE.*"toRegex(RegexOption.IGNORE_CASE),
         *  "ignoresubdomain.*".toRegex(),
         *  "domainname.*".toRegex(),
         *  ".*.dev$".toRegex(),
         * ```
         */
        public fun skipDomains(skipDomains: Regex): Builder =
            apply {
                this.skipDomainRegex.add(skipDomains)
            }

        /**
         * Creates a new [ChuckerInterceptor] instance with values defined in this
         * builder.
         */
        public fun build(): ChuckerInterceptor = ChuckerInterceptor(this)
    }

    private companion object {
        private const val MAX_CONTENT_LENGTH = 250_000L

        private val BUILT_IN_DECODERS = listOf(PlainTextDecoder)
    }
}
