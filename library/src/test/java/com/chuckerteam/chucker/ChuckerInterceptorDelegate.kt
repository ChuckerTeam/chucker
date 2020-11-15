package com.chuckerteam.chucker

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.support.CacheDirectoryProvider
import io.mockk.every
import io.mockk.mockk
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong

internal class ChuckerInterceptorDelegate(
    maxContentLength: Long = 250000L,
    headersToRedact: Set<String> = emptySet(),
    alwaysReadResponseBody: Boolean = false,
    cacheDirectoryProvider: CacheDirectoryProvider,
) : Interceptor {
    private val idGenerator = AtomicLong()
    private val transactions = CopyOnWriteArrayList<HttpTransaction>()

    private val mockContext = mockk<Context> {
        every { getString(any()) } returns ""
    }
    private val mockCollector = mockk<ChuckerCollector> {
        every { onRequestSent(any()) } returns Unit
        every { onResponseReceived(any()) } answers {
            val transaction = (args[0] as HttpTransaction)
            transaction.id = idGenerator.getAndIncrement()
            transactions.add(transaction)
        }
    }

    private val chucker = ChuckerInterceptor.Builder(context = mockContext)
        .collector(mockCollector)
        .maxContentLength(maxContentLength)
        .redactHeaders(headersToRedact)
        .alwaysReadResponseBody(alwaysReadResponseBody)
        .cacheDirectorProvider(cacheDirectoryProvider)
        .build()

    internal fun expectTransaction(): HttpTransaction {
        if (transactions.isEmpty()) {
            throw AssertionError("Expected transaction but was empty")
        }
        return transactions.removeAt(0)
    }

    internal fun expectNoTransactions() {
        if (transactions.isNotEmpty()) {
            throw AssertionError("Expected no transactions but found ${transactions.size}")
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        return chucker.intercept(chain)
    }
}
