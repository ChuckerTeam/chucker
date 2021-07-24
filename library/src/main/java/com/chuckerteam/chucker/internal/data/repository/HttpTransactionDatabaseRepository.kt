package com.chuckerteam.chucker.internal.data.repository

import androidx.lifecycle.LiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import com.chuckerteam.chucker.internal.data.room.ChuckerDatabase
import com.chuckerteam.chucker.internal.support.distinctUntilChanged

internal class HttpTransactionDatabaseRepository(private val database: ChuckerDatabase) : HttpTransactionRepository {

    private val transactionDao get() = database.transactionDao()

    override fun getFilteredTransactionTuples(
        path: String,
        code: String,
        urls: List<String>
    ): LiveData<List<HttpTransactionTuple>> {
        val query = getFilteredTransactionQuery(path, code, urls)
        return transactionDao.getFilteredTuples(query)
    }

    private fun getFilteredTransactionQuery(
        path: String,
        code: String,
        urls: List<String>
    ): SimpleSQLiteQuery {
        val codeQuery = "$code%"
        val pathQuery = if (path.isNotEmpty()) "%$path%" else "%"
        val urlsQuery = if (urls.isEmpty()) listOf("%") else urls

        val builder = StringBuilder(
            "SELECT id, requestDate, tookMs, protocol, method, host, " +
                "path, scheme, responseCode, requestPayloadSize, responsePayloadSize, error FROM " +
                "transactions WHERE responseCode LIKE (?) AND path LIKE (?) AND ("
        )
        urlsQuery.forEachIndexed { index, _ ->
            builder.append(" url LIKE (?) ")
            if (index != urlsQuery.count() - 1) {
                builder.append("OR")
            }
        }
        builder.append(") ORDER BY requestDate DESC")
        return SimpleSQLiteQuery(
            builder.toString(),
            arrayOf(codeQuery, pathQuery, *urlsQuery.toTypedArray())
        )
    }

    override fun getTransaction(transactionId: Long): LiveData<HttpTransaction?> {
        return transactionDao.getById(transactionId)
            .distinctUntilChanged { old, new -> old?.hasTheSameContent(new) != false }
    }

    override fun getSortedTransactionTuples(): LiveData<List<HttpTransactionTuple>> {
        return transactionDao.getSortedTuples()
    }

    override suspend fun deleteAllTransactions() {
        transactionDao.deleteAll()
    }

    override suspend fun insertTransaction(transaction: HttpTransaction) {
        val id = transactionDao.insert(transaction)
        transaction.id = id ?: 0
    }

    override suspend fun updateTransaction(transaction: HttpTransaction): Int {
        return transactionDao.update(transaction)
    }

    override suspend fun deleteOldTransactions(threshold: Long) {
        transactionDao.deleteBefore(threshold)
    }

    override suspend fun getAllTransactions(): List<HttpTransaction> = transactionDao.getAll()
}
