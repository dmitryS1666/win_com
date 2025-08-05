package com.cyber90.events.data.repository

import kotlinx.coroutines.flow.Flow
import com.cyber90.events.data.dao.ParticipantDao
import com.cyber90.events.data.entity.ParticipantEntity

class TeamParticipantRepository(private val dao: ParticipantDao) {

    fun getAll(): Flow<List<ParticipantEntity>> = dao.getAllParticipants()

    suspend fun insert(participant: ParticipantEntity): Long = dao.insert(participant)

    suspend fun delete(id: Int) = dao.delete(id)
}