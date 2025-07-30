package win.com.data.repository

import kotlinx.coroutines.flow.Flow
import win.com.data.dao.EventDao
import win.com.data.entity.EventEntity

class EventRepository(private val dao: EventDao) {

    fun getAllEvents(): Flow<List<EventEntity>> = dao.getAllEvents()

    suspend fun insert(event: EventEntity): Long = dao.insert(event)

    suspend fun update(event: EventEntity) = dao.update(event)

    suspend fun delete(event: EventEntity) = dao.delete(event)

    suspend fun getById(id: Long): EventEntity? = dao.getById(id)
}
