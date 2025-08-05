package com.cyber90.events.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.cyber90.events.data.entity.LiveResultEntity
import com.cyber90.events.data.entity.ResultEntity

@Dao
interface LiveResultDao {

    @Query("SELECT * FROM live_results WHERE eventId = :eventId")
    fun getResultsForEvent(eventId: Int): Flow<List<LiveResultEntity>>

    @Query("SELECT * FROM live_results WHERE participantId = :participantId AND eventId = :eventId LIMIT 1")
    suspend fun getResultForParticipant(eventId: Int, participantId: Int): LiveResultEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: LiveResultEntity)

    @Update
    suspend fun update(result: LiveResultEntity)

    @Query("DELETE FROM live_results WHERE eventId = :eventId")
    suspend fun deleteResultsByEvent(eventId: Int)

    @Query("SELECT * FROM results WHERE eventId = :eventId ORDER BY position ASC")
    fun getResultsByEventId(eventId: Int): LiveData<List<ResultEntity>>

    // если нужен "синхронный" запрос для suspend функции
    @Query("SELECT * FROM results WHERE eventId = :eventId ORDER BY position ASC")
    suspend fun getResultsByEventIdNow(eventId: Int): List<ResultEntity>
}
