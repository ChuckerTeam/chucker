package com.chuckerteam.chucker.internal.support

import com.chuckerteam.chucker.api.ChuckerEventListener
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import okhttp3.Call
import okhttp3.EventListener
import java.util.concurrent.ConcurrentHashMap

internal class RealChuckerEventListenerFactory : EventListener.Factory, HttpTransactionFactory {

    private val httpTransactionHolder = ConcurrentHashMap<Call, ChuckerEventListener>()

    override fun create(call: Call): EventListener {
        val eventListener = ChuckerEventListener { httpTransactionHolder.remove(it) }
        httpTransactionHolder[call] = eventListener

        return eventListener
    }

    override fun getHttpTransaction(call: Call): HttpTransaction {
        return httpTransactionHolder[call]?.httpTransaction
            ?: error("HttpTransaction required before the Call was created")
    }

}

internal interface HttpTransactionFactory {
    fun getHttpTransaction(call: Call): HttpTransaction
}

internal class SimpleHttpTransactionFactory : HttpTransactionFactory {
    override fun getHttpTransaction(call: Call): HttpTransaction {
        return HttpTransaction()
    }

}

