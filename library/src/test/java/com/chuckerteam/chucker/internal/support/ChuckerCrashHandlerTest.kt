package com.chuckerteam.chucker.internal.support

import com.chuckerteam.chucker.api.ChuckerCollector
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class ChuckerCrashHandlerTest {

    @Test
    fun uncaughtException_isReportedCorrectly() {
        val mockCollector = mockk<ChuckerCollector>()
        val mockThrowable = Throwable()
        val handler = ChuckerCrashHandler(mockCollector)
        every { mockCollector.onError(any(), any()) } returns Unit

        handler.uncaughtException(Thread.currentThread(), mockThrowable)

        verify { mockCollector.onError("Error caught on ${Thread.currentThread().name} thread", mockThrowable) }
    }
}
