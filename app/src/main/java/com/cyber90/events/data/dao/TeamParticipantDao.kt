package com.cyber90.events.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.cyber90.events.data.entity.TeamParticipantEntity

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

    @Query("SELECT * FROM team_participants")
    fun getAllTeamParticipants(): Flow<List<TeamParticipantEntity>>

    @Query("SELECT teamId, COUNT(*) as count FROM team_participants GROUP BY teamId")
    fun getParticipantsCountByTeam(): Flow<List<TeamParticipantsCount>>

    @Query("DELETE FROM teams")
    suspend fun clearAllTeamParticipants()
}

data class TeamParticipantsCount(
    val teamId: Long,
    val count: Int
)
