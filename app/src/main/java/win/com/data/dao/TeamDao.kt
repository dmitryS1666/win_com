package win.com.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import win.com.data.entity.TeamEntity

@Dao
interface TeamDao {
    @Query("SELECT * FROM teams ORDER BY id DESC")
    fun getAllTeams(): Flow<List<TeamEntity>>

    @Insert
    suspend fun insert(event: TeamEntity): Long

    @Update
    suspend fun update(event: TeamEntity)

    @Delete
    suspend fun delete(event: TeamEntity)
}