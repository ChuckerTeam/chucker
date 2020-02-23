package com.chuckerteam.chucker.internal.data.repository

import androidx.lifecycle.LiveData
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import com.chuckerteam.chucker.internal.data.room.ChuckerDatabase
import com.chuckerteam.chucker.internal.support.distinctUntilChanged
import java.util.concurrent.Executor
import java.util.concurrent.Executors

internal class HttpTransactionDatabaseRepository(private val database: ChuckerDatabase) : HttpTransactionRepository {

    private val executor: Executor = Executors.newSingleThreadExecutor()

    private val transcationDao get() = database.transactionDao()

    override fun getFilteredTransactionTuples(code: String, path: String): LiveData<List<HttpTransactionTuple>> {
        val pathQuery = if (path.isNotEmpty()) "%$path%" else "%"
        return transcationDao.getFilteredTuples("$code%", pathQuery)
    }

    override fun getTransaction(transactionId: Long): LiveData<HttpTransaction> {
        return transcationDao.getById(transactionId).distinctUntilChanged { old, new -> old.hasTheSameContent(new) }
    }

    override fun getSortedTransactionTuples(): LiveData<List<HttpTransactionTuple>> {
        return transcationDao.getSortedTuples()
    }

    override fun deleteAllTransactions() {
        executor.execute { transcationDao.deleteAll() }
    }

    override fun insertTransaction(transaction: HttpTransaction) {
        executor.execute {
            val id = transcationDao.insert(transaction)
            transaction.id = id ?: 0
        }
    }

    override fun updateTransaction(transaction: HttpTransaction): Int {
        return transcationDao.update(transaction)
    }

    override fun deleteOldTransactions(threshold: Long) {
        executor.execute { transcationDao.deleteBefore(threshold) }
    }
}
