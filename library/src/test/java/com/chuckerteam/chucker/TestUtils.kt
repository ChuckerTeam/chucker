package com.chuckerteam.chucker

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.io.File
import okio.Buffer
import okio.Okio

fun getResourceFile(file: String): Buffer {
    return Buffer().apply {
        writeAll(Okio.buffer(Okio.source(File("./src/test/resources/$file"))))
    }
}

fun <T> LiveData<T>.test(test: LiveDataRecord<T>.() -> Unit) {
    val observer = RecordingObserver<T>()
    observeForever(observer)
    LiveDataRecord(observer).test()
    removeObserver(observer)
    observer.records.clear()
}

class LiveDataRecord<T> internal constructor(
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
