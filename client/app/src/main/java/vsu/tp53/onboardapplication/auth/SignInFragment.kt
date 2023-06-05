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
import vsu.tp53.onboardapplication.auth.service.Errors
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
                try {
                    if (authUser())
                        it.findNavController().navigate(R.id.profileFragment)
                } catch (_: Exception) {

                }
            }
        }

        return binding.root
    }

    private suspend fun authUser(): Boolean {
        val login = binding.loginSignIn.text.toString()
        val password = binding.passwordSignIn.text.toString()
        Log.i("signIn-login", login)
        Log.i("signIn-password", password)

        try {
            val resp = authService.authorizeUser(User(null, "", login, password))
            return if (resp.error != null) {
                Log.w("SignIn-Frag", resp.error.toString())
                if (Errors.getByName(resp.error.toString()) != "") {
                    binding.signInError.text = Errors.getByName(resp.error.toString())
                } else {
                    binding.signInError.text = "Произошла ошибка"
                }
                false
            } else {
                true
            }
        } catch (e: Exception) {
            Log.e("SignIn-Frag-ex", e.message.toString())
            if (Errors.getByName(e.message.toString()) != "") {
                binding.signInError.text = Errors.getByName(e.message.toString())
            } else {
                binding.signInError.text = "Произошла ошибка"
            }
            return false
        }
    }
}