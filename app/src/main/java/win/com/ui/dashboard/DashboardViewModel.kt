package win.com.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import win.com.data.database.AppDatabase
import win.com.data.entity.EventEntity
import win.com.data.repository.EventRepository

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: EventRepository

    val lastEvent: LiveData<EventEntity?>

    val allEvents: LiveData<List<EventEntity>>
        get() = this.repository.getAllEvents().asLiveData()

    init {
        val db = AppDatabase.getDatabase(application)

        val eventDao = db.eventDao()

        repository = EventRepository(eventDao)

        lastEvent = repository.getAllEvents().map { it.firstOrNull() }.asLiveData()
    }

    fun deleteEvent(id: Int) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }

    fun getEventById(id: Int): LiveData<EventEntity> {
        return repository.getById(id)
    }

    fun updateEvent(event: EventEntity) {
        viewModelScope.launch {
            repository.update(event)
        }
    }
}
