package win.com.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.map
import win.com.data.database.AppDatabase
import win.com.data.entity.EventEntity
import win.com.data.repository.EventRepository

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: EventRepository

    val lastEvent: LiveData<EventEntity?>

    init {
        val dao = AppDatabase.getDatabase(application).eventDao()
        repository = EventRepository(dao)

        lastEvent = repository.getAllEvents()
            .map { it.firstOrNull() }
            .asLiveData()
    }
}
