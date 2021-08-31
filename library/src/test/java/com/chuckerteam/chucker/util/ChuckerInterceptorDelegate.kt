package com.chuckerteam.chucker.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.api.BodyDecoder
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.support.CacheDirectoryProvider
import io.mockk.every
import io.mockk.mockk
import okhttp3.Interceptor
import okhttp3.Response
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong


internal class ChuckerInterceptorDelegate(
    maxContentLength: Long = 250000L,
    headersToRedact: Set<String> = emptySet(),
    alwaysReadResponseBody: Boolean = false,
    cacheDirectoryProvider: CacheDirectoryProvider,
    decoders: List<BodyDecoder> = emptyList(),
) : Interceptor {
    private val idGenerator = AtomicLong()
    private val transactions = CopyOnWriteArrayList<HttpTransaction>()

    private val mockEditor = mockk<SharedPreferences.Editor> {
        every { remove(anyString()) } returns this
        every { remove("chucker_saved_redacted_headers") } returns this
        every { putString(anyString(), anyString()) } returns this
        every { putString("chucker_saved_redacted_headers", anyString()) } returns this
        every { putString("chucker_saved_redacted_headers", "Header-To-Redact") } returns this
        every { apply() } returns Unit
    }
    private val sharedPrefs = mockk<SharedPreferences> {
        every { edit() } returns mockEditor
        every { getString(anyString(), anyString()) } returns "dummy_value"
        every { getString(anyString(), null) } returns "dummy_value"
    }
    private val mockContext = mockk<Context> {
        every { getString(R.string.chucker_body_content_truncated) } returns "\n\n--- Content truncated ---"
        every { getSharedPreferences("chucker_prefs", 0) } returns sharedPrefs
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
        .apply { decoders.forEach(::addBodyDecoder) }
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
