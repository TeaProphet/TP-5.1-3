package vsu.tp53.onboardapplication.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.springframework.web.client.RestTemplate
import vsu.tp53.onboardapplication.R
import vsu.tp53.onboardapplication.auth.service.AuthService
import vsu.tp53.onboardapplication.databinding.SignInBinding
import vsu.tp53.onboardapplication.model.domain.User

class SignInFragment : Fragment() {
    private var _binding: SignInBinding? = null
    private val binding get() = _binding!!
    private lateinit var _authService: AuthService
    private val authService get() = _authService


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SignInBinding.inflate(inflater, container, false)

        if (container != null) {
            _authService = AuthService(RestTemplate(), container.context)
        }

        Log.i("messageSignIn", "SignIn!")
        binding.signInButton.setOnClickListener {
            lifecycleScope.launch {
                authUser()
                it.findNavController().navigate(R.id.profileFragment)
            }
        }

        return binding.root
    }

    private suspend fun authUser() {
        val login = binding.loginSignIn.text.toString()
        val password = binding.passwordSignIn.text.toString()
        Log.i("signIn-login", login)
        Log.i("signIn-password", password)

        authService.authorizeUser(User(null, "", login, password))
    }

}