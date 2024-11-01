// SharedViewModel.kt
package com.quinut.miseino.ui.shared

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _data = MutableLiveData<String>()
    val data: LiveData<String> get() = _data

    private val _requestDataUpdate = MutableLiveData<Boolean>()
    val requestDataUpdate: LiveData<Boolean> get() = _requestDataUpdate

    fun updateData(newData: String) {
        _data.postValue(newData) // Use postValue to ensure updates from background threads
    }

    fun requestDataUpdate(value: Boolean) {
        _requestDataUpdate.value = value
    }
}