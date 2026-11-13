package com.chuckerteam.chucker.internal.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chuckerteam.chucker.internal.data.entity.MapRemoteRule
import kotlinx.coroutines.flow.Flow

@Dao
internal interface MapRemoteRuleDao {
    @Query("SELECT * FROM map_remote_rules ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<MapRemoteRule>>

    @Query("SELECT * FROM map_remote_rules")
    fun getAll(): List<MapRemoteRule>

    @Query("SELECT * FROM map_remote_rules WHERE requestUrl = :requestUrl AND requestMethod = :requestMethod")
    suspend fun get(
        requestUrl: String,
        requestMethod: String,
    ): MapRemoteRule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(rule: MapRemoteRule)

    @Query("DELETE FROM map_remote_rules WHERE requestUrl = :requestUrl AND requestMethod = :requestMethod")
    suspend fun delete(
        requestUrl: String,
        requestMethod: String,
    )
}
