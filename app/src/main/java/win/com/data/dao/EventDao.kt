package win.com.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import win.com.data.entity.EventEntity

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY date DESC, time DESC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Insert
    suspend fun insert(event: EventEntity): Long

    @Update
    suspend fun update(event: EventEntity)

    @Delete
    suspend fun delete(event: EventEntity)
}
