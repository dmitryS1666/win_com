package win.com.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val date: String,
    val time: String,
    val category: String,
    val mode: String,
    val rounds: Int,
    val maxParticipants: Int,
    val isPrivate: Boolean,
    val status: String = "PLANNED"
)
