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
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class HttpTransactionDaoTest {
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
    fun `insert a transaction`() = runBlocking {
        val data = createRequest().withResponseData()
        val id = testObject.insert(data)

        with(db.query("select * from transactions", arrayOf())) {
            assertThat(count).isEqualTo(1)
            assertThat(moveToFirst()).isTrue()

            assertThat(longValue("id")).isEqualTo(id)
            assertThat(longValue("requestDate")).isEqualTo(data.requestDate)
            assertThat(longValue("responseDate")).isEqualTo(data.responseDate)
            assertThat(longValue("tookMs")).isEqualTo(data.tookMs)
            assertThat(stringValue("protocol")).isEqualTo(data.protocol)
            assertThat(stringValue("requestHeaders")).isEqualTo(data.requestHeaders)
            assertThat(stringValue("responseHeaders")).isEqualTo(data.responseHeaders)
            assertThat(stringValue("method")).isEqualTo(data.method)
            assertThat(stringValue("url")).isEqualTo(data.url)
            assertThat(stringValue("host")).isEqualTo(data.host)
            assertThat(stringValue("path")).isEqualTo(data.path)
            assertThat(stringValue("scheme")).isEqualTo(data.scheme)
            assertThat(stringValue("responseTlsVersion")).isEqualTo(data.responseTlsVersion)
            assertThat(stringValue("responseCipherSuite")).isEqualTo(data.responseCipherSuite)
            assertThat(longValue("requestPayloadSize")).isEqualTo(data.requestPayloadSize)
            assertThat(stringValue("requestContentType")).isEqualTo(data.requestContentType)
            assertThat(stringValue("responseMessage")).isEqualTo(data.responseMessage)
            assertThat(stringValue("responseBody")).isEqualTo(data.responseBody)
            assertThat(stringValue("error")).isEqualTo(data.error)
        }
    }

    @Test
    fun `get a transaction by ID`() = runBlocking {
        val disregardedTransaction = createRequest()
        insertTransaction(disregardedTransaction)
        val transaction = createRequest().withResponseData()
        insertTransaction(transaction)

        testObject.getById(transaction.id).observeForever {
            assertTransaction(transaction.id, transaction, it)
        }
    }

    @Test
    fun `get all transactions`() = runBlocking {
        val older = createRequest().apply { requestDate = 100L }
        val newer = createRequest().apply { requestDate = 200L }
        insertTransaction(older)
        insertTransaction(newer)

        val all = testObject.getAll()
        assertThat(all.size).isEqualTo(2)

        val firstAssertion = all.firstOrNull { it.id == older.id }
        assertThat(firstAssertion).isNotNull()
        assertTransaction(older.id, older, firstAssertion)

        val secondAssertion = all.firstOrNull { it.id == newer.id }
        assertThat(secondAssertion).isNotNull()
        assertTransaction(newer.id, newer, secondAssertion)
    }

    @Test
    fun `update a transaction`() = runBlocking {
        val older = createRequest().apply { requestDate = 100L }
        val newer = createRequest().apply { requestDate = 200L }
        insertTransaction(older)
        newer.id = older.id

        val rowsUpdated = testObject.update(newer)
        assertThat(rowsUpdated).isEqualTo(1)

        testObject.getById(older.id).observeForever {
            assertTransaction(older.id, newer, it)
        }
    }

    @Test
    fun `delete all transactions`() = runBlocking {
        testObject.insert(createRequest())
        assertRowCount(1)

        testObject.deleteAll()
        assertRowCount(0)
    }

    @Test
    fun `delete old transactions`() = runBlocking {
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
    fun `old data threshold is inclusive`() = runBlocking {
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
    fun `get sorted transaction tuples`() = runBlocking {
        val older = createRequest().withResponseData().apply { requestDate = 100L }
        val newer = createRequest().withResponseData().apply { requestDate = 200L }
        insertTransaction(older)
        insertTransaction(newer)

        testObject.getSortedTuples().observeForever { result ->
            assertTuples(listOf(newer, older), result)
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

        insertTransaction(transactionOne)
        insertTransaction(transactionTwo)
        insertTransaction(transactionThree)

        testObject.getFilteredTuples(codeQuery = "418", pathQuery = "%abc%").observeForever { result ->
            assertTuples(listOf(transactionOne, transactionTwo), result)
        }
    }

    @Test
    fun `transaction tuples are filtered by code`() = runBlocking {
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

        testObject.getFilteredTuples(codeQuery = "4%", pathQuery = "%").observeForever { result ->
            assertTuples(listOf(transactionThree, transactionOne), result)
        }
    }

    private suspend fun insertTransaction(transaction: HttpTransaction) {
        transaction.id = testObject.insert(transaction)!!
    }

    private fun assertRowCountMatchingId(id: Long, expectedCount: Long) {
        with(db.query("select count(*) from transactions where id=$id", arrayOf())) {
            moveToFirst()
            assertThat(getLong(0)).isEqualTo(expectedCount)
            close()
        }
    }

    private fun assertRowCount(rowCount: Long) {
        with(db.query("select count(*) from transactions", arrayOf())) {
            moveToFirst()
            assertThat(getLong(0)).isEqualTo(rowCount)
            close()
        }
    }

    private fun Cursor.stringValue(fieldName: String) =
        getString(getColumnIndex(fieldName))

    private fun Cursor.longValue(fieldName: String) =
        getLong(getColumnIndex(fieldName))
}
