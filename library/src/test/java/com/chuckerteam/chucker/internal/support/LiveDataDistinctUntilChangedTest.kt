package com.chuckerteam.chucker.internal.support

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import com.chuckerteam.chucker.test
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test

class LiveDataDistinctUntilChangedTest {
    @get:Rule val instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun initialUpstreamData_isEmittedDownstream() {
        val upstream = MutableLiveData<Any?>(null)

        upstream.distinctUntilChanged().test {
            assertEquals(null, expectData())
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
            assertEquals(1, expectData())

            upstream.value = 2
            assertEquals(2, expectData())

            upstream.value = null
            assertEquals(null, expectData())

            upstream.value = 2
            assertEquals(2, expectData())
        }
    }

    @Test
    fun newIndistinctData_isNotEmittedDownstream() {
        val upstream = MutableLiveData<String?>()

        upstream.distinctUntilChanged().test {
            upstream.value = null
            assertEquals(null, expectData())

            upstream.value = null
            expectNoData()

            upstream.value = ""
            assertEquals("", expectData())

            upstream.value = ""
            expectNoData()
        }
    }

    @Test
    fun customFunction_canBeUsedToDistinguishData() {
        val upstream = MutableLiveData<Pair<Int, String>>()

        upstream.distinctUntilChanged { old, new -> old.first == new.first }.test {
            upstream.value = 1 to ""
            assertEquals(1 to "", expectData())

            upstream.value = 1 to "a"
            expectNoData()

            upstream.value = 2 to "b"
            assertEquals(2 to "b", expectData())

            upstream.value = 3 to "b"
            assertEquals(3 to "b", expectData())
        }
    }
}
