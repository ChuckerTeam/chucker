package com.chuckerteam.chucker.internal.support

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LiveDataCombineLatestTest {
    @get:Rule val instantExecutorRule = InstantTaskExecutorRule()

    private val liveDataA = MutableLiveData<Boolean>()
    private val liveDataB = MutableLiveData<Int>()

    private val combinedData = liveDataA.combineLatest(liveDataB)
    private val results = mutableListOf<Pair<Boolean, Int>>()
    private val observer = Observer<Pair<Boolean, Int>> { results += it }

    @Before
    fun setUp() {
        combinedData.observeForever(observer)
    }

    @After
    fun tearDown() {
        combinedData.removeObserver(observer)
        results.clear()
    }

    @Test
    fun firstEmptyValue_preventsDownstreamEmissions() {
        liveDataB.value = 1
        liveDataB.value = 2
        liveDataB.value = 3

        assertEquals(emptyList<Pair<Boolean, Int>>(), results)
    }

    @Test
    fun secondEmptyValue_preventsDownstreamEmissions() {
        liveDataA.value = true
        liveDataA.value = false

        assertEquals(emptyList<Pair<Boolean, Int>>(), results)
    }

    @Test
    fun bothEmittedValues_areCombinedDownstream() {
        liveDataA.value = true
        liveDataB.value = 1

        assertEquals(listOf(true to 1), results)
    }

    @Test
    fun lastFirstValue_isCombinedWithNewestSecondValues() {
        liveDataA.value = true
        liveDataB.value = 1
        liveDataB.value = 2

        assertEquals(listOf(true to 1, true to 2), results)
    }

    @Test
    fun lastSecondValue_isCombinedWithNewestFirstValues() {
        liveDataA.value = true
        liveDataB.value = 1
        liveDataA.value = false

        assertEquals(listOf(true to 1, false to 1), results)
    }
}
