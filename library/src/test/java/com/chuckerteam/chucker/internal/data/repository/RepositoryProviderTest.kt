package com.chuckerteam.chucker.internal.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.chuckerteam.chucker.internal.data.room.ChuckerDatabase
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RepositoryProviderTest {
    private lateinit var db: ChuckerDatabase
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, ChuckerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
        RepositoryProvider.close()
    }

    @Test(expected = IllegalStateException::class)
    fun uninitialzedTransactionRepo() {
        RepositoryProvider.transaction()
    }

    @Test(expected = IllegalStateException::class)
    fun uninitialzedErrorsRepo() {
        RepositoryProvider.throwable()
    }

    @Test
    fun transactionRepoAvailableAfterInitialize() {
        RepositoryProvider.initialize(context)
        assertNotNull(RepositoryProvider.transaction())
    }

    @Test
    fun errorRepoAvailableAfterInitialize() {
        RepositoryProvider.initialize(context)
        assertNotNull(RepositoryProvider.throwable())
    }

    @Test
    fun providerCachesInstancesOfTransactionRepo() {
        RepositoryProvider.initialize(context)
        val one = RepositoryProvider.transaction()
        val two = RepositoryProvider.transaction()
        assertSame(one, two)
    }

    @Test
    fun providerCachesInstancesOfErrorRepo() {
        RepositoryProvider.initialize(context)
        val one = RepositoryProvider.throwable()
        val two = RepositoryProvider.throwable()
        assertSame(one, two)
    }
}
