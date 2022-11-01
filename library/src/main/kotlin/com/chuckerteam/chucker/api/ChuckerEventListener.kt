package com.chuckerteam.chucker.api

import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.Handshake
import okhttp3.Protocol
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy

internal class ChuckerEventListener constructor(
    private val onCallEnded: ((Call) -> Unit)
) : EventListener() {

    val httpTransaction = HttpTransaction()

    override fun callStart(call: Call) {
        super.callStart(call)
        httpTransaction.callStartDate = System.currentTimeMillis()
    }

    override fun callEnd(call: Call) {
        super.callEnd(call)
        onCallEnded(call)
        httpTransaction.callEndDate = System.currentTimeMillis()
    }

    override fun callFailed(call: Call, ioe: IOException) {
        super.callFailed(call, ioe)
        onCallEnded(call)
    }

    override fun canceled(call: Call) {
        super.canceled(call)
        onCallEnded(call)
    }

    override fun connectStart(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy) {
        super.connectStart(call, inetSocketAddress, proxy)
        httpTransaction.connectStartDate = System.currentTimeMillis()
    }

    override fun connectEnd(
        call: Call,
        inetSocketAddress: InetSocketAddress,
        proxy: Proxy,
        protocol: Protocol?
    ) {
        super.connectEnd(call, inetSocketAddress, proxy, protocol)
        httpTransaction.connectEndDate = System.currentTimeMillis()
    }

    override fun dnsStart(call: Call, domainName: String) {
        super.dnsStart(call, domainName)
        httpTransaction.dnsStartDate = System.currentTimeMillis()
    }

    override fun dnsEnd(call: Call, domainName: String, inetAddressList: List<InetAddress>) {
        super.dnsEnd(call, domainName, inetAddressList)
        httpTransaction.dnsEndDate = System.currentTimeMillis()
    }

    override fun secureConnectStart(call: Call) {
        super.secureConnectStart(call)
        httpTransaction.secureConnectStartDate = System.currentTimeMillis()
    }

    override fun secureConnectEnd(call: Call, handshake: Handshake?) {
        super.secureConnectEnd(call, handshake)
        httpTransaction.secureConnectEndDate = System.currentTimeMillis()
    }

}
