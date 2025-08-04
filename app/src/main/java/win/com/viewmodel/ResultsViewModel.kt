package win.com.viewmodel

import android.app.Application
import androidx.lifecycle.*
import win.com.data.database.AppDatabase
import win.com.data.entity.EventEntity
import win.com.data.repository.EventRepository

class ResultsViewModel(application: Application) : AndroidViewModel(application) {

    val db = AppDatabase.getDatabase(application)
    val eventDao = db.eventDao()
    val participantDao = db.participantDao()
    val resultDao = db.liveResultDao()

    private val repository = EventRepository(eventDao, participantDao, resultDao)

    private val _filter = MutableLiveData<String>("")
    private val _allEvents = repository.getFinishedEvents() // LiveData<List<EventEntity>>

    val finishedEvents: LiveData<List<EventEntity>> = MediatorLiveData<List<EventEntity>>().apply {
        var currentEvents: List<EventEntity>? = null
        var currentFilter = ""

        fun update() {
            value = currentEvents?.filter {
                it.name.contains(currentFilter, ignoreCase = true)
                // Добавь по дате и другим фильтрам при необходимости
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

    fun setFilter(filter: String) {
        _filter.value = filter
    }
}
