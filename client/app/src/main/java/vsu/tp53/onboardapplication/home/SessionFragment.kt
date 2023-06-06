package vsu.tp53.onboardapplication.home

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.springframework.web.client.RestTemplate
import vsu.tp53.onboardapplication.R
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSessionBinding.inflate(inflater, container, false)
        if (container != null) {
            _sessionService = SessionService(RestTemplate(), container.context)
            _profileService = ProfileService(RestTemplate(), container.context)
        }
        lifecycleScope.launch {
            val sessionId = requireArguments().getInt("id")
            val sessionInfo: SessionInfoBody? = _sessionService.getSessionInfo(sessionId)
            if (sessionInfo != null){
                binding.inSessionID.setText(sessionId.toString())
                setSessionData(sessionInfo)
            }
            binding.progressContent.visibility = View.GONE
            binding.pageContent.visibility = View.VISIBLE
        }

        binding.joinSessionButton.setOnClickListener {
            if (binding.joinSessionButton.text == "Записаться"){
                binding.joinSessionButton.setText("Отписаться")
            } else {
                binding.joinSessionButton.setText("Записаться")
            }
        }

        binding.changeSessionNameButton.setOnClickListener {
            val taskEditText = EditText(this.context)
            val dialog: AlertDialog = AlertDialog.Builder(this.context)
                .setTitle("Изменение названия")
                .setView(taskEditText)
                .setPositiveButton("Изменить", DialogInterface.OnClickListener { dialog, which ->
                    binding.inSessionName.text = taskEditText.text.toString()
                })
                .setNegativeButton("Отменить", null)
                .create()
            dialog.show()
        }

        binding.removeSessionButton.setOnClickListener {

            it.findNavController().navigate(R.id.homeFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private suspend fun setSessionData(sessionInfo: SessionInfoBody){
        binding.inSessionName.setText(sessionInfo.name)
        binding.inSessionDate.setText(sessionInfo.date_time.toString().replace('T',' '))
        binding.inSessionCity.setText(sessionInfo.city_address)
        binding.inSessionGames.setText(sessionInfo.games)
        binding.inSessionPlayers.setText(sessionInfo.players_max.toString())
        val listPlayers: MutableList<PlayerModel> = java.util.ArrayList()
        val players: Array<String> = sessionInfo.players
        for (nickname in players){
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