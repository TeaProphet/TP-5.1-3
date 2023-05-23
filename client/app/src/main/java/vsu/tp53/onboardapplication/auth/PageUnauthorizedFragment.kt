package vsu.tp53.onboardapplication.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import org.springframework.web.client.RestTemplate
import vsu.tp53.onboardapplication.R
import vsu.tp53.onboardapplication.auth.service.AuthService
import vsu.tp53.onboardapplication.databinding.UnauthorizedBinding

class PageUnauthorizedFragment : Fragment() {
    private var _binding: UnauthorizedBinding? = null
    private val binding get() = _binding!!

//    private lateinit var _authService: AuthService
//    private val authService get() = _authService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = UnauthorizedBinding.inflate(inflater, container, false)
//        if (container != null) {
//            _authService = AuthService(RestTemplate(), container.context)
//        }
//
//        if (authService.checkIfUserLoggedIn()) {
//            this.findNavController().navigate(R.id.profileFragment)
//        }

        binding.buttonSignIn.setOnClickListener {
            it.findNavController().navigate(R.id.signInFragment)
        }

        binding.buttonSignUp.setOnClickListener {
            it.findNavController().navigate(R.id.signUpFragment)
        }

        binding.buttonWithoutAuth.setOnClickListener {
            it.findNavController().navigate(R.id.homeFragment)
        }

        return binding.root
    }

}