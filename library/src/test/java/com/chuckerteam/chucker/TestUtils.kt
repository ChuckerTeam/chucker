package com.chuckerteam.chucker

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.chuckerteam.chucker.internal.support.hasBody
import okhttp3.Response
import okio.Buffer
import okio.ByteString
import okio.buffer
import okio.source
import java.io.File

internal const val SEGMENT_SIZE = 8_192L

internal fun getResourceFile(file: String): Buffer {
    return Buffer().apply {
        writeAll(File("./src/test/resources/$file").source().buffer())
    }
}

internal fun Response.readByteStringBody(length: Long? = null): ByteString? {
    return if (hasBody()) {
        body?.source()?.use { source ->
            if (length == null) {
                source.readByteString()
            } else {
                source.readByteString(length)
            }
        }
    } else {
        null
    }
}

internal fun <T> LiveData<T>.test(test: LiveDataRecord<T>.() -> Unit) {
    val observer = RecordingObserver<T>()
    observeForever(observer)
    LiveDataRecord(observer).test()
    removeObserver(observer)
    observer.records.clear()
}

internal class LiveDataRecord<T> internal constructor(
    private val observer: RecordingObserver<T>
) {
    fun expectData(): T {
        if (observer.records.isEmpty()) {
            throw AssertionError("Expected data but was empty.")
        }
        return observer.records.removeAt(0)
    }

    fun expectNoData() {
        if (observer.records.isNotEmpty()) {
            val data = observer.records[0]
            throw AssertionError("Expected no data but was $data.")
        }
    }
}

internal class RecordingObserver<T> : Observer<T> {
    val records = mutableListOf<T>()

    override fun onChanged(data: T) {
        records += data
    }
}
