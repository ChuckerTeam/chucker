package com.chuckerteam.chucker.internal.support

import android.content.Context
import com.chuckerteam.chucker.api.BodyDecoder
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import io.mockk.every
import io.mockk.mockk
import okhttp3.Headers
import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Test

internal class RequestProcessorTest {

    private val context: Context = mockk()
    private val chuckerCollector: ChuckerCollector = mockk(relaxed = true)
    private val maxContentLength: Long = 0
    private val headersToRedact: Set<String> = emptySet()
    private val bodyDecoders: List<BodyDecoder> = emptyList()

    private val requestProcessor: RequestProcessor = RequestProcessor(
        context = context,
        collector = chuckerCollector,
        maxContentLength = maxContentLength,
        headersToRedact = headersToRedact,
        bodyDecoders = bodyDecoders,
    )

    @Test
    fun `GIVEN graphql headers WHEN process request THEN transaction has graphQlOperationName`() {
        val operationName = "SearchCharacters"
        val transaction = HttpTransaction()
        val headersGraphQl = Headers.Builder().add("X-APOLLO-OPERATION-NAME", operationName).build()
        val request:Request = mockk(relaxed = true) {
            every { headers } returns headersGraphQl
        }

        requestProcessor.process(request, transaction)

        assertEquals(operationName, transaction.graphQlOperationName)
    }
}
