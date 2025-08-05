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

    private val _filterType = MutableLiveData<String>("")
    private val _filterQuery = MutableLiveData<String>("")

    val finishedEvents: LiveData<List<EventEntity>> = MediatorLiveData<List<EventEntity>>().apply {
        var events: List<EventEntity>? = null
        var type = ""
        var query = ""

        fun update() {
            value = events?.filter { event ->
                when (type) {
                    "name" -> event.name.contains(query, true)
                    "player" -> participants.value?.any {
                        it.eventId == event.id && it.nickname.contains(query, true)
                    } == true
                    "date" -> event.date == query
                    else -> true
                }
            }
        }

        addSource(_allEvents) {
            events = it
            update()
        }
        addSource(_filterType) {
            type = it ?: ""
            update()
        }
        addSource(_filterQuery) {
            query = it ?: ""
            update()
        }
        addSource(participants) {
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

    fun setFilterByName(name: String) {
        _filterType.value = "name"
        _filterQuery.value = name
    }

    fun setFilterByPlayer(player: String) {
        _filterType.value = "player"
        _filterQuery.value = player
    }

    fun setFilterByDate(date: String) {
        _filterType.value = "date"
        _filterQuery.value = date
    }
}
