package com.readystatesoftware.chuck.internal.data.repository

import android.arch.lifecycle.LiveData
import com.readystatesoftware.chuck.internal.data.entity.HttpTransaction
import com.readystatesoftware.chuck.internal.data.entity.HttpTransactionTuple
import com.readystatesoftware.chuck.internal.data.entity.RecordedThrowable
import com.readystatesoftware.chuck.internal.data.entity.RecordedThrowableTuple
import com.readystatesoftware.chuck.internal.data.room.ChuckerDatabase
import java.util.concurrent.Executor
import java.util.concurrent.Executors

internal class HttpTransactionDatabaseRepository(private val database: ChuckerDatabase) : HttpTransactionRepository {

    private val executor: Executor = Executors.newSingleThreadExecutor()

    override fun getFilteredTransactionTuples(code: String, path: String): LiveData<List<HttpTransactionTuple>> {
        val pathQuery = if (path.isNotEmpty()) "%$path%" else "%"
        return database.transactionDao().getFilteredTuples("$code%", pathQuery)
    }

    override fun getTransaction(transactionId: Long): LiveData<HttpTransaction> {
        return database.transactionDao().getById(transactionId)
    }

    override fun getSortedTransactionTuples(): LiveData<List<HttpTransactionTuple>> {
        return database.transactionDao().getSortedTuples()
    }

    override fun deleteAllTransactions() {
        executor.execute { database.transactionDao().deleteAll() }
    }

    override fun insertTransaction(transaction: HttpTransaction) {
        executor.execute {
            val id = database.transactionDao().insert(transaction)
            transaction.id = id ?: 0
        }
    }

    override fun updateTransaction(transaction: HttpTransaction) : Int {
        return database.transactionDao().update(transaction)
    }

    override fun deleteOldTransactions(threshold: Long) {
        executor.execute { database.transactionDao().deleteBefore(threshold) }
    }
}
