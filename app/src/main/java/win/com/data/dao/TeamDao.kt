package win.com.data.dao

import androidx.lifecycle.LiveData
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
    suspend fun insert(team: TeamEntity): Long

    @Update
    suspend fun update(team: TeamEntity)

    @Delete
    suspend fun delete(team: TeamEntity)

    @Query("SELECT * FROM teams WHERE id = :teamId LIMIT 1")
    fun getById(teamId: Int): LiveData<TeamEntity>
}
