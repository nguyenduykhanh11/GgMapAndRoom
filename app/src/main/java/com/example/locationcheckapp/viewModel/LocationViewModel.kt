package com.example.locationcheckapp.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.locationcheckapp.local.AppDatabase
import com.example.locationcheckapp.local.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val locationDao = AppDatabase.getInstance(application).locationDao()

    val readAllData: LiveData<List<Location>> = locationDao.readData()

    fun insert(location: Location) {
        viewModelScope.launch(Dispatchers.IO) {
            locationDao.insert(location)
        }
    }

    fun deleteAfterLast10Records(){
        viewModelScope.launch(Dispatchers.IO) {
            locationDao.deleteAfterLast10Records()
        }
    }
}