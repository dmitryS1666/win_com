package win.com.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import win.com.data.repository.DataRepository

class DashboardViewModelFactory(
    private val application: Application,
    private val dataRepository: DataRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(application, dataRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}