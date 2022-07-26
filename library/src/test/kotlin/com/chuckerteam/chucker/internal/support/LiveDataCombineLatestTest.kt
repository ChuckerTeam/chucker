package com.chuckerteam.chucker.internal.support

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.chuckerteam.chucker.util.test
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

internal class LiveDataCombineLatestTest {
    @get:Rule val instantExecutorRule = InstantTaskExecutorRule()

    private val inputA = MutableLiveData<Boolean>()
    private val inputB = MutableLiveData<Int>()

    private val upstream = inputA.combineLatest(inputB)

    @Test
    fun `downstream does not emit if the first source has no values`() {
        upstream.test {
            inputB.value = 1
            inputB.value = 2
            inputB.value = 3

            expectNoData()
        }
    }

    @Test
    fun `downstream does not emit if the second source has no values`() {
        upstream.test {
            inputA.value = true
            inputA.value = false

            expectNoData()
        }
    }

    @Test
    fun `downstream combines source values`() {
        upstream.test {
            inputA.value = true
            inputB.value = 1

            assertThat(expectData()).isEqualTo(true to 1)
        }
    }

    @Test
    fun `downstream updates with updates to the second source`() {
        upstream.test {
            inputA.value = true
            inputB.value = 1
            assertThat(expectData()).isEqualTo(true to 1)

            inputB.value = 2
            assertThat(expectData()).isEqualTo(true to 2)
        }
    }

    @Test
    fun `downstream updates with updates to the first source`() {
        upstream.test {
            inputA.value = true
            inputB.value = 1
            assertThat(expectData()).isEqualTo(true to 1)

            inputA.value = false
            assertThat(expectData()).isEqualTo(false to 1)
        }
    }
}
