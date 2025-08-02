package win.com.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "team_participants")
data class TeamParticipantEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val teamId: Long,
    val name: String,
    val role: String
)
