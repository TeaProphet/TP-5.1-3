package vsu.tp53.onboardapplication.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.springframework.web.client.RestTemplate
import vsu.tp53.onboardapplication.auth.service.ProfileService
import vsu.tp53.onboardapplication.databinding.FragmentUserSessionsBinding
import vsu.tp53.onboardapplication.home.SessionAdapter
import vsu.tp53.onboardapplication.home.service.SessionService
import vsu.tp53.onboardapplication.model.entity.SessionBody

class UserSessionsFragment : Fragment() {

    private lateinit var binding: FragmentUserSessionsBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var sessionAdapter: SessionAdapter
    private lateinit var _sessionService: SessionService
    private lateinit var _profileService: ProfileService
    private val sessionService get() = _sessionService
    private val profileService get() = _profileService
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserSessionsBinding.inflate(inflater, container, false)
        if (container != null) {
            _sessionService = SessionService(RestTemplate(), container.context)
            _profileService = ProfileService(RestTemplate(), container.context)
        }

        recyclerView = binding.recyclerUserSessions
        recyclerView.layoutManager = LinearLayoutManager(activity)

        lifecycleScope.launch {
            val sessions = sessionService.getSessions()
            val profileInfo = profileService.getProfileInfo(requireArguments().getString("nickname"))
            val userSessionIds = profileInfo!!.played_sessions
            val listOfSessions = mutableListOf<SessionBody>()

            if (userSessionIds != null) {
                if (userSessionIds.isNotEmpty()) {
                    for (session in sessions) {
                        if (userSessionIds.contains(session.sessionId)) {
                            listOfSessions.add(session)
                        }
                    }
                }
            }

            sessionAdapter =
                SessionAdapter(listOfSessions)
            recyclerView.adapter = sessionAdapter
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}