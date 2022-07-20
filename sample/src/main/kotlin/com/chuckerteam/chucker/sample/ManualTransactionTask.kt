package com.chuckerteam.chucker.sample

import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.entity.ManualHttpTransaction

class ManualTransactionTask(
    private val chuckerCollector: ChuckerCollector
) : HttpTask {

    override fun run() {
        val responseBody = "Some Custom response body!"
        chuckerCollector.saveTransaction(
            transaction = ManualHttpTransaction(
                requestDate = System.currentTimeMillis(),
                responseDate = System.currentTimeMillis(),
                protocol = "CustomProtocol",
                method = "Custom Method",
                responseContentType = "text/html; charset=UTF-8",
                requestContentType = "text/html; charset=UTF-8",
                scheme = "http",
                isResponseBodyEncoded = false,
                tookMs = 1000,
                responseCode = 200,
                responseMessage = "OK",
                responseBody = responseBody,
                isRequestBodyEncoded = false,
                responsePayloadSize = responseBody.toByteArray().size.toLong(),
                host = "Custom HOST",
                url = "http://customUrl.com/",
                path = "/custom",
            )
        )
    }
}
