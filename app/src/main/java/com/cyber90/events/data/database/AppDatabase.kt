package com.cyber90.events.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cyber90.events.data.dao.EventDao
import com.cyber90.events.data.dao.LiveResultDao
import com.cyber90.events.data.dao.ParticipantDao
import com.cyber90.events.data.dao.ResultDao
import com.cyber90.events.data.dao.TeamDao
import com.cyber90.events.data.dao.TeamParticipantDao
import com.cyber90.events.data.entity.EventEntity
import com.cyber90.events.data.entity.LiveResultEntity
import com.cyber90.events.data.entity.ParticipantEntity
import com.cyber90.events.data.entity.ResultEntity
import com.cyber90.events.data.entity.TeamEntity
import com.cyber90.events.data.entity.TeamParticipantEntity

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
