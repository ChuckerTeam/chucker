package com.readystatesoftware.chuck.internal.data.room

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.readystatesoftware.chuck.internal.data.entity.HttpTransaction
import com.readystatesoftware.chuck.internal.data.entity.RecordedThrowable

@Database(entities = [RecordedThrowable::class, HttpTransaction::class], version = 1, exportSchema = false)
internal abstract class ChuckerDatabase : RoomDatabase() {

    abstract fun throwableDao(): RecordedThrowableDao
    abstract fun transactionDao(): HttpTransactionDao

    companion object {
        private val DB_NAME = "chucker.db"

        fun create(context: Context): ChuckerDatabase {
            return Room.databaseBuilder(context, ChuckerDatabase::class.java, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build()
        }
    }

}
