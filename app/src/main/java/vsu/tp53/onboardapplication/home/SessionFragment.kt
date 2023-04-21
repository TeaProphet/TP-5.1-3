package vsu.tp53.onboardapplication.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import vsu.tp53.onboardapplication.R
import vsu.tp53.onboardapplication.databinding.FragmentSessionBinding

/**
 * A simple [Fragment] subclass.
 * Use the [SessionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SessionFragment : Fragment() {
    private lateinit var binding: FragmentSessionBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSessionBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }
}