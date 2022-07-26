package com.chuckerteam.chucker.internal.support

import android.annotation.SuppressLint
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import java.util.concurrent.Executor

internal fun <T1, T2, R> LiveData<T1>.combineLatest(
    other: LiveData<T2>,
    func: (T1, T2) -> R
): LiveData<R> {
    return MediatorLiveData<R>().apply {
        var lastA: T1? = null
        var lastB: T2? = null

        addSource(this@combineLatest) {
            lastA = it
            val observedB = lastB
            if (it == null && value != null) {
                value = null
            } else if (it != null && observedB != null) {
                value = func(it, observedB)
            }
        }

        addSource(other) {
            lastB = it
            val observedA = lastA
            if (it == null && value != null) {
                value = null
            } else if (observedA != null && it != null) {
                value = func(observedA, it)
            }
        }
    }
}

internal fun <T1, T2> LiveData<T1>.combineLatest(other: LiveData<T2>): LiveData<Pair<T1, T2>> {
    return combineLatest(other) { a, b -> a to b }
}

// Unlike built-in extension operation is performed on a provided thread pool.
// This is needed in our case since we compare requests and responses which can be big
// and result in frame drops.
internal fun <T> LiveData<T>.distinctUntilChanged(
    executor: Executor = ioExecutor(),
    areEqual: (old: T, new: T) -> Boolean = { old, new -> old == new }
): LiveData<T> {
    val distinctMediator = MediatorLiveData<T>()
    var old = uninitializedToken
    distinctMediator.addSource(this) { new ->
        executor.execute {
            @Suppress("UNCHECKED_CAST")
            if (old === uninitializedToken || !areEqual(old as T, new)) {
                old = new
                distinctMediator.postValue(new)
            }
        }
    }
    return distinctMediator
}

private val uninitializedToken: Any? = Any()

// It is lesser evil than providing a custom executor.
@SuppressLint("RestrictedApi")
private fun ioExecutor() = ArchTaskExecutor.getIOThreadExecutor()
