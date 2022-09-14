package com.chuckerteam.chucker.internal.support

import android.content.Context
import com.chuckerteam.chucker.api.BodyDecoder
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import io.mockk.every
import io.mockk.mockk
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

internal class RequestProcessorTest {

    private val context: Context = mockk()
    private val chuckerCollector: ChuckerCollector = mockk(relaxed = true)
    private val maxContentLength: Long = 0
    private val headersToRedact: Set<String> = emptySet()
    private val bodyDecoders: List<BodyDecoder> = emptyList()
    private val expectedGraphQLUrl = "http://some/graphql/url"
    private val requestProcessor: RequestProcessor = RequestProcessor(
        context = context,
        collector = chuckerCollector,
        maxContentLength = maxContentLength,
        headersToRedact = headersToRedact,
        bodyDecoders = bodyDecoders,
    )

    @Test
    fun `GIVEN graphql Url WHEN process request THEN transaction isGraphQLRequest`() {
        val transaction = HttpTransaction()
        val request: Request = mockk(relaxed = true ) {
            every { url } returns expectedGraphQLUrl.toHttpUrl()
        }
        requestProcessor.process(request, transaction, expectedGraphQLUrl)

        assertTrue(transaction.isGraphQLRequest)
    }

    @Test
    fun `GIVEN no graphql Url WHEN process request THEN transaction !isGraphQLRequest`() {
        val transaction = HttpTransaction()
        val request: Request = mockk(relaxed = true ) {
            every { url } returns "http://some/random/url".toHttpUrl()
        }
        requestProcessor.process(request, transaction, expectedGraphQLUrl)

        assertFalse(transaction.isGraphQLRequest)
    }

    @Test
    fun `Given an empty GraphQL path WHEN process request THEN transaction is NOT GraphQLRequest`() {
        val transaction = HttpTransaction()
        val emptyGraphQLUrl = ""
        val request: Request = mockk(relaxed = true) {
            every { url } returns "https://some/random/url".toHttpUrl()
        }

        requestProcessor.process(request,transaction,emptyGraphQLUrl)
        assertFalse(transaction.isGraphQLRequest)
    }

    @Test
    fun `Given a blank GraphQL path WHEN process request THEN transaction is NOT GraphQLPath`() {
        val transaction = HttpTransaction()
        val blankPath = "     "

        val request: Request = mockk(relaxed = true) {
            every { url } returns "https://some/random/url".toHttpUrl()
        }

        requestProcessor.process(request,transaction,blankPath)
        assertFalse(transaction.isGraphQLRequest)
    }

}
