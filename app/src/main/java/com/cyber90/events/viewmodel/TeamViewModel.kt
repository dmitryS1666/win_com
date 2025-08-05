package com.cyber90.events.viewmodel
import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import com.cyber90.events.data.dao.TeamParticipantsCount
import com.cyber90.events.data.database.AppDatabase
import com.cyber90.events.data.entity.TeamEntity
import com.cyber90.events.data.entity.TeamParticipantEntity
import com.cyber90.events.data.repository.TeamRepository

class TeamViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TeamRepository
    private val job = SupervisorJob()
    private val saveScope = CoroutineScope(Dispatchers.IO + job)

    val allTeams: LiveData<List<TeamEntity>>
    val allParticipants: LiveData<List<TeamParticipantEntity>>
    val participantsCountByTeam: LiveData<List<TeamParticipantsCount>>

    init {
        val db = AppDatabase.getDatabase(application)
        val teamDao = db.teamDao()
        val teamParticipantDao = db.teamParticipantDao()
        repository = TeamRepository(teamDao, teamParticipantDao, viewModelScope)

        allTeams = repository.getAllTeams()
            .map { list -> list.sortedByDescending { it.id } }
            .asLiveData()

        allParticipants = repository.getAllParticipants().asLiveData()

        participantsCountByTeam = repository.getParticipantsCountByTeam().asLiveData()
    }

    fun insertWithParticipants(team: TeamEntity, participants: List<TeamParticipantEntity>) {
        viewModelScope.launch {
            repository.insert(team, participants)
        }
    }

    fun deleteTeam(team: TeamEntity) {
        viewModelScope.launch {
            repository.delete(team)
        }
    }

    fun getParticipants(teamId: Int): LiveData<List<TeamParticipantEntity>> {
        return repository.getParticipantsForTeam(teamId).asLiveData()
    }

    fun getTeamById(id: Int): LiveData<TeamEntity> = repository.getById(id)

    fun saveTeamAndParticipants(team: TeamEntity, participants: List<TeamParticipantEntity>) {
        saveScope.launch {
            try {
                repository.update(team)
                repository.updateParticipantsForTeam(team.id.toLong(), participants)
            } catch (e: Exception) {
                Log.e("SAVE", "Exception in saveTeamAndParticipants: ${e.message}", e)
            }
        }
    }

    suspend fun getTeamByName(teamName: String): TeamEntity? {
        return repository.getTeamByName(teamName)
    }

    suspend fun insertTeamParticipant(teamParticipant: TeamParticipantEntity) {
        repository.insertTeamParticipant(teamParticipant)
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}
