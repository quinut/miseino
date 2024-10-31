package com.quinut.miseino.ui.connection

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.quinut.miseino.databinding.FragmentConnectionBinding

class ConnectionFragment : Fragment() {

    private var _binding: FragmentConnectionBinding? = null
    private val binding get() = _binding!!
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val BLUETOOTH_CONNECT_PERMISSION_REQUEST_CODE = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val connectionViewModel =
            ViewModelProvider(this)[ConnectionViewModel::class.java]

        _binding = FragmentConnectionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize Bluetooth components
        val bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        // Check Bluetooth connection status
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkBluetoothConnection()
        } else {
            displayBluetoothConnectionStatus(binding.deviceInfo)
        }

        return root
    }

    private fun checkBluetoothConnection() {
        val deviceInfoTextView: TextView = binding.deviceInfo

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.BLUETOOTH_CONNECT), BLUETOOTH_CONNECT_PERMISSION_REQUEST_CODE)
        } else {
            displayBluetoothConnectionStatus(deviceInfoTextView)
        }
    }

    private fun displayBluetoothConnectionStatus(deviceInfoTextView: TextView) {
        if (bluetoothAdapter.isEnabled) {
            bluetoothAdapter.getProfileProxy(requireContext(), object : BluetoothProfile.ServiceListener {
                override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                    val connectedDevices: List<BluetoothDevice> = proxy.connectedDevices
                    if (connectedDevices.isNotEmpty()) {
                        val deviceInfo = StringBuilder("Connected Devices:\n")
                        for (device in connectedDevices) {
                            deviceInfo.append("Name: ${device.name}, Address: ${device.address}\n")
                        }
                        deviceInfoTextView.text = deviceInfo.toString()
                    } else {
                        deviceInfoTextView.text = "No devices connected"
                    }
                    bluetoothAdapter.closeProfileProxy(profile, proxy)
                }

                override fun onServiceDisconnected(profile: Int) {
                    deviceInfoTextView.text = "No devices connected"
                }
            }, BluetoothProfile.GATT) // Use the appropriate profile for your use case
        } else {
            Toast.makeText(context, "Bluetooth is disabled", Toast.LENGTH_SHORT).show()
            deviceInfoTextView.text = "Bluetooth is disabled"
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == BLUETOOTH_CONNECT_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    checkBluetoothConnection()
                }
            } else {
                Toast.makeText(context, "Bluetooth permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}