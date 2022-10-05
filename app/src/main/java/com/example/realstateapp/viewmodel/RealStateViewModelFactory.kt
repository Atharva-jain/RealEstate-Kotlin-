package com.example.realstateapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.realstateapp.data.remote.ApiInterface

class RealStateViewModelFactory(
    private val app: Application,
    private val apiInterface: ApiInterface
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return RealStateViewModel(app, apiInterface) as T
    }
}
