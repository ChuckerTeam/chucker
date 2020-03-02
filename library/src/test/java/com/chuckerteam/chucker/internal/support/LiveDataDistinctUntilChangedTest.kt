package com.chuckerteam.chucker.internal.support

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import com.chuckerteam.chucker.test
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

class LiveDataDistinctUntilChangedTest {
    @get:Rule val instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun initialUpstreamData_isEmittedDownstream() {
        val upstream = MutableLiveData<Any?>(null)

        upstream.distinctUntilChanged().test {
            assertThat(expectData()).isNull()
        }
    }

    @Test
    fun emptyUpstream_isNotEmittedDownstream() {
        val upstream = MutableLiveData<Any?>()

        upstream.distinctUntilChanged().test {
            expectNoData()
        }
    }

    @Test
    fun newDistinctData_isEmittedDownstream() {
        val upstream = MutableLiveData<Int?>()

        upstream.distinctUntilChanged().test {
            upstream.value = 1
            assertThat(expectData()).isEqualTo(1)

            upstream.value = 2
            assertThat(expectData()).isEqualTo(2)

            upstream.value = null
            assertThat(expectData()).isNull()

            upstream.value = 2
            assertThat(expectData()).isEqualTo(2)
        }
    }

    @Test
    fun newIndistinctData_isNotEmittedDownstream() {
        val upstream = MutableLiveData<String?>()

        upstream.distinctUntilChanged().test {
            upstream.value = null
            assertThat(expectData()).isNull()

            upstream.value = null
            expectNoData()

            upstream.value = ""
            assertThat(expectData()).isEmpty()

            upstream.value = ""
            expectNoData()
        }
    }

    @Test
    fun customFunction_canBeUsedToDistinguishData() {
        val upstream = MutableLiveData<Pair<Int, String>>()

        upstream.distinctUntilChanged { old, new -> old.first == new.first }.test {
            upstream.value = 1 to ""
            assertThat(expectData()).isEqualTo(1 to "")

            upstream.value = 1 to "a"
            expectNoData()

            upstream.value = 2 to "b"
            assertThat(expectData()).isEqualTo(2 to "b")

            upstream.value = 3 to "b"
            assertThat(expectData()).isEqualTo(3 to "b")
        }
    }
}
