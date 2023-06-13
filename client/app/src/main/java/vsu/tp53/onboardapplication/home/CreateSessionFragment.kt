package vsu.tp53.onboardapplication.home

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
import vsu.tp53.onboardapplication.auth.service.ProfileService
import vsu.tp53.onboardapplication.databinding.FragmentCreateSessionBinding
import vsu.tp53.onboardapplication.home.service.SessionService
import vsu.tp53.onboardapplication.model.entity.SessionEntity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

        _binding!!.createSessionButton.setOnClickListener {
            Log.i("CreateSession", "ButtonPressed")
            lifecycleScope.launch {
                Log.i("CreateSession", "Insude scope")
                createSession()
                it.findNavController().navigate(R.id.homeFragment)
            }
        }
        return _binding!!.root
    }

    private suspend fun createSession() {
        Log.i("CreateSession", "Creact session method")
        val df: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        val localDate: LocalDateTime =
            LocalDateTime.parse("${_binding!!.dateInput.text} ${_binding!!.timeInput.text}", df)
        val sessionEntity = SessionEntity(
            "",
            _binding!!.nameInput.text.toString(),
            _binding!!.addressInput.text.toString(),
            _binding!!.gamesInput.text.toString(),
            localDate.toString(),
            arrayOf(profileService.getUserNickname()),
            _binding!!.playersNumberInput.text.toString().toInt()
        )

        _sessionService.createSession(sessionEntity)
    }
}