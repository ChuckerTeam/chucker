package com.chuckerteam.chucker

import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import java.util.Date

object TestTransactionFactory {

    internal fun createTransaction(method: String): HttpTransaction {
        return HttpTransaction(
            id = 0,
            requestDate = Date(1300000).time,
            responseDate = Date(1300300).time,
            tookMs = 1000L,
            protocol = "HTTP",
            method = method,
            url = "http://localhost/getUsers",
            host = "localhost",
            path = "/getUsers",
            scheme = "",
            responseTlsVersion = "",
            responseCipherSuite = "",
            requestContentLength = 1000L,
            requestContentType = "application/json",
            requestHeaders = null,
            requestBody = null,
            isRequestBodyPlainText = true,
            responseCode = 200,
            responseMessage = "OK",
            error = null,
            responseContentLength = 1000L,
            responseContentType = "application/json",
            responseHeaders = null,
            responseBody =
                """{"field": "value"}""",
            isResponseBodyPlainText = true,
            responseImageData = null
        )
    }

    val expectedGetHttpTransaction =
        """
        URL: http://localhost/getUsers
        Method: GET
        Protocol: HTTP
        Status: Complete
        Response: 200 OK
        SSL: No
        
        Request time: ${Date(1300000)}
        Response time: ${Date(1300300)}
        Duration: 1000 ms
        
        Request size: 1.0 kB
        Response size: 1.0 kB
        Total size: 2.0 kB
        
        ---------- Request ----------
        
        
        
        ---------- Response ----------
        
        {
          "field": "value"
        }
        """.trimIndent()

    val expectedHttpPostTransaction =
        """
        URL: http://localhost/getUsers
        Method: POST
        Protocol: HTTP
        Status: Complete
        Response: 200 OK
        SSL: No
        
        Request time: ${Date(1300000)}
        Response time: ${Date(1300300)}
        Duration: 1000 ms
        
        Request size: 1.0 kB
        Response size: 1.0 kB
        Total size: 2.0 kB
        
        ---------- Request ----------
        
        
        
        ---------- Response ----------
        
        {
          "field": "value"
        }
        """.trimIndent()
}
