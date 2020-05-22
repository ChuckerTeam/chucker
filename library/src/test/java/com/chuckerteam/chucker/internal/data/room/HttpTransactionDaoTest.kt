package com.chuckerteam.chucker.internal.data.room

import android.content.Context
import android.database.Cursor
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.assertTransaction
import com.chuckerteam.chucker.internal.data.entity.assertTuples
import com.chuckerteam.chucker.internal.data.entity.createRequest
import com.chuckerteam.chucker.internal.data.entity.withResponseData
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HttpTransactionDaoTest {
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: ChuckerDatabase
    private lateinit var testObject: HttpTransactionDao

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, ChuckerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        testObject = db.transactionDao()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun insertedDataMakesItToTheDatabase() = runBlocking {
        val data = createRequest().withResponseData()
        val id = testObject.insert(data)

        with(db.query("select * from transactions", arrayOf())) {
            assertEquals(1, count)
            assertTrue(moveToFirst())

            assertEquals(id, longValue("id"))
            assertEquals(data.requestDate, longValue("requestDate"))
            assertEquals(data.responseDate, longValue("responseDate"))
            assertEquals(data.tookMs, longValue("tookMs"))
            assertEquals(data.protocol, stringValue("protocol"))
            assertEquals(data.requestHeaders, stringValue("requestHeaders"))
            assertEquals(data.responseHeaders, stringValue("responseHeaders"))
            assertEquals(data.method, stringValue("method"))
            assertEquals(data.url, stringValue("url"))
            assertEquals(data.host, stringValue("host"))
            assertEquals(data.path, stringValue("path"))
            assertEquals(data.scheme, stringValue("scheme"))
            assertEquals(data.responseTlsVersion, stringValue("responseTlsVersion"))
            assertEquals(data.responseCipherSuite, stringValue("responseCipherSuite"))
            assertEquals(data.requestContentLength, longValue("requestContentLength"))
            assertEquals(data.requestContentType, stringValue("requestContentType"))
            assertEquals(data.responseMessage, stringValue("responseMessage"))
            assertEquals(data.responseBody, stringValue("responseBody"))
            assertEquals(data.error, stringValue("error"))
        }
    }

    @Test
    fun loadSpecificTransactionById() = runBlocking {
        val disregardedTransaction = createRequest()
        insertTransaction(disregardedTransaction)
        val transaction = createRequest().withResponseData()
        insertTransaction(transaction)

        testObject.getById(transaction.id).observeForever {
            assertTransaction(transaction.id, transaction, it)
        }
    }

    @Test
    fun loadAllTransactions() = runBlocking {
        val older = createRequest().apply { requestDate = 100L }
        val newer = createRequest().apply { requestDate = 200L }
        insertTransaction(older)
        insertTransaction(newer)

        val all = testObject.getAll()
        assertEquals(2, all.size)

        val firstAssertion = all.firstOrNull { it.id == older.id }
        assertNotNull(firstAssertion)
        assertTransaction(older.id, older, firstAssertion)

        val secondAssertion = all.firstOrNull { it.id == newer.id }
        assertNotNull(secondAssertion)
        assertTransaction(newer.id, newer, secondAssertion)
    }

    @Test
    fun updateTransaction() = runBlocking {
        val older = createRequest().apply { requestDate = 100L }
        val newer = createRequest().apply { requestDate = 200L }
        insertTransaction(older)
        newer.id = older.id

        val rowsUpdated = testObject.update(newer)
        assertEquals(1, rowsUpdated)

        testObject.getById(older.id).observeForever {
            assertTransaction(older.id, newer, it)
        }
    }

    @Test
    fun deleteAllTheData() = runBlocking {
        testObject.insert(createRequest())
        assertRowCount(1)

        testObject.deleteAll()
        assertRowCount(0)
    }

    @Test
    fun deleteDataBefore() = runBlocking {
        val older = createRequest().apply { requestDate = 100L }
        val newer = createRequest().apply { requestDate = 200L }
        insertTransaction(older)
        insertTransaction(newer)
        assertRowCount(2)

        testObject.deleteBefore(150L)

        assertRowCount(1)
        assertRowCountMatchingId(older.id, 0)
        assertRowCountMatchingId(newer.id, 1)
    }

    @Test
    fun deleteDataBefore_timestampExactlyMatchesThreshold() = runBlocking {
        val older = createRequest().apply { requestDate = 100L }
        val newer = createRequest().apply { requestDate = 200L }
        insertTransaction(older)
        insertTransaction(newer)
        assertRowCount(2)

        testObject.deleteBefore(100L)

        assertRowCount(1)
        assertRowCountMatchingId(older.id, 0)
        assertRowCountMatchingId(newer.id, 1)
    }

    @Test
    fun loadSortedTuples() = runBlocking {
        val older = createRequest().withResponseData().apply { requestDate = 100L }
        val newer = createRequest().withResponseData().apply { requestDate = 200L }
        insertTransaction(older)
        insertTransaction(newer)

        testObject.getSortedTuples().observeForever { result ->
            assertTuples(listOf(newer, older), result)
        }
    }

    @Test
    fun filterTuplesByPath() = runBlocking {
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

        insertTransaction(transactionOne)
        insertTransaction(transactionTwo)
        insertTransaction(transactionThree)

        testObject.getFilteredTuples("418", "%abc%").observeForever { result ->
            assertTuples(listOf(transactionOne, transactionTwo), result)
        }
    }

    @Test
    fun filterTuplesByCode() = runBlocking {
        val transactionOne =
            createRequest("abc").withResponseData().apply {
                requestDate = 200L
                responseCode = 400
            }
        val transactionTwo =
            createRequest("abcdef").withResponseData().apply {
                requestDate = 100L
                responseCode = 200
            }
        val transactionThree =
            createRequest("def").withResponseData().apply {
                requestDate = 300L
                responseCode = 418 // I am still a teapot
            }

        insertTransaction(transactionOne)
        insertTransaction(transactionTwo)
        insertTransaction(transactionThree)

        testObject.getFilteredTuples("4%", "%").observeForever { result ->
            assertTuples(listOf(transactionThree, transactionOne), result)
        }
    }

    private suspend fun insertTransaction(transaction: HttpTransaction) {
        transaction.id = testObject.insert(transaction)!!
    }

    private fun assertRowCountMatchingId(id: Long, expectedCount: Long) {
        with(db.query("select count(*) from transactions where id=$id", arrayOf())) {
            moveToFirst()
            assertEquals(expectedCount, getLong(0))
            close()
        }
    }

    private fun assertRowCount(rowCount: Long) {
        with(db.query("select count(*) from transactions", arrayOf())) {
            moveToFirst()
            assertEquals(rowCount, getLong(0))
            close()
        }
    }

    private fun Cursor.stringValue(fieldName: String) =
        getString(getColumnIndex(fieldName))

    private fun Cursor.longValue(fieldName: String) =
        getLong(getColumnIndex(fieldName))
}
