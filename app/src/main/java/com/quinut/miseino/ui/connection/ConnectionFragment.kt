package com.quinut.miseino.ui.connection

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val requestBluetoothPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.BLUETOOTH_CONNECT] == true &&
            permissions[Manifest.permission.BLUETOOTH_SCAN] == true) {
            enableBluetooth()
        } else {
            // Handle the case where the user denies the permissions
        }
    }

    private val deviceDiscoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    if (it.name == "HC-06" && !deviceList.contains(it)) {
                        deviceList.add(it)
                        deviceAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_connection, container, false)
        setupRecyclerView(view)
        setupBluetooth()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel.requestDataUpdate.observe(viewLifecycleOwner) { request ->
            if (request == true) {
                sendLatestData()
                sharedViewModel.requestDataUpdate(false) // Reset the request
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unregisterReceiver(deviceDiscoveryReceiver)
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.deviceRecyclerView)
        deviceAdapter = DeviceAdapter(deviceList, this)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = deviceAdapter
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun setupBluetooth() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED) {
            requestBluetoothPermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            )
        } else {
            enableBluetooth()
        }
    }

    private fun enableBluetooth() {
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            return
        }
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            populateDeviceList()
        }
    }

    private fun populateDeviceList() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED) {
            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
            deviceList.clear()
            pairedDevices?.let {
                deviceList.addAll(it.filter { device -> device.name == "HC-06" })
            }
            deviceAdapter.notifyDataSetChanged()

            // Register for broadcasts when a device is discovered
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            requireContext().registerReceiver(deviceDiscoveryReceiver, filter)

            // Start discovery
            bluetoothAdapter?.startDiscovery()
        }
    }

    override fun onItemClick(device: BluetoothDevice) {
        connectToDevice(device)
    }

    private fun connectToDevice(device: BluetoothDevice) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED) {
            val uuid: UUID = device.uuids[0].uuid // Use the first UUID from the device
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
            bluetoothSocket?.let { socket ->
                try {
                    socket.connect()
                    // Connection successful, start communication
                    startCommunication(socket)
                } catch (e: IOException) {
                    Log.e("ConnectionFragment", "Error connecting to device, attempt 1", e)
                    try {
                        socket.close()
                    } catch (closeException: IOException) {
                        Log.e("ConnectionFragment", "Error closing socket after failed attempt", closeException)
                    }
                    // Show toast message on the main thread
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Unable to connect to device", Toast.LENGTH_SHORT).show()
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
                    if (bytes == -1) {
                        Log.e("ConnectionFragment", "Input stream closed")
                        break
                    }
                    val readMessage = String(buffer, 0, bytes)
                    activity?.runOnUiThread {
                        sharedViewModel.updateData(readMessage)
                    }
                } catch (e: IOException) {
                    Log.e("ConnectionFragment", "Error reading from input stream", e)
                    break
                }
            }
            try {
                socket.close()
            } catch (closeException: IOException) {
                Log.e("ConnectionFragment", "Error closing socket", closeException)
            }
        }.start()
    }

    private fun sendLatestData() {
        bluetoothSocket?.let { socket ->
            val inputStream: InputStream = socket.inputStream
            val buffer = ByteArray(1024)
            try {
                val bytes = inputStream.read(buffer)
                if (bytes != -1) {
                    val readMessage = String(buffer, 0, bytes)
                    sharedViewModel.updateData(readMessage)
                } else {
                    Log.e("ConnectionFragment", "Input stream closed")
                }
            } catch (e: IOException) {
                Log.e("ConnectionFragment", "Error reading from input stream", e)
            }
        }
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 1
    }
}