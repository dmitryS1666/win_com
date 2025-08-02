package win.com.ui.teams

import android.app.Application
import androidx.annotation.OptIn
import androidx.lifecycle.*
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import win.com.data.database.AppDatabase
import win.com.data.entity.TeamEntity
import win.com.data.entity.TeamParticipantEntity
import win.com.data.repository.TeamParticipantRepository
import win.com.data.repository.TeamRepository

class TeamViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TeamRepository
    private val job = SupervisorJob()
    private val saveScope = CoroutineScope(Dispatchers.IO + job)

    val allTeams: LiveData<List<TeamEntity>>

    init {
        val db = AppDatabase.getDatabase(application)
        val teamDao = db.teamDao()
        val teamParticipantDao = db.teamParticipantDao()
        repository = TeamRepository(teamDao, teamParticipantDao, viewModelScope)

        allTeams = repository.getAllTeams()
            .map { it.sortedByDescending { team -> team.id } }
            .asLiveData()
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

    @OptIn(UnstableApi::class)
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

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}
