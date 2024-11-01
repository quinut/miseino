package com.quinut.miseino.ui.connection

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quinut.miseino.R

class ConnectionFragment : Fragment(R.layout.fragment_connection), DeviceAdapter.OnItemClickListener {

    private lateinit var deviceAdapter: DeviceAdapter
    private val deviceList = mutableListOf<BluetoothDevice>()

    private val requestBluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            enableBluetooth()
        } else {
            // Handle the case where the user denies the permission
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_connection, container, false)
        setupRecyclerView(view)
        setupBluetooth()
        return view
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.deviceRecyclerView)
        deviceAdapter = DeviceAdapter(deviceList, this)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = deviceAdapter
    }

    private fun setupBluetooth() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestBluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            enableBluetooth()
        }
    }

    private fun enableBluetooth() {
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            return
        }
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            populateDeviceList(bluetoothAdapter)
        }
    }

    private fun populateDeviceList(bluetoothAdapter: BluetoothAdapter) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
            if (pairedDevices != null) {
                deviceList.clear()
                deviceList.addAll(pairedDevices)
                deviceAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onItemClick(device: BluetoothDevice) {
        // Handle the connection logic here
        connectToDevice(device)
    }

    private fun connectToDevice(device: BluetoothDevice) {
        // Implement the connection logic
        // For example, you can use BluetoothSocket to connect to the device
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 1
    }
}