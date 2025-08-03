package win.com.data.repository

import win.com.data.dao.EventDao
import win.com.data.dao.ParticipantDao
import win.com.data.dao.TeamDao
import win.com.data.dao.TeamParticipantDao

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