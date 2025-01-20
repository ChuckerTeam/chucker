package com.chuckerteam.chucker.internal.ui

import android.text.TextUtils
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import com.chuckerteam.chucker.internal.data.repository.HttpTransactionRepository
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.internal.support.NotificationHelper
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

@ExperimentalCoroutinesApi
internal class MainViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    private lateinit var transactionRepository: HttpTransactionRepository

    @MockK
    private lateinit var transactionObserver: Observer<List<HttpTransactionTuple>>

    private lateinit var viewModel: MainViewModel

    private val emptyTransactionList = MutableLiveData<List<HttpTransactionTuple>>()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        mockkStatic(TextUtils::class)

        every { transactionObserver.onChanged(any()) } just runs

        every { transactionRepository.getSortedTransactionTuples() } returns emptyTransactionList
        every {
            transactionRepository.getFilteredTransactionTuples(
                any(),
                any(),
            )
        } returns emptyTransactionList

        mockkObject(RepositoryProvider)
        every { RepositoryProvider.transaction() } returns transactionRepository

        mockkObject(NotificationHelper)
        every { NotificationHelper.clearBuffer() } just runs

        viewModel = MainViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `when search query is empty, getSortedTransactionTuples is called`() =
        runTest {
            val expectedTuples = listOf(mockk<HttpTransactionTuple>(relaxed = true))
            val transactionLiveData = MutableLiveData<List<HttpTransactionTuple>>()
            every { transactionRepository.getSortedTransactionTuples() } returns transactionLiveData
            every { TextUtils.isDigitsOnly(any()) } returns false

            viewModel.transactions.observeForever(transactionObserver)
            viewModel.updateItemsFilter("")
            transactionLiveData.value = expectedTuples

            verify { transactionRepository.getSortedTransactionTuples() }
            verify { transactionObserver.onChanged(expectedTuples) }
        }

    @Test
    fun `when search query contains only digits, getFilteredTransactionTuples is called with correct parameters`() =
        runTest {
            val searchQuery = "123"
            val expectedTuples = listOf(mockk<HttpTransactionTuple>(relaxed = true))
            val transactionLiveData = MutableLiveData<List<HttpTransactionTuple>>()
            every {
                transactionRepository.getFilteredTransactionTuples(
                    searchQuery,
                    "",
                )
            } returns transactionLiveData
            every { TextUtils.isDigitsOnly(searchQuery) } returns true

            viewModel.transactions.observeForever(transactionObserver)
            viewModel.updateItemsFilter(searchQuery)
            transactionLiveData.value = expectedTuples

            verify { transactionRepository.getFilteredTransactionTuples(searchQuery, "") }
            verify { transactionObserver.onChanged(expectedTuples) }
        }

    @Test
    fun `when search query contains text, getFilteredTransactionTuples is called with correct parameters`() =
        runTest {
            val searchQuery = "test"
            val expectedTuples = listOf(mockk<HttpTransactionTuple>(relaxed = true))
            val transactionLiveData = MutableLiveData<List<HttpTransactionTuple>>()
            every {
                transactionRepository.getFilteredTransactionTuples(
                    "",
                    searchQuery,
                )
            } returns transactionLiveData
            every { TextUtils.isDigitsOnly(searchQuery) } returns false

            viewModel.transactions.observeForever(transactionObserver)
            viewModel.updateItemsFilter(searchQuery)
            transactionLiveData.value = expectedTuples

            verify { transactionRepository.getFilteredTransactionTuples("", searchQuery) }
            verify { transactionObserver.onChanged(expectedTuples) }
        }

    @Test
    fun `getAllTransactions returns repository data`() =
        runTest {
            val expectedTransactions = listOf(mockk<HttpTransaction>(relaxed = true))
            coEvery { transactionRepository.getAllTransactions() } returns expectedTransactions

            val result = viewModel.getAllTransactions()

            assertEquals(expectedTransactions, result)
            coVerify { transactionRepository.getAllTransactions() }
        }

    @Test
    fun `clearTransactions clears repository and notification buffer`() =
        runTest {
            coEvery { transactionRepository.deleteAllTransactions() } just runs

            viewModel.clearTransactions()
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { transactionRepository.deleteAllTransactions() }
            verify { NotificationHelper.clearBuffer() }
        }
}
