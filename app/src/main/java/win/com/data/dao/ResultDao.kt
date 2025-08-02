package win.com.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import win.com.data.entity.ResultEntity

@Dao
interface ResultDao {
    @Query("SELECT * FROM results ORDER BY id DESC, time DESC")
    fun getAllResults(): Flow<List<ResultEntity>>

    @Insert
    suspend fun insert(event: ResultEntity): Int

    @Update
    suspend fun update(event: ResultEntity)

    @Delete
    suspend fun delete(event: ResultEntity)
}