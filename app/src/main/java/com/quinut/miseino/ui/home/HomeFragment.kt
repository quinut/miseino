package com.quinut.miseino.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.quinut.miseino.databinding.FragmentHomeBinding
import com.quinut.miseino.ui.shared.SharedViewModel
import java.io.IOException

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel.data.observe(viewLifecycleOwner) { data ->
            Log.d("HomeFragment", "Data received: $data")
            binding.dataTextView.text = data
        }

        binding.button.setOnClickListener {
            receiveDataFromBluetooth()
        }
    }

    private fun receiveDataFromBluetooth() {
        val buffer = ByteArray(1024)
        var bytes: Int

        Thread {
            try {
                val inputStream = sharedViewModel.bluetoothSocket.value?.inputStream
                bytes = inputStream?.read(buffer) ?: 0
                if (bytes > 0) {
                    val data = String(buffer, 0, bytes)
                    Log.d("HomeFragment", "Data read from input streamHF: $data")
                    activity?.runOnUiThread {
                        sharedViewModel.updateData(data)
                    }
                } else {
                    Log.e("HomeFragment", "No data receivedHF")
                    Log.d("HomeFragment", sharedViewModel.getLatestData().toString())

                    activity?.runOnUiThread {
                        sharedViewModel.updateData("No data receivedHF")
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("HomeFragment", "Error receiving data", e)
                activity?.runOnUiThread {
                    sharedViewModel.updateData("Error receiving data")
                }
            }
        }.start()
    }





    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}