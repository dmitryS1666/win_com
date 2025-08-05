package win.com.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import win.com.data.database.AppDatabase
import win.com.data.entity.EventEntity
import win.com.data.entity.ParticipantEntity
import win.com.data.entity.TeamParticipantEntity
import win.com.data.repository.EventRepository

class ResultsViewModel(application: Application) : AndroidViewModel(application) {
    val db = AppDatabase.getDatabase(application)
    val eventDao = db.eventDao()
    val participantDao = db.participantDao()
    val teamParticipantDao = db.teamParticipantDao()
    val resultDao = db.liveResultDao()

    private val repository = EventRepository(eventDao, participantDao, teamParticipantDao, resultDao)

    private val _filter = MutableLiveData<String>("")
    private val _allEvents = repository.getFinishedEvents() // LiveData<List<EventEntity>>

    private val _participants = MutableLiveData<List<ParticipantEntity>>()
    val participants: LiveData<List<ParticipantEntity>> = _participants

    val finishedEvents: LiveData<List<EventEntity>> = MediatorLiveData<List<EventEntity>>().apply {
        var currentEvents: List<EventEntity>? = null
        var currentFilter = ""

        fun update() {
            value = currentEvents?.filter {
                it.name.contains(currentFilter, ignoreCase = true)
                // Можно фильтровать по дате и участникам, если нужно
            }
        }

        addSource(_allEvents) {
            currentEvents = it
            update()
        }

        addSource(_filter) {
            currentFilter = it ?: ""
            update()
        }
    }

    init {
        loadParticipants()
    }

    private fun loadParticipants() {
        viewModelScope.launch {
            val list = repository.getAllParticipants().first()
            _participants.postValue(list)
        }
    }

    fun setFilter(filter: String) {
        _filter.value = filter
    }
}
