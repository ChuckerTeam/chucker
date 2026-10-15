package com.chuckerteam.chucker.internal.ui

import android.text.TextUtils
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import java.text.DateFormat

@ExperimentalCoroutinesApi
internal class MainViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    private lateinit var transactionRepository: HttpTransactionRepository

    private lateinit var viewModel: MainViewModel

    private val emptyTransactionList = MutableLiveData<List<HttpTransactionTuple>>()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        mockkStatic(TextUtils::class)

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
            val expectedTuples = listOf(tuple(id = 1L))
            val transactionLiveData = MutableLiveData<List<HttpTransactionTuple>>()
            every { transactionRepository.getSortedTransactionTuples() } returns transactionLiveData
            every { TextUtils.isDigitsOnly(any()) } returns false

            viewModel.updateItemsFilter("")
            transactionLiveData.value = expectedTuples

            assertEquals(
                listOf(expectedUi(id = 1L)),
                viewModel.transactions.first { it.isNotEmpty() },
            )
            verify { transactionRepository.getSortedTransactionTuples() }
        }

    @Test
    fun `when search query contains only digits, getFilteredTransactionTuples is called with correct parameters`() =
        runTest {
            val searchQuery = "123"
            val expectedTuples = listOf(tuple(id = 2L))
            val transactionLiveData = MutableLiveData<List<HttpTransactionTuple>>()
            every {
                transactionRepository.getFilteredTransactionTuples(
                    searchQuery,
                    "",
                )
            } returns transactionLiveData
            every { TextUtils.isDigitsOnly(searchQuery) } returns true

            viewModel.updateItemsFilter(searchQuery)
            transactionLiveData.value = expectedTuples

            assertEquals(
                listOf(expectedUi(id = 2L)),
                viewModel.transactions.first { it.isNotEmpty() },
            )
            verify { transactionRepository.getFilteredTransactionTuples(searchQuery, "") }
        }

    @Test
    fun `when search query contains text, getFilteredTransactionTuples is called with correct parameters`() =
        runTest {
            val searchQuery = "test"
            val expectedTuples = listOf(tuple(id = 3L))
            val transactionLiveData = MutableLiveData<List<HttpTransactionTuple>>()
            every {
                transactionRepository.getFilteredTransactionTuples(
                    "",
                    searchQuery,
                )
            } returns transactionLiveData
            every { TextUtils.isDigitsOnly(searchQuery) } returns false

            viewModel.updateItemsFilter(searchQuery)
            transactionLiveData.value = expectedTuples

            assertEquals(
                listOf(expectedUi(id = 3L)),
                viewModel.transactions.first { it.isNotEmpty() },
            )
            verify { transactionRepository.getFilteredTransactionTuples("", searchQuery) }
        }

    @Test
    fun `getTransactions returns repository data`() =
        runTest {
            val expectedTransactions = listOf(mockk<HttpTransaction>(relaxed = true))
            coEvery { transactionRepository.getAllTransactions() } returns expectedTransactions

            val result = viewModel.getTransactions()

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

    @Test
    fun `toggleSelection should add item when not selected`() {
        val id = 1L
        viewModel.toggleSelection(id)

        val selected = viewModel.getSelectedIds()
        assertEquals(listOf(id), selected)
        assertEquals(true, viewModel.isItemSelected.value)
    }

    @Test
    fun `toggleSelection should remove item when already selected`() {
        val id = 1L
        viewModel.toggleSelection(id)
        viewModel.toggleSelection(id)

        val selected = viewModel.getSelectedIds()
        assertEquals(emptyList<Long>(), selected)
        assertEquals(false, viewModel.isItemSelected.value)
    }

    @Test
    fun `startSelection should toggle item if already in selection mode`() {
        val id = 1L
        viewModel.toggleSelection(id)
        viewModel.startSelection(id)

        val selected = viewModel.getSelectedIds()
        assertEquals(emptyList<Long>(), selected)
        assertEquals(false, viewModel.isItemSelected.value)
    }

    @Test
    fun `startSelection should start selection mode if not already active`() {
        val id = 2L
        viewModel.startSelection(id)

        val selected = viewModel.getSelectedIds()
        assertEquals(listOf(id), selected)
        assertEquals(true, viewModel.isItemSelected.value)
    }

    @Test
    fun `getSelectedIds should return currently selected IDs`() {
        val ids = listOf(1L, 2L)
        ids.forEach { viewModel.toggleSelection(it) }

        val result = viewModel.getSelectedIds()
        assertEquals(ids, result)
    }

    @Test
    fun `restoreSelection should update selected IDs and state`() {
        val restoredIds = listOf(1L, 2L, 3L)

        viewModel.restoreSelection(restoredIds)

        assertEquals(restoredIds, viewModel.getSelectedIds())
        assertEquals(true, viewModel.isItemSelected.value)
    }

    @Test
    fun `restoreSelection with empty list should clear selection state`() {
        viewModel.restoreSelection(emptyList())

        assertEquals(emptyList<Long>(), viewModel.getSelectedIds())
        assertEquals(false, viewModel.isItemSelected.value)
    }

    private fun tuple(id: Long) =
        HttpTransactionTuple(
            id = id,
            requestDate = 1L,
            tookMs = 12L,
            protocol = "HTTP/1.1",
            method = "GET",
            host = "example.org",
            path = "/$id",
            scheme = "https",
            responseCode = 200,
            requestPayloadSize = 3L,
            responsePayloadSize = 4L,
            error = null,
            graphQlDetected = false,
            graphQlOperationName = null,
        )

    private fun expectedUi(id: Long) =
        TransactionUi(
            id = id,
            requestDate = DateFormat.getTimeInstance().format(1L),
            host = "example.org",
            path = "GET /$id",
            isSsl = true,
            responseCode = 200,
            status = HttpTransaction.Status.Complete,
            duration = "12 ms",
            totalSize = "7 B",
            graphQlOperationName = null,
            isGraphQl = false,
        )
}
