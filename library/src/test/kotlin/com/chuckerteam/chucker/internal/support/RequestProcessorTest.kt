package com.chuckerteam.chucker.internal.support

import android.content.Context
import com.chuckerteam.chucker.api.BodyDecoder
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import io.mockk.every
import io.mockk.mockk
import okhttp3.HttpUrl
import okhttp3.Request
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

internal class RequestProcessorTest {
    private val context: Context = mockk()
    private val chuckerCollector: ChuckerCollector = mockk(relaxed = true)
    private val maxContentLength: Long = 0
    private val headersToRedact: Set<String> = emptySet()
    private val bodyDecoders: List<BodyDecoder> = emptyList()

    private val graphQLPath = "graphql"

    private val requestProcessor: RequestProcessor = RequestProcessor(
        context = context,
        collector = chuckerCollector,
        maxContentLength = maxContentLength,
        headersToRedact = headersToRedact,
        bodyDecoders = bodyDecoders,
    )


    @Test
    fun `Given a GraphQL Url path WHEN process request THEN transaction is GraphQLRequest`() {
        val transaction = HttpTransaction()
        val graphQLUrl = getUrl(graphQLPath)

        val request: Request = mockk(relaxed = true) {
            every{ url } returns graphQLUrl
        }
        requestProcessor.process(request,transaction,graphQLPath)
        assertTrue(transaction.isGraphQLRequest)

    }

    @Test
    fun `Given an Url with no GraphQL path WHEN process request THEN transaction is NOT GraphQLRequest`() {
        val transaction = HttpTransaction()
        val urlWithNoGraphQLPath = getUrl()
        val request:Request = mockk(relaxed = true) {
            every { url } returns urlWithNoGraphQLPath
        }

        requestProcessor.process(request,transaction,null)
        assertFalse(transaction.isGraphQLRequest)

    }

    @Test
    fun `Given an empty GraphQL path WHEN process request THEN transaction is NOT GraphQLRequest`() {
        val transaction = HttpTransaction()
        val emptyPath = ""
        val urlWithEmptyGraphQLPath = getUrl(emptyPath)
        val request: Request = mockk(relaxed = true) {
            every { url } returns urlWithEmptyGraphQLPath
        }

        requestProcessor.process(request,transaction,emptyPath)
        assertFalse(transaction.isGraphQLRequest)
    }


    @Test
    fun `Given a blank GraphQL path WHEN process request THEN transaction is NOT GraphQLPath`() {
        val transaction = HttpTransaction()
        val blankPath = "     "
        val urlWithEmptyGraphQLPath = getUrl(blankPath)
        val request: Request = mockk(relaxed = true) {
            every { url } returns urlWithEmptyGraphQLPath
        }

        requestProcessor.process(request,transaction,blankPath)
        assertFalse(transaction.isGraphQLRequest)
    }

    private fun getUrl(graphQLPath: String? = null) =  HttpUrl.Builder()
        .scheme("https")
        .host("random_host")
        .addPathSegment(graphQLPath ?: "some/other/path")
        .build()

}

