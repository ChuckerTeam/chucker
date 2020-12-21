package com.chuckerteam.chucker.api

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.support.CacheDirectoryProvider
import com.chuckerteam.chucker.internal.support.RequestProcessor
import com.chuckerteam.chucker.internal.support.ResponseProcessor
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

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
     * This constructor is a shorthand for `ChuckerInterceptor.Builder(context).build()`.
     *
     * @param context An Android [Context]
     * @see ChuckerInterceptor.Builder
     */
    public constructor(context: Context) : this(Builder(context))

    private val context = builder.context

    private val collector = builder.collector ?: ChuckerCollector(context)

    private val headersToRedact = builder.headersToRedact.toMutableSet()

    private val requestProcessor = RequestProcessor(
        context = context,
        collector = collector,
        maxContentLength = builder.maxContentLength
    )
    private val responseProcessor = ResponseProcessor(
        collector = collector,
        cacheDirectoryProvider = builder.cacheDirectoryProvider ?: CacheDirectoryProvider { context.filesDir },
        maxContentLength = builder.maxContentLength,
        redactedHeaders = headersToRedact,
        alwaysReadResponseBody = builder.alwaysReadResponseBody,
    )

    /** Adds [headerName] into [headersToRedact] */
    public fun redactHeader(vararg headerName: String) {
        headersToRedact.addAll(headerName)
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val transaction = HttpTransaction()
        val request = requestProcessor.process(chain.request(), transaction)

        val response = try {
            chain.proceed(request)
        } catch (e: IOException) {
            transaction.error = e.toString()
            collector.onResponseReceived(transaction)
            throw e
        }

        return responseProcessor.process(response, transaction)
    }

    /**
     * Multi casts a [Response] body if it is available and downstreams it to a file which will
     * be available for Chucker to consume and save in the [transaction][HttpTransaction] at some point in the future
     * when the end user reads bytes form the [response][Response].
     */

    /**
     * Assembles a new [ChuckerInterceptor].
     *
     * @param context An Android [Context].
     */
    public class Builder(internal var context: Context) {
        internal var collector: ChuckerCollector? = null
        internal var maxContentLength = MAX_CONTENT_LENGTH
        internal var cacheDirectoryProvider: CacheDirectoryProvider? = null
        internal var alwaysReadResponseBody = false
        internal var headersToRedact = emptySet<String>()

        /**
         * Sets the [ChuckerCollector] to customize data retention.
         */
        public fun collector(collector: ChuckerCollector): Builder = apply {
            this.collector = collector
        }

        /**
         * Sets the maximum length for requests and responses content before their truncation.
         *
         * Warning: setting this value too high may cause unexpected results.
         */
        public fun maxContentLength(length: Long): Builder = apply {
            this.maxContentLength = length
        }

        /**
         * Sets headers that will be redacted if their names match.
         * They will be replaced with the `**` symbols in the Chucker UI.
         */
        public fun redactHeaders(headerNames: Iterable<String>): Builder = apply {
            this.headersToRedact = headerNames.toSet()
        }

        /**
         * Sets headers that will be redacted if their names match.
         * They will be replaced with the `**` symbols in the Chucker UI.
         */
        public fun redactHeaders(vararg headerNames: String): Builder = apply {
            this.headersToRedact = headerNames.toSet()
        }

        /**
         * If set to `true` [ChuckerInterceptor] will read full content of response
         * bodies even in case of parsing errors or closing the response body without reading it.
         *
         * Warning: enabling this feature may potentially cause different behaviour from the
         * production application.
         */
        public fun alwaysReadResponseBody(enable: Boolean): Builder = apply {
            this.alwaysReadResponseBody = enable
        }

        /**
         * Sets provider of a directory where Chucker will save temporary responses
         * before processing them.
         */
        @VisibleForTesting
        internal fun cacheDirectorProvider(provider: CacheDirectoryProvider): Builder = apply {
            this.cacheDirectoryProvider = provider
        }

        /**
         * Creates a new [ChuckerInterceptor] instance with values defined in this builder.
         */
        public fun build(): ChuckerInterceptor = ChuckerInterceptor(this)
    }

    private companion object {
        private const val MAX_CONTENT_LENGTH = 250_000L
    }
}
