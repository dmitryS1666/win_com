package win.com.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import win.com.data.entity.EventEntity
import win.com.data.entity.ParticipantEntity

@Dao
interface ParticipantDao {
    @Query("SELECT * FROM participants ORDER BY id DESC")
    fun getAllParticipants(): Flow<List<ParticipantEntity>>

    @Insert
    suspend fun insert(event: ParticipantEntity): Long

    @Update
    suspend fun update(event: ParticipantEntity)

    @Delete
    suspend fun delete(event: ParticipantEntity)
}