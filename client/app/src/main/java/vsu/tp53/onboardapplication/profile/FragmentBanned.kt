package vsu.tp53.onboardapplication.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import vsu.tp53.onboardapplication.databinding.FragmentBannedBinding

class FragmentBanned : Fragment() {
    private var _binding: FragmentBannedBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBannedBinding.inflate(inflater, container, false)

        return binding.root
    }
}