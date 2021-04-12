package com.chuckerteam.chucker.internal.data.repository

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.chuckerteam.chucker.internal.data.entity.assertTuples
import com.chuckerteam.chucker.internal.data.entity.createRequest
import com.chuckerteam.chucker.internal.data.entity.randomString
import com.chuckerteam.chucker.internal.data.entity.withResponseData
import com.chuckerteam.chucker.internal.data.room.ChuckerDatabase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class HttpTransactionDatabaseRepositoryTest {
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: ChuckerDatabase
    private lateinit var testObject: HttpTransactionDatabaseRepository

    private val transaction = createRequest().apply {
        id = 123456L
        host = randomString()
        requestDate = 100
    }
    private val otherTransaction = createRequest().apply {
        id = 234567L
        host = randomString()
        requestDate = 200
    }

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, ChuckerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        testObject = HttpTransactionDatabaseRepository(db)
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `get a single transaction`() = runBlocking {
        val data = createRequest().apply { id = 123L }
        db.transactionDao().insert(data)

        testObject.getTransaction(data.id).observeForever {
            assertThat(it?.id).isEqualTo(data.id)
            assertThat(it?.requestHeaders).isEqualTo(data.requestHeaders)
            assertThat(it?.url).isEqualTo(data.url)
            assertThat(it?.host).isEqualTo(data.host)
            assertThat(it?.path).isEqualTo(data.path)
            assertThat(it?.scheme).isEqualTo(data.scheme)
            assertThat(it?.isRequestBodyEncoded).isEqualTo(data.isRequestBodyEncoded)
            assertThat(it?.requestDate).isEqualTo(data.requestDate)
            assertThat(it?.method).isEqualTo(data.method)
            assertThat(it?.requestContentType).isEqualTo(data.requestContentType)
            assertThat(it?.requestPayloadSize).isEqualTo(data.requestPayloadSize)
        }
    }

    @Test
    fun `insert a single transaction`() = runBlocking {
        val data = createRequest()

        testObject.insertTransaction(data)
        assertThat(data.id > 0).isTrue()

        testObject.getTransaction(data.id).observeForever {
            assertThat(it?.id).isEqualTo(data.id)
            assertThat(it?.requestHeaders).isEqualTo(data.requestHeaders)
            assertThat(it?.url).isEqualTo(data.url)
            assertThat(it?.host).isEqualTo(data.host)
            assertThat(it?.path).isEqualTo(data.path)
            assertThat(it?.scheme).isEqualTo(data.scheme)
            assertThat(it?.isRequestBodyEncoded).isEqualTo(data.isRequestBodyEncoded)
            assertThat(it?.requestDate).isEqualTo(data.requestDate)
            assertThat(it?.method).isEqualTo(data.method)
            assertThat(it?.requestContentType).isEqualTo(data.requestContentType)
            assertThat(it?.requestPayloadSize).isEqualTo(data.requestPayloadSize)
        }
    }

    @Test
    fun `get all transactions`() = runBlocking {
        testObject.insertTransaction(transaction)
        testObject.insertTransaction(otherTransaction)

        val result = testObject.getAllTransactions()
        val first = result.firstOrNull { it.id == transaction.id }
        assertThat(first).isNotNull()
        assertThat(first?.host).isEqualTo(transaction.host)

        val second = result.firstOrNull { it.id == otherTransaction.id }
        assertThat(second).isNotNull()
        assertThat(second?.host).isEqualTo(otherTransaction.host)
    }

    @Test
    fun `get sorted transaction tuples`() = runBlocking {
        testObject.insertTransaction(transaction)
        testObject.insertTransaction(otherTransaction)

        testObject.getSortedTransactionTuples().observeForever { result ->
            assertTuples(listOf(otherTransaction, transaction), result)
        }
    }

    @Test
    fun `transaction tuples are not filtered with empty inputs`() = runBlocking {
        val transactionOne =
            createRequest("abc").withResponseData().apply {
                requestDate = 200L
            }
        val transactionTwo =
            createRequest("abcdef").withResponseData().apply {
                requestDate = 100L
            }
        val transactionThree =
            createRequest("def").withResponseData().apply {
                requestDate = 300L
            }

        testObject.insertTransaction(transactionOne)
        testObject.insertTransaction(transactionTwo)
        testObject.insertTransaction(transactionThree)

        testObject.getFilteredTransactionTuples(code = "", path = "").observeForever { result ->
            assertTuples(listOf(transactionThree, transactionOne, transactionTwo), result)
        }
    }

    @Test
    fun `transaction tuples are filtered by path`() = runBlocking {
        val transactionOne =
            createRequest("abc").withResponseData().apply {
                requestDate = 200L
            }
        val transactionTwo =
            createRequest("abcdef").withResponseData().apply {
                requestDate = 100L
            }
        val transactionThree =
            createRequest("def").withResponseData().apply {
                requestDate = 300L
            }

        testObject.insertTransaction(transactionOne)
        testObject.insertTransaction(transactionTwo)
        testObject.insertTransaction(transactionThree)

        testObject.getFilteredTransactionTuples(code = "", path = "def").observeForever { result ->
            assertTuples(listOf(transactionThree, transactionTwo), result)
        }
    }

    @Test
    fun `transaction tuples are filtered by code`() = runBlocking {
        val transactionOne =
            createRequest("abc").withResponseData().apply {
                requestDate = 200L
                responseCode = 418
            }
        val transactionTwo =
            createRequest("abcdef").withResponseData().apply {
                requestDate = 100L
                responseCode = 200
            }
        val transactionThree =
            createRequest("def").withResponseData().apply {
                requestDate = 300L
                responseCode = 400
            }

        testObject.insertTransaction(transactionOne)
        testObject.insertTransaction(transactionTwo)
        testObject.insertTransaction(transactionThree)

        testObject.getFilteredTransactionTuples(code = "4", path = "").observeForever { result ->
            assertTuples(listOf(transactionThree, transactionOne), result)
        }
    }

    @Test
    fun `delete all transactions`() = runBlocking {
        testObject.insertTransaction(transaction)
        testObject.insertTransaction(otherTransaction)

        val result = testObject.getAllTransactions()
        assertThat(result.size).isEqualTo(2)

        testObject.deleteAllTransactions()
        val empty = testObject.getAllTransactions()
        assertThat(empty.isEmpty()).isTrue()
    }

    @Test
    fun `delete old transactions`() = runBlocking {
        testObject.insertTransaction(transaction)
        testObject.insertTransaction(otherTransaction)

        val result = testObject.getAllTransactions()
        assertThat(result.size).isEqualTo(2)

        testObject.deleteOldTransactions(150L)

        val secondResult = testObject.getAllTransactions()
        assertThat(secondResult.size).isEqualTo(1)
        val second = secondResult.firstOrNull { it.id == otherTransaction.id }
        assertThat(second).isNotNull()
        assertThat(second?.host).isEqualTo(otherTransaction.host)
    }

    @Test
    fun `update a transaction`() = runBlocking {
        testObject.insertTransaction(transaction)

        val newHost = randomString()
        transaction.host = newHost
        testObject.updateTransaction(transaction)

        testObject.getTransaction(123456L).observeForever {
            assertThat(it?.id).isEqualTo(transaction.id)
            assertThat(it?.host).isEqualTo(newHost)
        }
    }
}
