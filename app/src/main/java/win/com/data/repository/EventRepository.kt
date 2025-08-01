package win.com.data.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import win.com.data.dao.EventDao
import win.com.data.entity.EventEntity

class EventRepository(private val dao: EventDao) {

    fun getAllEvents(): Flow<List<EventEntity>> = dao.getAllEvents()

    suspend fun insert(event: EventEntity): Long = dao.insert(event)

    suspend fun update(event: EventEntity) = dao.update(event)

    suspend fun delete(id: Int) = dao.delete(id)

    fun getById(id: Int): LiveData<EventEntity> = dao.getById(id)
}
