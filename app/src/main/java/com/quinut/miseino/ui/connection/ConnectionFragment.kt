package com.quinut.miseino.ui.connection

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quinut.miseino.R
import com.quinut.miseino.ui.shared.SharedViewModel
import java.io.IOException
import java.io.InputStream
import java.util.*

class ConnectionFragment : Fragment(R.layout.fragment_connection), DeviceAdapter.OnItemClickListener {

    private lateinit var deviceAdapter: DeviceAdapter
    private val deviceList = mutableListOf<BluetoothDevice>()
    private var bluetoothSocket: BluetoothSocket? = null
    private val sharedViewModel: SharedViewModel by activityViewModels()

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
        connectToDevice(device)
    }

    private fun connectToDevice(device: BluetoothDevice) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val uuid: UUID = device.uuids[0].uuid // Use the first UUID from the device
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
            bluetoothSocket?.let { socket ->
                try {
                    socket.connect()
                    // Connection successful, start communication
                    startCommunication(socket)
                } catch (e: IOException) {
                    Log.e("ConnectionFragment", "Error connecting to device", e)
                    try {
                        socket.close()
                    } catch (closeException: IOException) {
                        Log.e("ConnectionFragment", "Error closing socket", closeException)
                    }
                }
            }
        }
    }

    private fun startCommunication(socket: BluetoothSocket) {
        val inputStream: InputStream = socket.inputStream
        val buffer = ByteArray(1024)
        var bytes: Int

        Thread {
            while (true) {
                try {
                    bytes = inputStream.read(buffer)
                    val readMessage = String(buffer, 0, bytes)
                    activity?.runOnUiThread {
                        sharedViewModel.updateData(readMessage)
                    }
                } catch (e: IOException) {
                    Log.e("ConnectionFragment", "Error reading from input stream", e)
                    break
                }
            }
        }.start()
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 1
    }
}