package com.chuckerteam.chucker.internal.ui.transaction

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.chuckerteam.chucker.internal.InstantExecutorExtension
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import com.chuckerteam.chucker.internal.data.repository.HttpTransactionRepository
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class)
internal class TransactionViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @MockK
    private lateinit var transactionRepository: HttpTransactionRepository

    @MockK
    private lateinit var transactionObserver: Observer<List<HttpTransactionTuple>>

    @MockK
    private lateinit var encodeUrlObserver: Observer<Boolean>

    @MockK
    private lateinit var transactionTitleObserver: Observer<String>

    @MockK
    private lateinit var doesUrlRequireEncodingObserver: Observer<Boolean>

    @MockK
    private lateinit var doesRequestBodyRequireEncodingObserver: Observer<Boolean>

    @MockK
    private lateinit var formatRequestBodyObserver: Observer<Boolean>

    private val transaction = MutableLiveData<HttpTransaction?>()

    private lateinit var viewModel: TransactionViewModel

    private val mockTransactionId = 0L

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        every { transactionObserver.onChanged(any()) } just runs
        every { encodeUrlObserver.onChanged(any()) } just runs
        every { transactionTitleObserver.onChanged(any()) } just runs
        every { doesUrlRequireEncodingObserver.onChanged(any()) } just runs
        every { doesRequestBodyRequireEncodingObserver.onChanged(any()) } just runs
        every { formatRequestBodyObserver.onChanged(any()) } just runs
        every { transactionRepository.getTransaction(any()) } returns transaction
        mockkObject(RepositoryProvider)
        every { RepositoryProvider.transaction() } returns transactionRepository
        viewModel = TransactionViewModel(transactionId = mockTransactionId)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `encodeUrl defaults to false`() {
        assertFalse(viewModel.encodeUrl.value!!)
    }

    @Test
    fun `encodeUrl setter changes value`() {
        viewModel.encodeUrl.observeForever(encodeUrlObserver)
        viewModel.encodeUrl(true)
        assertTrue(viewModel.encodeUrl.value!!)

        viewModel.encodeUrl(false)
        assertFalse(viewModel.encodeUrl.value!!)
    }

    @Test
    fun `switchUrlEncoding toggles encodeUrl`() {
        viewModel.switchUrlEncoding()
        assertTrue(viewModel.encodeUrl.value!!)

        viewModel.switchUrlEncoding()
        assertFalse(viewModel.encodeUrl.value!!)
    }

    @Test
    fun `transactionTitle formats raw path when encoding off`() {
        viewModel.transactionTitle.observeForever(transactionTitleObserver)
        val txn = mockk<HttpTransaction>()
        every { txn.method } returns "POST"
        every { txn.getFormattedPath(encode = false) } returns "/foo?bar=1"
        every { txn.getFormattedPath(encode = true) } returns "/foo%3Fbar%3D1"
        transaction.value = txn

        viewModel.encodeUrl(false)

        assertEquals("POST /foo?bar=1", viewModel.transactionTitle.value)
    }

    @Test
    fun `transactionTitle formats encoded path when encoding on`() {
        viewModel.transactionTitle.observeForever(transactionTitleObserver)
        val txn = mockk<HttpTransaction>()
        every { txn.method } returns "GET"
        every { txn.getFormattedPath(encode = false) } returns "/baz"
        every { txn.getFormattedPath(encode = true) } returns "/baz%20encoded"
        transaction.value = txn

        viewModel.encodeUrl(true)

        assertEquals("GET /baz%20encoded", viewModel.transactionTitle.value)
    }

    @Test
    fun `doesUrlRequireEncoding false when paths identical`() {
        viewModel.doesUrlRequireEncoding.observeForever(doesUrlRequireEncodingObserver)
        val txn = mockk<HttpTransaction>()
        every { txn.getFormattedPath(true) } returns "/same"
        every { txn.getFormattedPath(false) } returns "/same"
        transaction.value = txn

        assertFalse(viewModel.doesUrlRequireEncoding.value!!)
    }

    @Test
    fun `doesUrlRequireEncoding true when paths differ`() {
        viewModel.doesUrlRequireEncoding.observeForever(doesUrlRequireEncodingObserver)
        val txn = mockk<HttpTransaction>()
        every { txn.getFormattedPath(true) } returns "/enc"
        every { txn.getFormattedPath(false) } returns "/raw"
        transaction.value = txn

        assertTrue(viewModel.doesUrlRequireEncoding.value!!)
    }

    @Test
    fun `doesRequestBodyRequireEncoding handles null and non‐form content`() {
        viewModel.doesRequestBodyRequireEncoding.observeForever(
            doesRequestBodyRequireEncodingObserver,
        )
        val txn1 = mockk<HttpTransaction>()
        every { txn1.requestContentType } returns null
        transaction.value = txn1
        assertFalse(viewModel.doesRequestBodyRequireEncoding.value!!)

        val txn2 = mockk<HttpTransaction>()
        every { txn2.requestContentType } returns "application/json"
        transaction.value = txn2
        assertFalse(viewModel.doesRequestBodyRequireEncoding.value!!)
    }

    @Test
    fun `doesRequestBodyRequireEncoding true for form-urlencoded`() {
        viewModel.doesRequestBodyRequireEncoding.observeForever(
            doesRequestBodyRequireEncodingObserver,
        )
        val txn = mockk<HttpTransaction>()
        every { txn.requestContentType } returns "application/x-www-form-urlencoded; charset=UTF-8"
        transaction.value = txn

        assertTrue(viewModel.doesRequestBodyRequireEncoding.value ?: false)
    }

    @Test
    fun `formatRequestBody true when bodyReq false, encodeUrl false`() {
        viewModel.formatRequestBody.observeForever(formatRequestBodyObserver)
        viewModel.encodeUrl(false)
        transaction.value = mockk { every { requestContentType } returns null }
        assertTrue(viewModel.formatRequestBody.value!!)
    }

    @Test
    fun `formatRequestBody true when bodyReq false, encodeUrl true`() {
        viewModel.formatRequestBody.observeForever(formatRequestBodyObserver)
        viewModel.encodeUrl(true)
        transaction.value = mockk { every { requestContentType } returns null }
        assertTrue(viewModel.formatRequestBody.value!!)
    }

    @Test
    fun `formatRequestBody true when bodyReq true, encodeUrl false`() {
        viewModel.formatRequestBody.observeForever(formatRequestBodyObserver)
        viewModel.encodeUrl(false)
        transaction.value = mockk { every { requestContentType } returns "x-www-form-urlencoded" }
        assertTrue(viewModel.formatRequestBody.value!!)
    }

    @Test
    fun `formatRequestBody false when bodyReq true, encodeUrl true`() {
        viewModel.formatRequestBody.observeForever(formatRequestBodyObserver)
        viewModel.encodeUrl(true)
        transaction.value = mockk { every { requestContentType } returns "x-www-form-urlencoded" }
        assertFalse(viewModel.formatRequestBody.value!!)
    }

    @Test
    fun `transaction property returns repository LiveData`() {
        assertSame(transaction, viewModel.transaction)
    }
}
