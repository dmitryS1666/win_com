package win.com.data.repository

import kotlinx.coroutines.flow.Flow
import win.com.data.dao.ParticipantDao
import win.com.data.entity.ParticipantEntity

class ParticipantRepository(private val dao: ParticipantDao) {

    fun getAll(): Flow<List<ParticipantEntity>> = dao.getAllParticipants()

    suspend fun insert(participant: ParticipantEntity): Long = dao.insert(participant)

    suspend fun delete(id: Int) = dao.delete(id)
}
