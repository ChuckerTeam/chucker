package com.readystatesoftware.chuck.internal.data.repository

import android.content.Context
import com.readystatesoftware.chuck.internal.data.room.ChuckerDatabase

/**
 * A singleton to hold the [ChuckerRepository] instance. Make sure you call [initialize] before
 * accessing the stored instance.
 */
internal object ChuckerRepositoryProvider {

    private var instance: ChuckerRepository? = null

    @JvmStatic fun it(): ChuckerRepository {
        return checkNotNull(instance) {
            "You can't access the repository if you don't initialize it!"
        }
    }

    @JvmStatic fun initialize(context: Context): ChuckerRepository {
        ChuckerDatabaseRepository(ChuckerDatabase.create(context)).let {
            instance = it
            return it
        }
    }
}