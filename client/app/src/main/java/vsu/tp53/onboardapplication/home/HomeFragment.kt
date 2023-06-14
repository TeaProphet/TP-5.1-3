package vsu.tp53.onboardapplication.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.springframework.web.client.RestTemplate
import vsu.tp53.onboardapplication.R
import vsu.tp53.onboardapplication.databinding.FragmentHomeBinding
import vsu.tp53.onboardapplication.model.SessionBody
import vsu.tp53.onboardapplication.service.AuthService
import vsu.tp53.onboardapplication.service.SessionService
import java.time.LocalDateTime

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var sessionAdapter: SessionAdapter
    private lateinit var _sessionService: SessionService
    private lateinit var _authService: AuthService
    private val sessionService get() = _sessionService
    private val authService get() = _authService
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        if (container != null) {
            _sessionService = SessionService(RestTemplate(), container.context)
            _authService = AuthService(RestTemplate(), container.context)
        }

        recyclerView = binding.recyclerSessionsSession
        recyclerView.layoutManager = LinearLayoutManager(activity)

        lifecycleScope.launch {
            val sessions = _sessionService.getSessions()
            sessionAdapter = if (sessions.isNotEmpty()) {
                val filteredSessions = mutableListOf<SessionBody>()
                for (session in sessions){
                    if (session.date_time > LocalDateTime.now()){
                        filteredSessions.add(session)
                    }
                }
                SessionAdapter(filteredSessions)
            } else {
                SessionAdapter(mutableListOf())
            }
//            sessionAdapter =
//                SessionAdapter(_sessionService.getSessions() as MutableList<SessionBody>)
            recyclerView.adapter = sessionAdapter
            if (!(authService.checkIfUserLoggedIn() || authService.checkTokenIsNotExpired())) {
                binding.addButton.isVisible = false
            }
            binding.progressContent.visibility = View.GONE
            binding.pageContent.visibility = View.VISIBLE
        }

        binding.addButton.setOnClickListener {
            it.findNavController().navigate(R.id.createSessionFragment)
        }

        val swipeRefreshLayout = binding.refreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            lifecycleScope.launch {
                val sessions = _sessionService.getSessions()
                sessionAdapter = if (sessions.isNotEmpty()) {
                    SessionAdapter(_sessionService.getSessions() as MutableList<SessionBody>)
                } else {
                    SessionAdapter(mutableListOf())
                }
//            sessionAdapter =
//                SessionAdapter(_sessionService.getSessions() as MutableList<SessionBody>)
                recyclerView.adapter = sessionAdapter
                binding.progressContent.visibility = View.GONE
                binding.pageContent.visibility = View.VISIBLE
                swipeRefreshLayout.isRefreshing = false
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}