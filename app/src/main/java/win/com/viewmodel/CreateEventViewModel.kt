package win.com.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import win.com.data.database.AppDatabase
import win.com.data.entity.EventEntity
import win.com.data.repository.EventRepository

class CreateEventViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: EventRepository

    init {
        val dao = AppDatabase.getDatabase(application).eventDao()
        val participantDao = AppDatabase.getDatabase(application).participantDao()
        val liveResultDao = AppDatabase.getDatabase(application).liveResultDao()
        repository = EventRepository(dao, participantDao, liveResultDao)
    }

    fun createEvent(event: EventEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.insert(event)
            onComplete()
        }
    }
}
