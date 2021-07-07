package com.chuckerteam.chucker.internal.data.repository

import androidx.lifecycle.LiveData
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import com.chuckerteam.chucker.internal.data.room.ChuckerDatabase
import com.chuckerteam.chucker.internal.support.distinctUntilChanged

internal class HttpTransactionDatabaseRepository(private val database: ChuckerDatabase) : HttpTransactionRepository {

    private val transactionDao get() = database.transactionDao()

    override fun getFilteredTransactionTuples(
        code: String,
        path: String,
        requestTags: List<String?>
    ): LiveData<List<HttpTransactionTuple>> {
        val pathQuery = if (path.isNotEmpty()) "%$path%" else "%"
        return if (requestTags.isEmpty()) {
            transactionDao.getFilteredTuples("$code%", pathQuery)
        } else {
            if (requestTags.any { it == null }) {
                transactionDao.getFilteredTuplesWithNoRequestTags("$code%", pathQuery, requestTags)
            } else {
                transactionDao.getFilteredTuples("$code%", pathQuery, requestTags)
            }
        }
    }

    override fun getTransaction(transactionId: Long): LiveData<HttpTransaction?> {
        return transactionDao.getById(transactionId)
            .distinctUntilChanged { old, new -> old?.hasTheSameContent(new) != false }
    }

    override fun getSortedTransactionTuples(requestTags: List<String?>): LiveData<List<HttpTransactionTuple>> {
        return if (requestTags.isEmpty()) {
            transactionDao.getSortedTuples()
        } else {
            if (requestTags.any { it == null }) {
                transactionDao.getSortedTuplesWithNoRequestTags(requestTags)
            } else {
                transactionDao.getSortedTuples(requestTags)
            }
        }
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

    override suspend fun getByUrl(url: String): HttpTransaction? = transactionDao.getByUrl(url)

    override suspend fun getAllRequestTags(): List<String> = transactionDao.getAllRequestTags()
}
