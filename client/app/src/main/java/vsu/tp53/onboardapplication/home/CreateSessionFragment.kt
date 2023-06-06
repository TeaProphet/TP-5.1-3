package vsu.tp53.onboardapplication.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.springframework.web.client.RestTemplate
import vsu.tp53.onboardapplication.R
import vsu.tp53.onboardapplication.auth.service.AuthService
import vsu.tp53.onboardapplication.auth.service.ProfileService
import vsu.tp53.onboardapplication.databinding.FragmentCreateSessionBinding
import vsu.tp53.onboardapplication.databinding.FragmentHomeBinding
import vsu.tp53.onboardapplication.databinding.FragmentProfileBinding
import vsu.tp53.onboardapplication.home.service.SessionService

class CreateSessionFragment : Fragment() {

    private var _binding: FragmentCreateSessionBinding? = null
    private lateinit var _authService: AuthService
    private val authService get() = _authService
    private lateinit var _profileService: ProfileService
    private val profileService get() = _profileService
    private lateinit var _sessionService: SessionService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateSessionBinding.inflate(inflater, container, false)
        if (container != null) {
            _authService = AuthService(RestTemplate(), container.context)
            _profileService = ProfileService(RestTemplate(), container.context)
            _sessionService = SessionService(RestTemplate(), container.context)
        }
        lifecycleScope.launch {
            if (!_authService.checkIfUserLoggedIn() || !profileService.getProfileInfo(null)!!.is_admin){
                _binding!!.createSessionButton.visibility = View.GONE
            } else {
                _binding!!.createSessionButton.setOnClickListener({
                    _sessionService
                })
            }
        }
        return inflater.inflate(R.layout.fragment_create_session, container, false)
    }
}