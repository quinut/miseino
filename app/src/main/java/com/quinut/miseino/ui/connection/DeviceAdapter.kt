package com.quinut.miseino.ui.connection

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.quinut.miseino.R

class DeviceAdapter(
    private val deviceList: List<BluetoothDevice>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(device: BluetoothDevice)
    }

    class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById(R.id.device_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = deviceList[position]
        if (ActivityCompat.checkSelfPermission(
                holder.itemView.context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            holder.deviceName.text = device.name ?: "Unknown Device"
        }
        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(device)
        }
    }

    override fun getItemCount(): Int {
        return deviceList.size
    }
}