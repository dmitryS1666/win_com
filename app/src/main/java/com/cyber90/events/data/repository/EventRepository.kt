package com.cyber90.events.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.Flow
import com.cyber90.events.data.dao.EventDao
import com.cyber90.events.data.dao.EventParticipantCount
import com.cyber90.events.data.dao.LiveResultDao
import com.cyber90.events.data.dao.ParticipantDao
import com.cyber90.events.data.dao.TeamParticipantDao
import com.cyber90.events.data.entity.EventEntity
import com.cyber90.events.data.entity.ParticipantEntity
import com.cyber90.events.data.entity.ResultEntity
import com.cyber90.events.data.entity.TeamParticipantEntity

class EventRepository(
    private val dao: EventDao,
    private val participantDao: ParticipantDao,
    private val teamParticipantDao: TeamParticipantDao,
    private val resultDao: LiveResultDao
) {

    fun getAllEvents(): Flow<List<EventEntity>> = dao.getAllEvents()

    suspend fun insert(event: EventEntity): Long = dao.insert(event)

    suspend fun update(event: EventEntity) = dao.update(event)

    suspend fun delete(id: Int) = dao.delete(id)

    fun getById(id: Int): LiveData<EventEntity> = dao.getById(id)

    fun getParticipantsByEventId(eventId: Int): LiveData<List<ParticipantEntity>> {
        return participantDao.getParticipantsForEvent(eventId).asLiveData()
    }

    suspend fun insertParticipant(participant: ParticipantEntity) {
        participantDao.insert(participant)
    }

    suspend fun deleteParticipantsByEventId(eventId: Int) {
        participantDao.deleteByEventId(eventId)
    }

    fun getParticipantCountsByEvent(): Flow<List<EventParticipantCount>> {
        return participantDao.getParticipantCountsByEvent()
    }

    suspend fun getEventByIdNow(id: Int): EventEntity? {
        return dao.getEventByIdNow(id)
    }

    suspend fun updateLapTime(eventId: Int, name: String, lapTime: String) {
        participantDao.updateLapTime(eventId, name, lapTime)
    }

    suspend fun updatePosition(eventId: Int, name: String, pos: String) {
        participantDao.updatePosition(eventId, name, pos)
    }

    fun getFinishedEvents(): LiveData<List<EventEntity>> {
        return dao.getFinishedEvents()
    }

    fun getFinishedEventById(id: Int): EventEntity? {
        return dao.getFinishedEventById(id)
    }

    fun getResultsByEventId(eventId: Int): LiveData<List<ResultEntity>> {
        return resultDao.getResultsByEventId(eventId)
    }

    suspend fun getResultsByEventIdNow(eventId: Int): List<ResultEntity> {
        return resultDao.getResultsByEventIdNow(eventId)
    }

    suspend fun getAllParticipants(): Flow<List<ParticipantEntity>> {
        return participantDao.getAllParticipants()
    }
}
