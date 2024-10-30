package com.quinut.miseino.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.quinut.miseino.databinding.FragmentListBinding

class ListFragment : Fragment() {

private var _binding: FragmentListBinding? = null
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val listViewModel =
            ViewModelProvider(this).get(ListViewModel::class.java)

    _binding = FragmentListBinding.inflate(inflater, container, false)
    val root: View = binding.root


    return root
  }

override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}