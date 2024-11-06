// SharedViewModel.kt
package com.quinut.miseino.ui.shared

import android.bluetooth.BluetoothSocket
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _data = MutableLiveData<String>()
    val data: LiveData<String> get() = _data

    private val _requestDataUpdate = MutableLiveData<Boolean>()
    val requestDataUpdate: LiveData<Boolean> get() = _requestDataUpdate

    private val _bluetoothSocket = MutableLiveData<BluetoothSocket>()
    val bluetoothSocket: LiveData<BluetoothSocket> get() = _bluetoothSocket

    fun updateData(newData: String) {
        _data.postValue(newData)
    }

    fun requestDataUpdate(value: Boolean) {
        _requestDataUpdate.value = value
    }

    fun setBluetoothSocket(socket: BluetoothSocket) {
        _bluetoothSocket.value = socket
    }

    fun getLatestData(): String? {
        return _data.value
    }
}