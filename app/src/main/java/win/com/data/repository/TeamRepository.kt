package win.com.data.repository

import androidx.annotation.OptIn
import androidx.lifecycle.LiveData
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import win.com.data.dao.TeamDao
import win.com.data.dao.TeamParticipantDao
import win.com.data.entity.TeamEntity
import win.com.data.entity.TeamParticipantEntity

class TeamRepository(
    private val teamDao: TeamDao,
    private val teamParticipantDao: TeamParticipantDao,
    private val externalScope: CoroutineScope
) {
    fun saveTeamAndParticipants(team: TeamEntity, participants: List<TeamParticipantEntity>) {
        externalScope.launch {
            update(team)
            updateParticipantsForTeam(team.id.toLong(), participants)
        }
    }

    fun getAllTeams(): Flow<List<TeamEntity>> = teamDao.getAllTeams()

    suspend fun insert(team: TeamEntity, participants: List<TeamParticipantEntity>) {
        val teamId = teamDao.insert(team)
        val withTeamId = participants.map { it.copy(teamId = teamId) }
        teamParticipantDao.insertAll(withTeamId)
    }

    @OptIn(UnstableApi::class)
    suspend fun update(team: TeamEntity) {
        try {
            teamDao.update(team)
        } catch (e: Exception) {
            Log.e("SAVE", "Exception in update: ${e.message}", e)
            throw e
        }
    }

    suspend fun delete(team: TeamEntity) {
        teamParticipantDao.deleteAllForTeam(team.id.toLong())
        teamDao.delete(team)
    }

    fun getById(id: Int): LiveData<TeamEntity> = teamDao.getById(id)

    fun getParticipantsForTeam(teamId: Int): Flow<List<TeamParticipantEntity>> =
        teamParticipantDao.getParticipantsForTeam(teamId)

    @OptIn(UnstableApi::class)
    suspend fun updateParticipantsForTeam(teamId: Long, participants: List<TeamParticipantEntity>) {
        try {
            Log.d("SAVE", "teamId: $teamId")

            teamParticipantDao.deleteParticipantsByTeamId(teamId)
            Log.d("SAVE", "deleteParticipantsByTeamId")

            teamParticipantDao.insertAll(participants)
            Log.d("SAVE", "Inserted participants: $participants")
        } catch (e: Exception) {
            Log.e("SAVE", "Error updating participants: ${e.message}", e)
        }
    }
}
