package win.com.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "live_results")
data class LiveResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventId: Int,
    val participantId: Int,
    val lapTime: String = "",
    val result: String = "-",
    val finalPosition: String = "-"
)
