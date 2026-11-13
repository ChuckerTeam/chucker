package com.chuckerteam.chucker.internal.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.chuckerteam.chucker.internal.data.entity.MapRemoteRule

/**
 * Kept apart from [ChuckerDatabase], which drops all of its tables on every schema change:
 * losing recorded traffic on upgrade is fine, losing rules set up by hand is not.
 */
@Database(entities = [MapRemoteRule::class], version = 1, exportSchema = false)
internal abstract class MapRemoteRuleDatabase : RoomDatabase() {
    abstract fun mapRemoteRuleDao(): MapRemoteRuleDao

    companion object {
        private const val DB_NAME = "chucker_map_remote.db"

        fun create(applicationContext: Context): MapRemoteRuleDatabase =
            Room
                .databaseBuilder(applicationContext, MapRemoteRuleDatabase::class.java, DB_NAME)
                // The rules are read from the interceptor, on whichever thread the app runs its calls.
                .allowMainThreadQueries()
                .build()
    }
}
