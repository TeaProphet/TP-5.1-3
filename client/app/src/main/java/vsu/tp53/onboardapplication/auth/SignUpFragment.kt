package vsu.tp53.onboardapplication.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import kotlinx.coroutines.launch
import org.springframework.web.client.RestTemplate
import vsu.tp53.onboardapplication.R
import vsu.tp53.onboardapplication.auth.service.AuthService
import vsu.tp53.onboardapplication.auth.service.Errors
import vsu.tp53.onboardapplication.databinding.SignUpBinding
import vsu.tp53.onboardapplication.model.domain.User

class SignUpFragment : Fragment() {
    private var _binding: SignUpBinding? = null
    private val binding get() = _binding!!

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
            lifecycleScope.launch {
                if (registerUser()) {
                    Log.i("SignUp-Frag", "Success")
                    it.findNavController().navigate(R.id.profileFragment)
                }
            }
        }
        return binding.root
    }

    private suspend fun registerUser(): Boolean {
        val nickname = binding.nicknameSignUp.text.toString()
        val login = binding.loginSignUp.text.toString()
        val password = binding.passwordSignUp.text.toString()
        Log.i("signUp-login", login)
        Log.i("signUp-password", password)

        if (nickname.length < 4) {
            binding.signUpError.text = "Никнейм должен содержать не менее 4 символов."
            return false
        }

        if (!authService.checkEmail(login)) {
            binding.signUpError.text = "Неверный формат логина"
            return false
        }

        if (password.length < 6) {
            binding.signUpError.text = "Пароль должен содержать не менее 6 символов"
            return false
        }

        try {
            val tokenResp = authService.registerUser(User(null, nickname, login, password))
            return if (tokenResp.error != null) {
                if (Errors.getByName(tokenResp.error.toString()) != "") {
                    binding.signUpError.text = Errors.getByName(tokenResp.error.toString())
                    false
                } else {
                    binding.signUpError.text = "Произошла ошибка"
                    false
                }
            } else {
                true
            }
        } catch (e: Exception) {
            if (Errors.getByName(e.message.toString()) != "") {
                binding.signUpError.text = Errors.getByName(e.message.toString())
            } else {
                binding.signUpError.text = "Произошла ошибка"
            }
            return false
        }
    }
}