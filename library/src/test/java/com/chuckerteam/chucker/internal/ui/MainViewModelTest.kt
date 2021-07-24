package com.chuckerteam.chucker.internal.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.chuckerteam.chucker.api.Group
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import com.chuckerteam.chucker.internal.data.repository.HttpTransactionRepository
import com.google.common.truth.Truth
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

internal class MainViewModelTest {

    lateinit var sut: MainViewModel

    @MockK
    lateinit var transaction: HttpTransactionRepository

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        sut = MainViewModel(transaction)
        every { transaction.getSortedTransactionTuples() } returns MutableLiveData()
    }

    @Test
    fun `WHEN add group is called THEN Transaction getFilteredTransactionTuples is called`() {
        val group = Group(name = "Group 1", urls = listOf("url1"))

        every { transaction.getFilteredTransactionTuples(any(), any(), group.urls) } returns MutableLiveData()
        sut.transactions.observeForTesting {
            sut.addGroup(group)

            verify(exactly = 1) {
                transaction.getFilteredTransactionTuples("", "", group.urls)
            }
        }
    }

    @Test
    fun `WHEN add group is called THEN it sets a value to transactions liveData`() {
        val group = Group(name = "Group 1", urls = listOf("url1"))
        val livedata = MutableLiveData<List<HttpTransactionTuple>>()
        every { transaction.getFilteredTransactionTuples(any(), any(), group.urls) } returns livedata

        livedata.value = listOf(createTransactionTupple(1), createTransactionTupple(2))
        sut.addGroup(group)
        val value = sut.transactions.getOrAwaitValue()

        Truth.assertThat(value).hasSize(2)
    }

    @Test
    fun `WHEN remove group is called THEN Transaction getFilteredTransactionTuples is called`() {
        val groupOne = Group(name = "Group 1", urls = listOf("url1"))
        val groupTwo = Group(name = "Group 2", urls = listOf("url2"))

        every { transaction.getFilteredTransactionTuples(any(), any(), any()) } returns MutableLiveData()
        sut.transactions.observeForTesting {
            sut.addGroup(groupOne)
            sut.addGroup(groupTwo)
            sut.removeGroup(groupTwo)

            verify(exactly = 2) {
                transaction.getFilteredTransactionTuples("", "", groupOne.urls)
            }
            verify(exactly = 1) {
                transaction.getFilteredTransactionTuples(
                    "",
                    "",
                    listOf(groupOne, groupTwo).map { it.urls }.flatten()
                )
            }
        }
    }

    private fun createTransactionTupple(id: Long) = HttpTransactionTuple(
        id = id,
        requestDate = null,
        tookMs = null,
        protocol = null,
        method = null,
        host = null,
        path = null,
        scheme = null,
        responseCode = null,
        requestPayloadSize = null,
        responsePayloadSize = null,
        error = null,
        url = null
    )
}

private fun <T> LiveData<T>.observeForTesting(block: () -> Unit) {
    val observer = Observer<T> { }
    try {
        observeForever(observer)
        block()
    } finally {
        removeObserver(observer)
    }
}

private fun <T> LiveData<T>.getOrAwaitValue(): T {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(o: T?) {
            data = o
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }
    this.observeForever(observer)

    // Don't wait indefinitely if the LiveData is not set.
    if (!latch.await(2, TimeUnit.SECONDS)) {
        this.removeObserver(observer)
        throw TimeoutException("LiveData value was never set.")
    }

    @Suppress("UNCHECKED_CAST")
    return data as T
}
