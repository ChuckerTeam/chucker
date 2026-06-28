package com.chuckerteam.chucker.sample

import android.content.Context
import android.util.Log
import com.chuckerteam.chucker.api.ChuckerGrpcInterceptor
import com.chuckerteam.chucker.sample.grpc.HelloReply
import com.chuckerteam.chucker.sample.grpc.HelloRequest
import com.chuckerteam.chucker.sample.grpc.SampleGreeterGrpc
import io.grpc.ManagedChannel
import io.grpc.StatusRuntimeException
import io.grpc.okhttp.OkHttpChannelBuilder
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal class GrpcTask(
    context: Context,
) {
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val interceptor =
        ChuckerGrpcInterceptor(
            collector = (context.applicationContext as SampleApplication).chuckerCollector,
            context = context,
        )
    private lateinit var channel: ManagedChannel

    fun execute() {
        scope.launch {
            channel =
                OkHttpChannelBuilder
                    .forAddress("localhost", SampleApplication.GRPC_PORT)
                    .usePlaintext()
                    .intercept(interceptor)
                    .build()

            val blocking = SampleGreeterGrpc.newBlockingStub(channel)
            val async = SampleGreeterGrpc.newStub(channel)

            runUnary(blocking)
            delay(CALL_DELAY_MS)
            runServerStream(blocking)
            delay(CALL_DELAY_MS)
            runClientStream(async)
            delay(CALL_DELAY_MS)
            runBidiStream(async)

            channel.shutdown().awaitTermination(SHUTDOWN_TIMEOUT_SEC, TimeUnit.SECONDS)
        }
    }

    fun cancel() {
        job.cancel()
        if (::channel.isInitialized && !channel.isTerminated) {
            channel.shutdownNow()
        }
    }

    private fun runUnary(stub: SampleGreeterGrpc.SampleGreeterBlockingStub) {
        try {
            val reply = stub.sayHello(HelloRequest.newBuilder().setName("UnaryUser").build())
            Log.i(TAG, "Unary: ${reply.message}")
        } catch (e: StatusRuntimeException) {
            Log.e(TAG, "Unary failed", e)
        }
    }

    private fun runServerStream(stub: SampleGreeterGrpc.SampleGreeterBlockingStub) {
        try {
            stub
                .sayHelloServerStream(HelloRequest.newBuilder().setName("StreamUser").build())
                .forEach { Log.i(TAG, "ServerStream: ${it.message}") }
        } catch (e: StatusRuntimeException) {
            Log.e(TAG, "ServerStream failed", e)
        }
    }

    private suspend fun runClientStream(stub: SampleGreeterGrpc.SampleGreeterStub) {
        val latch = CountDownLatch(1)
        val observer =
            stub.sayHelloClientStream(
                object : StreamObserver<HelloReply> {
                    override fun onNext(value: HelloReply) {
                        Log.i(TAG, "ClientStream: ${value.message}")
                    }

                    override fun onError(t: Throwable) {
                        Log.e(TAG, "ClientStream error", t)
                        latch.countDown()
                    }

                    override fun onCompleted() {
                        latch.countDown()
                    }
                },
            )
        try {
            listOf("User1", "User2", "User3").forEach {
                observer.onNext(HelloRequest.newBuilder().setName(it).build())
                delay(STREAM_MESSAGE_DELAY_MS)
            }
            observer.onCompleted()
            latch.await(AWAIT_TIMEOUT_SEC, TimeUnit.SECONDS)
        } catch (e: StatusRuntimeException) {
            Log.e(TAG, "ClientStream failed", e)
        } catch (e: InterruptedException) {
            Log.e(TAG, "ClientStream interrupted", e)
            Thread.currentThread().interrupt()
        }
    }

    private suspend fun runBidiStream(stub: SampleGreeterGrpc.SampleGreeterStub) {
        val latch = CountDownLatch(1)
        val observer =
            stub.sayHelloBidiStream(
                object : StreamObserver<HelloReply> {
                    override fun onNext(value: HelloReply) {
                        Log.i(TAG, "BidiStream: ${value.message}")
                    }

                    override fun onError(t: Throwable) {
                        Log.e(TAG, "BidiStream error", t)
                        latch.countDown()
                    }

                    override fun onCompleted() {
                        latch.countDown()
                    }
                },
            )
        try {
            listOf("BidiUser1", "BidiUser2", "BidiUser3")
                .asFlow()
                .map {
                    delay(BIDI_MESSAGE_DELAY_MS)
                    HelloRequest.newBuilder().setName(it).build()
                }.collect { observer.onNext(it) }
            observer.onCompleted()
            latch.await(AWAIT_TIMEOUT_SEC, TimeUnit.SECONDS)
        } catch (e: StatusRuntimeException) {
            Log.e(TAG, "BidiStream failed", e)
        } catch (e: InterruptedException) {
            Log.e(TAG, "BidiStream interrupted", e)
            Thread.currentThread().interrupt()
        }
    }

    private companion object {
        const val TAG = "GrpcTask"
        const val CALL_DELAY_MS = 500L
        const val STREAM_MESSAGE_DELAY_MS = 200L
        const val BIDI_MESSAGE_DELAY_MS = 300L
        const val AWAIT_TIMEOUT_SEC = 10L
        const val SHUTDOWN_TIMEOUT_SEC = 5L
    }
}
