package com.chuckerteam.chucker.internal.data.repository

import androidx.lifecycle.LiveData
import com.chuckerteam.chucker.internal.data.entity.WebsocketTraffic
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import com.chuckerteam.chucker.internal.data.room.ChuckerDatabase
import java.util.concurrent.Executor
import java.util.concurrent.Executors

internal class WebsocketDatabaseRepository(private val database: ChuckerDatabase) : WebsocketRepository {

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

    override fun deleteOldTransactions(threshold: Long) {
        executor.execute { database.transactionDao().deleteBefore(threshold) }
    }

/*
    override fun insertNegotiation(transaction: HttpTransaction) {
        executor.execute {
            val id = database.transactionDao().insert(transaction)
            transaction.id = id ?: 0
        }
    }

    override fun updateNegotiation(transaction: HttpTransaction) : Int {
        return database.transactionDao().update(transaction)
    }
*/

    override fun insertTraffic(traffic: WebsocketTraffic) {

    }
}
