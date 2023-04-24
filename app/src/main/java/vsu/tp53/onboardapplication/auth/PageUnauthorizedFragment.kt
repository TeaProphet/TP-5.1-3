package vsu.tp53.onboardapplication.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import vsu.tp53.onboardapplication.R
import vsu.tp53.onboardapplication.databinding.UnauthorizedBinding

class PageUnauthorizedFragment : Fragment() {
    private var _binding: UnauthorizedBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = UnauthorizedBinding.inflate(inflater, container, false)

        binding.buttonSignIn.setOnClickListener {
            it.findNavController().navigate(R.id.homeFragment)
        }

        binding.buttonSignUp.setOnClickListener {
            it.findNavController().navigate(R.id.homeFragment)
        }

        binding.buttonWithoutAuth.setOnClickListener {
            it.findNavController().navigate(R.id.homeFragment)
        }

        return binding.root
    }

}