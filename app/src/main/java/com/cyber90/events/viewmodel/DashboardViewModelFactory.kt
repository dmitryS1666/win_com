package com.cyber90.events.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cyber90.events.data.repository.DataRepository

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