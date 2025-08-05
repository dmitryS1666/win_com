package com.cyber90.events.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "participants")
data class ParticipantEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var eventId: Int,
    val nickname: String,
    val team: String?,
    val role: String, // HOST, PILOT, ORGA
    var lapTime: String? = null,
    var pos: String? = null
)
