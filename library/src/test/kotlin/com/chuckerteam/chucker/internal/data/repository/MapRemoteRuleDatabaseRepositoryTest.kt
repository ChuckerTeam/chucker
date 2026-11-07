package com.chuckerteam.chucker.internal.data.repository

import android.content.Context
import android.os.Looper
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.chuckerteam.chucker.internal.data.entity.toMapRemoteRuleUrl
import com.chuckerteam.chucker.internal.data.room.MapRemoteRuleDao
import com.chuckerteam.chucker.internal.data.room.MapRemoteRuleDatabase
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
internal class MapRemoteRuleDatabaseRepositoryTest {
    private lateinit var db: MapRemoteRuleDatabase
    private lateinit var testObject: MapRemoteRuleDatabaseRepository

    private val requestUrl = "https://example.com/users".toHttpUrl()

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db =
            Room
                .inMemoryDatabaseBuilder(context, MapRemoteRuleDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        testObject = MapRemoteRuleDatabaseRepository(db)
    }

    @After
    fun tearDown() = db.close()

    private fun save(
        target: String,
        method: String = "GET",
    ) = runBlocking { testObject.saveRule(requestUrl.toMapRemoteRuleUrl(), method, target) }

    private fun targetOf(
        url: String,
        method: String = "GET",
    ) = testObject.findTarget(url.toHttpUrl(), method)?.toString()

    @Test
    fun `rule saved before the first lookup is found`() {
        save("https://mock.example.com/users")

        // A new instance has nothing in memory yet, like the first call after the app starts.
        val coldRepository = MapRemoteRuleDatabaseRepository(db)

        assertThat(coldRepository.findTarget(requestUrl, "GET").toString())
            .isEqualTo("https://mock.example.com/users")
    }

    @Test
    fun `failing to open the database does not break the call`() {
        val brokenDao =
            mockk<MapRemoteRuleDao> {
                every { getAll() } throws IllegalStateException("A migration was required")
            }
        val brokenDb = mockk<MapRemoteRuleDatabase> { every { mapRemoteRuleDao() } returns brokenDao }

        assertThat(MapRemoteRuleDatabaseRepository(brokenDb).findTarget(requestUrl, "GET")).isNull()
    }

    @Test
    fun `failing to observe the rules shows no rules`() {
        val brokenDao =
            mockk<MapRemoteRuleDao> {
                every { observeAll() } returns flow { throw IllegalStateException("A migration was required") }
            }
        val brokenDb = mockk<MapRemoteRuleDatabase> { every { mapRemoteRuleDao() } returns brokenDao }
        val rules = MapRemoteRuleDatabaseRepository(brokenDb).getRules()

        rules.observeForever { }
        shadowOf(Looper.getMainLooper()).idle()

        assertThat(rules.value).isEmpty()
    }

    @Test
    fun `no target without a matching rule`() {
        save("https://mock.example.com/users")

        assertThat(targetOf("https://example.com/other")).isNull()
        assertThat(targetOf("https://other.example.com/users")).isNull()
    }

    @Test
    fun `rules are specific to a method`() {
        save("https://mock.example.com/get", method = "GET")
        save("https://mock.example.com/post", method = "POST")

        assertThat(targetOf(requestUrl.toString(), "GET")).isEqualTo("https://mock.example.com/get")
        assertThat(targetOf(requestUrl.toString(), "POST")).isEqualTo("https://mock.example.com/post")
        assertThat(targetOf(requestUrl.toString(), "DELETE")).isNull()
    }

    @Test
    fun `method is matched ignoring case`() {
        save("https://mock.example.com/users", method = "get")

        assertThat(targetOf(requestUrl.toString(), "GET")).isEqualTo("https://mock.example.com/users")
    }

    @Test
    fun `query parameters of the call are forwarded to a target without any`() {
        save("https://mock.example.com/users")

        assertThat(targetOf("https://example.com/users?page=2&q=a%20b"))
            .isEqualTo("https://mock.example.com/users?page=2&q=a%20b")
    }

    @Test
    fun `query parameters of the target take precedence`() {
        save("https://mock.example.com/users?fixture=empty")

        assertThat(targetOf("https://example.com/users?page=2"))
            .isEqualTo("https://mock.example.com/users?fixture=empty")
    }

    @Test
    fun `saving again replaces the target for future lookups`() {
        save("https://mock.example.com/first")
        assertThat(targetOf(requestUrl.toString())).isEqualTo("https://mock.example.com/first")

        save("https://mock.example.com/second")

        assertThat(targetOf(requestUrl.toString())).isEqualTo("https://mock.example.com/second")
    }

    @Test
    fun `deleting a rule stops the mapping and keeps other methods`() {
        save("https://mock.example.com/get", method = "GET")
        save("https://mock.example.com/post", method = "POST")
        assertThat(targetOf(requestUrl.toString(), "GET")).isNotNull()

        runBlocking { testObject.deleteRule(requestUrl.toMapRemoteRuleUrl(), "GET") }

        assertThat(targetOf(requestUrl.toString(), "GET")).isNull()
        assertThat(targetOf(requestUrl.toString(), "POST")).isNotNull()
    }
}
