package com.chuckerteam.chucker.sample

import android.app.Application
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.RetentionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SampleApplication : Application() {

    private val serverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val grpcServer by lazy { SampleGrpcServer(GRPC_PORT) }

    val chuckerCollector by lazy {
        ChuckerCollector(
            context = this,
            showNotification = true,
            retentionPeriod = RetentionManager.Period.ONE_HOUR,
        )
    }

    override fun onCreate() {
        super.onCreate()
        serverScope.launch { grpcServer.start() }
    }

    override fun onTerminate() {
        super.onTerminate()
        serverScope.launch { grpcServer.stop() }
    }

    companion object {
        const val GRPC_PORT = 50051
    }
}
