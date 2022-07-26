package com.chuckerteam.chucker.util

import android.content.Context
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.har.Har
import com.chuckerteam.chucker.internal.data.har.log.Creator
import com.chuckerteam.chucker.internal.data.har.log.Entry
import com.chuckerteam.chucker.internal.data.har.log.entry.Request
import com.chuckerteam.chucker.internal.data.har.log.entry.Response
import com.chuckerteam.chucker.internal.data.har.log.entry.request.PostData
import com.chuckerteam.chucker.internal.data.har.log.entry.response.Content
import com.chuckerteam.chucker.internal.support.HarUtils
import kotlinx.coroutines.runBlocking
import java.util.Date

internal object HarTestUtils {

    internal fun createTransaction(method: String): HttpTransaction {
        val requestBodySize = when (method) {
            "GET" -> null
            else -> 1000L
        }
        return HttpTransaction(
            id = 0,
            requestDate = Date(1300000).time,
            responseDate = Date(1300300).time,
            tookMs = 1000L,
            protocol = "HTTP",
            method = method,
            url = "http://localhost:80/getUsers",
            host = "localhost",
            path = "/getUsers",
            scheme = "",
            responseTlsVersion = "",
            responseCipherSuite = "",
            requestPayloadSize = requestBodySize,
            requestContentType = "application/json",
            requestHeaders = null,
            requestHeadersSize = null,
            requestBody = null,
            isRequestBodyEncoded = false,
            responseCode = 200,
            responseMessage = "OK",
            error = null,
            responsePayloadSize = 1000L,
            responseContentType = "application/json",
            responseHeaders = null,
            responseHeadersSize = null,
            responseBody = """{"field": "value"}""",
            isResponseBodyEncoded = false,
            responseImageData = null
        )
    }

    internal fun Context.createSingleTransactionHar(method: String): Har {
        return HarUtils.fromHttpTransactions(
            listOf(createTransaction(method)),
            Creator(getString(R.string.chucker_name), getString(R.string.chucker_version))
        )
    }

    internal fun Context.createListTransactionHar(): Har {
        return HarUtils.fromHttpTransactions(
            listOf(createTransaction("GET"), createTransaction("POST")),
            Creator(getString(R.string.chucker_name), getString(R.string.chucker_version))
        )
    }

    internal fun Context.createHarString(): String {
        return runBlocking {
            HarUtils.harStringFromTransactions(
                listOf(createTransaction("POST")),
                getString(R.string.chucker_name),
                getString(R.string.chucker_version)
            )
        }
    }

    internal fun createContent(method: String): Content {
        return Content(createTransaction(method))
    }

    internal fun createEntry(method: String): Entry? {
        return Entry(createTransaction(method))
    }

    internal fun createPostData(method: String): PostData? {
        return PostData(createTransaction(method))
    }

    internal fun createRequest(method: String): Request? {
        return Request(createTransaction(method))
    }

    internal fun createResponse(method: String): Response? {
        return Response(createTransaction(method))
    }
}
