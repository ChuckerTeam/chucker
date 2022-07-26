package com.chuckerteam.chucker.internal.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.chuckerteam.chucker.internal.data.room.ChuckerDatabase
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class RepositoryProviderTest {
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

    @Test
    fun `fails with uninitialized transaction repository`() {
        assertThrows<IllegalStateException> {
            RepositoryProvider.transaction()
        }
    }

    @Test
    fun `transaction repository is available after initialization`() {
        RepositoryProvider.initialize(context)
        assertThat(RepositoryProvider.transaction()).isNotNull()
    }

    @Test
    fun `transaction repository is cached`() {
        RepositoryProvider.initialize(context)
        val one = RepositoryProvider.transaction()
        val two = RepositoryProvider.transaction()
        assertThat(one).isSameInstanceAs(two)
    }
}
