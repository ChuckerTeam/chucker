package com.chuckerteam.chucker.internal.support

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

internal fun <T> LiveData<T>.observeOnce(observer: Observer<T>) {
    observeForever(object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}
