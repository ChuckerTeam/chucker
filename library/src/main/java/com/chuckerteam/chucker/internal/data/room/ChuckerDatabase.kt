package com.chuckerteam.chucker.internal.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.RecordedThrowable

@Database(entities = [RecordedThrowable::class, HttpTransaction::class], version = 2, exportSchema = false)
internal abstract class ChuckerDatabase : RoomDatabase() {

    abstract fun throwableDao(): RecordedThrowableDao
    abstract fun transactionDao(): HttpTransactionDao

    companion object {
        private const val OLD_DB_NAME = "chuck.db"
        private const val DB_NAME = "chucker.db"

        fun create(context: Context): ChuckerDatabase {
            // We eventually delete the old DB if a previous version of Chuck/Chucker was used.
            context.getDatabasePath(OLD_DB_NAME).delete()

            return Room.databaseBuilder(context, ChuckerDatabase::class.java, DB_NAME)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
