package com.chuckerteam.chucker.internal.support

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.chuckerteam.chucker.test
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

class LiveDataCombineLatestTest {
    @get:Rule val instantExecutorRule = InstantTaskExecutorRule()

    private val inputA = MutableLiveData<Boolean>()
    private val inputB = MutableLiveData<Int>()

    private val upstream = inputA.combineLatest(inputB)

    @Test
    fun firstEmptyValue_preventsDownstreamEmissions() {
        upstream.test {
            inputB.value = 1
            inputB.value = 2
            inputB.value = 3

            expectNoData()
        }
    }

    @Test
    fun secondEmptyValue_preventsDownstreamEmissions() {
        upstream.test {
            inputA.value = true
            inputA.value = false

            expectNoData()
        }
    }

    @Test
    fun bothEmittedValues_areCombinedDownstream() {
        upstream.test {
            inputA.value = true
            inputB.value = 1

            assertThat(expectData()).isEqualTo(true to 1)
        }
    }

    @Test
    fun lastFirstValue_isCombinedWithNewestSecondValues() {
        upstream.test {
            inputA.value = true
            inputB.value = 1
            assertThat(expectData()).isEqualTo(true to 1)

            inputB.value = 2
            assertThat(expectData()).isEqualTo(true to 2)
        }
    }

    @Test
    fun lastSecondValue_isCombinedWithNewestFirstValues() {
        upstream.test {
            inputA.value = true
            inputB.value = 1
            assertThat(expectData()).isEqualTo(true to 1)

            inputA.value = false
            assertThat(expectData()).isEqualTo(false to 1)
        }
    }
}
