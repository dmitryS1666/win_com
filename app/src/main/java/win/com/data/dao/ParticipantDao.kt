package win.com.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import win.com.data.entity.ParticipantEntity

@Dao
interface ParticipantDao {
    @Query("SELECT * FROM participants ORDER BY id DESC")
    fun getAllParticipants(): Flow<List<ParticipantEntity>>

    @Insert
    suspend fun insert(participant: ParticipantEntity): Long

    @Update
    suspend fun update(participant: ParticipantEntity)

    @Query("DELETE FROM events WHERE id = :participantId")
    suspend fun delete(participantId: Int)
}