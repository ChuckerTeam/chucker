package com.readystatesoftware.chuck.internal.data.repository

import android.arch.lifecycle.LiveData
import com.readystatesoftware.chuck.internal.data.entity.HttpTransaction
import com.readystatesoftware.chuck.internal.data.entity.HttpTransactionTuple
import com.readystatesoftware.chuck.internal.data.entity.RecordedThrowable
import com.readystatesoftware.chuck.internal.data.entity.RecordedThrowableTuple
import com.readystatesoftware.chuck.internal.data.room.ChuckerDatabase
import java.util.concurrent.Executor
import java.util.concurrent.Executors

internal class ChuckerDatabaseRepository(private val database: ChuckerDatabase) : ChuckerRepository {

    override fun getFilteredTransactionTuples(code: String, path: String): LiveData<List<HttpTransactionTuple>> {
        val pathQuery = if (path.isNotEmpty()) "%$path%" else "%"
        return database.transactionDao().getFilteredTransactionsTuples("$code%", pathQuery)
    }

    override fun getTransaction(transactionId: Long): LiveData<HttpTransaction> {
        return database.transactionDao().getRecordedTransaction(transactionId)
    }

    override fun getSortedTransactionTuples(): LiveData<List<HttpTransactionTuple>> {
        return database.transactionDao().getSortedTransactionsTuples()
    }

    override fun deleteAllTransactions() {
        executor.execute { database.transactionDao().deleteAllTransactions() }
    }

    override fun getRecordedThrowable(id: Long): LiveData<RecordedThrowable> {
        return database.throwableDao().getRecordedThrowable(id)
    }

    override fun deleteAllThrowables() {
        executor.execute { database.throwableDao().deleteAllThrowables() }
    }

    override fun getSortedThrowablesTuples(): LiveData<List<RecordedThrowableTuple>> {
        return database.throwableDao().getSortedThrowablesTuples()
    }

    private val executor: Executor = Executors.newSingleThreadExecutor()

    override fun insertTransaction(transaction: HttpTransaction) {
        executor.execute {
            val id = database.transactionDao().insertTransaction(transaction)
            transaction.id = id ?: 0
        }
    }

    override fun updateTransaction(transaction: HttpTransaction) {
        executor.execute { database.transactionDao().updateTransaction(transaction) }
    }

    override fun saveThrowable(throwable: RecordedThrowable) {
        executor.execute { database.throwableDao().insertRecordedThrowables(throwable) }
    }

    override fun deleteOldTransactions(threshold: Long) {
        executor.execute { database.transactionDao().deleteOldTransactions(threshold) }
    }

    override fun deleteOldThrowables(threshold: Long) {
        executor.execute { database.throwableDao().deleteOldThrowables(threshold) }
    }
}
