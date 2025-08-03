package win.com.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.Flow
import win.com.data.dao.EventDao
import win.com.data.dao.EventParticipantCount
import win.com.data.dao.ParticipantDao
import win.com.data.entity.EventEntity
import win.com.data.entity.ParticipantEntity

class EventRepository(private val dao: EventDao, private val participantDao: ParticipantDao) {

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
}
