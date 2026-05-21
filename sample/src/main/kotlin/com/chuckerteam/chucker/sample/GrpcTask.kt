package com.chuckerteam.chucker.sample

import android.content.Context
import android.util.Log
import com.chuckerteam.chucker.api.ChuckerGrpcInterceptor
import com.chuckerteam.chucker.sample.grpc.HelloRequest
import com.chuckerteam.chucker.sample.grpc.SampleGreeterGrpc
import io.grpc.ManagedChannel
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

internal class GrpcTask(context: Context) {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val interceptor = ChuckerGrpcInterceptor(
        collector = (context.applicationContext as SampleApplication).chuckerCollector,
        context = context,
    )
    private lateinit var channel: ManagedChannel

    fun execute() {
        scope.launch {
            channel = OkHttpChannelBuilder.forAddress("localhost", SampleApplication.GRPC_PORT)
                .usePlaintext()
                .intercept(interceptor)
                .build()

            val blocking = SampleGreeterGrpc.newBlockingStub(channel)
            val async = SampleGreeterGrpc.newStub(channel)

            runUnary(blocking)
            delay(500)
            runServerStream(blocking)
            delay(500)
            runClientStream(async)
            delay(500)
            runBidiStream(async)

            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
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
        } catch (e: Exception) {
            Log.e(TAG, "Unary failed", e)
        }
    }

    private fun runServerStream(stub: SampleGreeterGrpc.SampleGreeterBlockingStub) {
        try {
            stub.sayHelloServerStream(HelloRequest.newBuilder().setName("StreamUser").build())
                .forEach { Log.i(TAG, "ServerStream: ${it.message}") }
        } catch (e: Exception) {
            Log.e(TAG, "ServerStream failed", e)
        }
    }

    private suspend fun runClientStream(stub: SampleGreeterGrpc.SampleGreeterStub) {
        val latch = CountDownLatch(1)
        val observer = stub.sayHelloClientStream(object : StreamObserver<com.chuckerteam.chucker.sample.grpc.HelloReply> {
            override fun onNext(value: com.chuckerteam.chucker.sample.grpc.HelloReply) {
                Log.i(TAG, "ClientStream: ${value.message}")
            }

            override fun onError(t: Throwable) {
                Log.e(TAG, "ClientStream error", t)
                latch.countDown()
            }

            override fun onCompleted() {
                latch.countDown()
            }
        })
        try {
            listOf("User1", "User2", "User3").forEach {
                observer.onNext(HelloRequest.newBuilder().setName(it).build())
                delay(200)
            }
            observer.onCompleted()
            latch.await(10, TimeUnit.SECONDS)
        } catch (e: Exception) {
            Log.e(TAG, "ClientStream failed", e)
        }
    }

    private suspend fun runBidiStream(stub: SampleGreeterGrpc.SampleGreeterStub) {
        val latch = CountDownLatch(1)
        val observer = stub.sayHelloBidiStream(object : StreamObserver<com.chuckerteam.chucker.sample.grpc.HelloReply> {
            override fun onNext(value: com.chuckerteam.chucker.sample.grpc.HelloReply) {
                Log.i(TAG, "BidiStream: ${value.message}")
            }

            override fun onError(t: Throwable) {
                Log.e(TAG, "BidiStream error", t)
                latch.countDown()
            }

            override fun onCompleted() {
                latch.countDown()
            }
        })
        try {
            listOf("BidiUser1", "BidiUser2", "BidiUser3").asFlow().map {
                delay(300)
                HelloRequest.newBuilder().setName(it).build()
            }.collect { observer.onNext(it) }
            observer.onCompleted()
            latch.await(10, TimeUnit.SECONDS)
        } catch (e: Exception) {
            Log.e(TAG, "BidiStream failed", e)
        }
    }

    private companion object {
        const val TAG = "GrpcTask"
    }
}
