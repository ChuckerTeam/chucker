package com.chuckerteam.chucker

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import io.mockk.every
import io.mockk.mockk
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong
import okhttp3.Interceptor
import okhttp3.Response

class ChuckerInterceptorDelegate(
    maxContentLength: Long = 250000L,
    headersToRedact: Set<String> = emptySet()
) : Interceptor {
    private val idGenerator = AtomicLong()
    private val transactions = CopyOnWriteArrayList<HttpTransaction>()

    private val mockContext = mockk<Context> {
        every { getString(any()) } returns ""
    }
    private val mockCollector = mockk<ChuckerCollector>() {
        every { onRequestSent(any()) } returns Unit
        every { onResponseReceived(any()) } answers {
            val transaction = (args[0] as HttpTransaction)
            transaction.id = idGenerator.getAndIncrement()
            transactions.add(transaction)
        }
    }

    private val chucker = ChuckerInterceptor(mockContext, mockCollector, maxContentLength, headersToRedact)

    internal fun expectTransaction(): HttpTransaction {
        if (transactions.isEmpty()) {
            throw AssertionError("Expected transaction but was empty.")
        }
        return transactions.removeAt(0)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        return chucker.intercept(chain)
    }
}
