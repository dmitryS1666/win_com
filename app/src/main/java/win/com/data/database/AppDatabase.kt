package win.com.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import win.com.data.dao.EventDao
import win.com.data.dao.LiveResultDao
import win.com.data.dao.ParticipantDao
import win.com.data.dao.ResultDao
import win.com.data.dao.TeamDao
import win.com.data.dao.TeamParticipantDao
import win.com.data.entity.EventEntity
import win.com.data.entity.LiveResultEntity
import win.com.data.entity.ParticipantEntity
import win.com.data.entity.ResultEntity
import win.com.data.entity.TeamEntity
import win.com.data.entity.TeamParticipantEntity

@Database(
    entities = [
        EventEntity::class,
        ParticipantEntity::class,
        TeamEntity::class,
        ResultEntity::class,
        TeamParticipantEntity::class,
        LiveResultEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao

    abstract fun participantDao(): ParticipantDao

    abstract fun teamDao(): TeamDao

    abstract fun teamParticipantDao(): TeamParticipantDao

    abstract fun liveResultDao(): LiveResultDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "esports_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
