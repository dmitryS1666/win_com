package com.cyber90.events.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.cyber90.events.data.database.AppDatabase
import com.cyber90.events.data.entity.EventEntity
import com.cyber90.events.data.repository.EventRepository

class CreateEventViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: EventRepository

    init {
        val dao = AppDatabase.getDatabase(application).eventDao()
        val participantDao = AppDatabase.getDatabase(application).participantDao()
        val teamParticipantDao = AppDatabase.getDatabase(application).teamParticipantDao()
        val liveResultDao = AppDatabase.getDatabase(application).liveResultDao()
        repository = EventRepository(dao, participantDao, teamParticipantDao, liveResultDao)
    }

    fun createEvent(event: EventEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.insert(event)
            onComplete()
        }
    }
}
