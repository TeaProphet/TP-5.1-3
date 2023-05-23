package vsu.tp53.onboardapplication.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import org.springframework.web.client.RestTemplate
import vsu.tp53.onboardapplication.R
import vsu.tp53.onboardapplication.auth.service.AuthService
import vsu.tp53.onboardapplication.databinding.SignUpBinding
import vsu.tp53.onboardapplication.model.domain.User

class SignUpFragment : Fragment() {
    private var _binding: SignUpBinding? = null
    private val binding get() = _binding!!

    //    @Autowired
    private lateinit var _authService: AuthService
    private val authService get() = _authService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SignUpBinding.inflate(inflater, container, false)
        if (container != null) {
            _authService = AuthService(RestTemplate(), container.context)
        }

        Log.i("messageSignUp", "SignUp!")
        binding.signUpButton.setOnClickListener {
            registerUser()
            it.findNavController().navigate(R.id.profileFragment)
        }
        return binding.root
    }

    private fun registerUser() {
        val login = binding.loginSignUp.text.toString()
        val password = binding.passwordSignUp.text.toString()
        Log.i("signUp-login", login)
        Log.i("signUp-password", password)

        authService.registerUser(User(login, password))
    }
}