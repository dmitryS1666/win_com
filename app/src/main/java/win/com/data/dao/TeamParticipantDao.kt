package win.com.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import win.com.data.entity.TeamParticipantEntity

@Dao
interface TeamParticipantDao {
    @Query("SELECT * FROM team_participants WHERE teamId = :teamId")
    fun getParticipantsForTeam(teamId: Int): Flow<List<TeamParticipantEntity>>

    @Delete
    suspend fun delete(participant: TeamParticipantEntity)

    @Query("DELETE FROM team_participants WHERE teamId = :teamId")
    suspend fun deleteAllForTeam(teamId: Long)

    @Query("DELETE FROM team_participants WHERE teamId = :teamId")
    suspend fun deleteParticipantsByTeamId(teamId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipant(participant: TeamParticipantEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(participants: List<TeamParticipantEntity>)
}
