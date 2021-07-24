package com.chuckerteam.chucker.internal.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.chuckerteam.chucker.api.Group
import com.chuckerteam.chucker.internal.data.repository.HttpTransactionRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

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
