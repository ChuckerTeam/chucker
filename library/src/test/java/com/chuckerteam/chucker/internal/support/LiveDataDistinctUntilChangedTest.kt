package com.chuckerteam.chucker.internal.support

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import com.chuckerteam.chucker.util.test
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

internal class LiveDataDistinctUntilChangedTest {
    @get:Rule val instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `downstream emits initial upstream data`() {
        val upstream = MutableLiveData<Any?>(null)

        upstream.distinctUntilChanged().test {
            assertThat(expectData()).isNull()
        }
    }

    @Test
    fun `downstream does not emit if the upstream is empty`() {
        val upstream = MutableLiveData<Any?>()

        upstream.distinctUntilChanged().test {
            expectNoData()
        }
    }

    @Test
    fun `downstream emits new values`() {
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
    fun `downstream does not emit repeated data`() {
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
    fun `downstream emits according to distinct filter`() {
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
