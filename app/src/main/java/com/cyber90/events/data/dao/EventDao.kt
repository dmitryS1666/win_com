package com.cyber90.events.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.cyber90.events.data.entity.EventEntity

@Dao
interface EventDao {

    @Query("SELECT * FROM events ORDER BY date DESC, time DESC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Insert
    suspend fun insert(event: EventEntity): Long

    @Update
    suspend fun update(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun delete(eventId: Int)

    @Query("SELECT * FROM events WHERE id = :eventId LIMIT 1")
    fun getById(eventId: Int): LiveData<EventEntity>

    @Query("DELETE FROM events")
    suspend fun clearAllEvents()

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    suspend fun getEventByIdNow(id: Int): EventEntity?

    @Query("SELECT * FROM events WHERE status = 'FINISHED' ORDER BY date DESC, time DESC")
    fun getFinishedEvents(): LiveData<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    fun getFinishedEventById(id: Int): EventEntity?
}
