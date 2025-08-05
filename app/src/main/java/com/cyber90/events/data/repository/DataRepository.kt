package com.cyber90.events.data.repository

import com.cyber90.events.data.dao.EventDao
import com.cyber90.events.data.dao.ParticipantDao
import com.cyber90.events.data.dao.TeamDao
import com.cyber90.events.data.dao.TeamParticipantDao

class DataRepository(
    private val eventDao: EventDao,
    private val participantDao: ParticipantDao,
    private val teamParticipantDao: TeamParticipantDao,
    private val teamDao: TeamDao
) {
    suspend fun clearAllData() {
        eventDao.clearAllEvents()
        participantDao.clearAllParticipants()
        teamDao.clearAllTeams()
        teamParticipantDao.clearAllTeamParticipants()
    }
}