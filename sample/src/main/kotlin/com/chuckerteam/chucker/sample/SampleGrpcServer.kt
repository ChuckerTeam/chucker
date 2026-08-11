package com.chuckerteam.chucker.sample

import android.util.Log
import com.chuckerteam.chucker.sample.grpc.HelloReply
import com.chuckerteam.chucker.sample.grpc.HelloRequest
import com.chuckerteam.chucker.sample.grpc.SampleGreeterGrpc
import io.grpc.Server
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.io.IOException
import java.util.concurrent.TimeUnit

internal class SampleGrpcServer(
    private val port: Int,
) {
    private val server: Server by lazy {
        NettyServerBuilder
            .forPort(port)
            .addService(GreeterService())
            .executor(Dispatchers.IO.asExecutor())
            .build()
    }

    fun start() {
        try {
            server.start()
            Log.i(TAG, "Server started on port $port")
        } catch (e: IOException) {
            Log.e(TAG, "Failed to start server", e)
        }
    }

    fun stop() {
        try {
            server.shutdown().awaitTermination(SHUTDOWN_TIMEOUT_SEC, TimeUnit.SECONDS)
            Log.i(TAG, "Server stopped")
        } catch (e: InterruptedException) {
            Log.w(TAG, "Shutdown interrupted, forcing stop", e)
            server.shutdownNow()
            Thread.currentThread().interrupt()
        }
    }

    private class GreeterService : SampleGreeterGrpc.SampleGreeterImplBase() {
        override fun sayHello(
            req: HelloRequest,
            responseObserver: StreamObserver<HelloReply>,
        ) {
            responseObserver.onNext(HelloReply.newBuilder().setMessage("Hello ${req.name} (Unary)").build())
            responseObserver.onCompleted()
        }

        override fun sayHelloServerStream(
            req: HelloRequest,
            responseObserver: StreamObserver<HelloReply>,
        ) {
            repeat(SERVER_STREAM_REPLIES) { i ->
                responseObserver.onNext(
                    HelloReply.newBuilder().setMessage("Hello ${req.name}, part ${i + 1} (Server Stream)").build(),
                )
                Thread.sleep(STREAM_REPLY_DELAY_MS)
            }
            responseObserver.onCompleted()
        }

        override fun sayHelloClientStream(responseObserver: StreamObserver<HelloReply>): StreamObserver<HelloRequest> {
            val names = StringBuilder()
            return object : StreamObserver<HelloRequest> {
                override fun onNext(value: HelloRequest) {
                    if (names.isNotEmpty()) names.append(", ")
                    names.append(value.name)
                }

                override fun onError(t: Throwable) {
                    responseObserver.onError(
                        io.grpc.Status
                            .fromThrowable(t)
                            .asRuntimeException(),
                    )
                }

                override fun onCompleted() {
                    responseObserver.onNext(HelloReply.newBuilder().setMessage("Hello $names! (Client Stream)").build())
                    responseObserver.onCompleted()
                }
            }
        }

        override fun sayHelloBidiStream(responseObserver: StreamObserver<HelloReply>): StreamObserver<HelloRequest> =
            object : StreamObserver<HelloRequest> {
                override fun onNext(value: HelloRequest) {
                    responseObserver.onNext(
                        HelloReply.newBuilder().setMessage("Ack: ${value.name} (Bidi Stream)").build(),
                    )
                }

                override fun onError(t: Throwable) {
                    responseObserver.onError(
                        io.grpc.Status
                            .fromThrowable(t)
                            .asRuntimeException(),
                    )
                }

                override fun onCompleted() {
                    responseObserver.onCompleted()
                }
            }
    }

    private companion object {
        const val TAG = "SampleGrpcServer"
        const val SHUTDOWN_TIMEOUT_SEC = 5L
        const val SERVER_STREAM_REPLIES = 3
        const val STREAM_REPLY_DELAY_MS = 300L
    }
}
