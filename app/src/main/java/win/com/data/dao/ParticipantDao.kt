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

    @Query("SELECT * FROM participants WHERE eventId = :eventId")
    fun getParticipantsForEvent(eventId: Int): Flow<List<ParticipantEntity>>

    @Query("DELETE FROM participants WHERE eventId = :eventId")
    suspend fun deleteByEventId(eventId: Int)

    @Query("SELECT eventId, COUNT(*) as count FROM participants GROUP BY eventId")
    fun getParticipantCountsByEvent(): Flow<List<EventParticipantCount>>

    @Query("DELETE FROM participants")
    suspend fun clearAllParticipants()

    @Query("UPDATE participants SET lapTime = :lapTime WHERE eventId = :eventId AND nickname = :name")
    suspend fun updateLapTime(eventId: Int, name: String, lapTime: String)

    @Query("UPDATE participants SET pos = :pos WHERE eventId = :eventId AND nickname = :name")
    suspend fun updatePosition(eventId: Int, name: String, pos: String)
}

data class EventParticipantCount(
    val eventId: Int,
    val count: Int
)