package vsu.tp53.onboardapplication.home

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.springframework.web.client.RestTemplate
import vsu.tp53.onboardapplication.R
import vsu.tp53.onboardapplication.auth.service.AuthService
import vsu.tp53.onboardapplication.auth.service.ProfileService
import vsu.tp53.onboardapplication.databinding.FragmentSessionBinding
import vsu.tp53.onboardapplication.home.service.SessionService
import vsu.tp53.onboardapplication.model.entity.ProfileInfoEntity
import vsu.tp53.onboardapplication.model.entity.SessionInfoBody


class SessionFragment : Fragment() {

    private lateinit var binding: FragmentSessionBinding
    private lateinit var playersRecyclerView: RecyclerView
    private lateinit var playersAdapter: PlayerAdapter
    private lateinit var _sessionService: SessionService
    private lateinit var _profileService: ProfileService
    private lateinit var _authService: AuthService
    private val authService get() = _authService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSessionBinding.inflate(inflater, container, false)
        if (container != null) {
            _sessionService = SessionService(RestTemplate(), container.context)
            _profileService = ProfileService(RestTemplate(), container.context)
            _authService = AuthService(RestTemplate(), container.context)
        }
        lifecycleScope.launch {
            val sessionId = requireArguments().getInt("id")
            val sessionInfo: SessionInfoBody? = _sessionService.getSessionInfo(sessionId)
            if (sessionInfo != null) {
                binding.inSessionID.setText(sessionId.toString())
                setSessionData(sessionInfo)
            }
            Log.i("SessionFragment", sessionInfo.toString())
            val profileInfo = _profileService.getProfileInfo(null)
            Log.i("SessionFragment", profileInfo.toString())
            val res: Boolean =
                !(authService.checkIfUserLoggedIn() && (_profileService.getUserNickname() == sessionInfo!!.owner
                        || profileInfo!!.is_admin))
            Log.i("SessionFragment-res", res.toString())
            Log.i(
                "SessionFragment-owner",
                (_profileService.getUserNickname() != sessionInfo!!.owner).toString()
            )
            if (res) {
                binding.changeSessionNameButton.isVisible = false
                binding.removeSessionButton.isVisible = false
            }

            if (!authService.checkIfUserLoggedIn()) {
                binding.joinSessionButton.isVisible = false
            }
            if (authService.checkIfUserLoggedIn()) {
                if (profileInfo!!.played_sessions != null) {
                    if (profileInfo.played_sessions!!.contains(
                            binding.inSessionID.text.toString().toInt()
                        )
                    ) {
                        binding.joinSessionButton.text = "Отписаться"
                    } else {
                        binding.joinSessionButton.text = "Записаться"
                    }
                } else {
                    binding.joinSessionButton.text = "Записаться"
                }
            }
            binding.progressContent.visibility = View.GONE
            binding.pageContent.visibility = View.VISIBLE
        }

        binding.joinSessionButton.setOnClickListener {
            lifecycleScope.launch {
                if (binding.joinSessionButton.text == "Записаться") {
                    _sessionService.joinSession(binding.inSessionID.text.toString().toInt())
                    binding.joinSessionButton.text = "Отписаться"
                } else {
                    _sessionService.leaveSession(binding.inSessionID.text.toString().toInt())
                    binding.joinSessionButton.text = "Записаться"
                }
            }
        }

        binding.changeSessionNameButton.setOnClickListener {
            val taskEditText = EditText(this.context)
            val dialog: AlertDialog = AlertDialog.Builder(this.context)
                .setTitle("Изменение названия")
                .setView(taskEditText)
                .setPositiveButton("Изменить") { _, _ ->
                    lifecycleScope.launch {
                        _sessionService.changeSessionName(
                            taskEditText.text.toString(),
                            binding.inSessionName.text.toString(),
                            binding.inSessionID.text.toString().toInt()
                        )
                        binding.inSessionName.text = taskEditText.text.toString()
                    }
                }
                .setNegativeButton("Отменить", null)
                .create()
            dialog.show()
        }

        binding.removeSessionButton.setOnClickListener {
            lifecycleScope.launch {
                _sessionService.deleteSession(binding.inSessionID.text.toString().toInt())
                it.findNavController().navigate(R.id.homeFragment)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private suspend fun setSessionData(sessionInfo: SessionInfoBody) {
        binding.inSessionName.setText(sessionInfo.name)
        binding.inSessionDate.setText(sessionInfo.date_time.toString().replace('T', ' '))
        binding.inSessionCity.setText(sessionInfo.city_address)
        binding.inSessionGames.setText(sessionInfo.games)
        binding.inSessionPlayers.setText(sessionInfo.players_max.toString())
        val listPlayers: MutableList<PlayerModel> = java.util.ArrayList()
        val players: Array<String> = sessionInfo.players
        for (nickname in players) {
            val profileInfo: ProfileInfoEntity? = _profileService.getProfileInfo(nickname)
            if (profileInfo != null) {
                listPlayers.add(PlayerModel(nickname, profileInfo.reputation))
            }
        }
        playersRecyclerView = binding.recyclerPlayersPlayer
        playersRecyclerView.layoutManager = LinearLayoutManager(activity)

        playersAdapter = PlayerAdapter(listPlayers)
        playersRecyclerView.adapter = playersAdapter
    }
}