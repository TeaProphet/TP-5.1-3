package vsu.tp53.onboardapplication.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.springframework.web.client.RestTemplate
import vsu.tp53.onboardapplication.R
import vsu.tp53.onboardapplication.databinding.FragmentHomeBinding
import vsu.tp53.onboardapplication.home.service.SessionService
import vsu.tp53.onboardapplication.model.entity.SessionBody
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var sessionAdapter: SessionAdapter
    private lateinit var _sessionService: SessionService
    private val sessionService get() = _sessionService
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        if (container != null) {
            _sessionService = SessionService(RestTemplate(), container.context)
        }

        recyclerView = binding.recyclerSessionsSession
        recyclerView.layoutManager = LinearLayoutManager(activity)
        lifecycleScope.launch {
            sessionAdapter = SessionAdapter(_sessionService.getSessions() as MutableList<SessionBody>)
            recyclerView.adapter = sessionAdapter
       }

        binding.addButton.setOnClickListener {
            it.findNavController().navigate(R.id.createSessionFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
    private fun getDataSession(): List<SessionModel> {
        val listSession: MutableList<SessionModel> = java.util.ArrayList()
        val date: LocalDate = LocalDate.now()
        val cur_time = LocalTime.now()
        val t_formatter = DateTimeFormatter.ofPattern("HH:mm")
        val dateString = date.year.toString()+ "." + date.month.value.toString() + "." + date.dayOfMonth.toString() + " " + cur_time.format(t_formatter)
        Log.i("Sessions", "response")

//        val response = sessionService.getSessions()
//        Log.i("Sessions", response.toString())

        listSession.add(SessionModel(1,"Битвы героев", dateString, "Воронеж", "1/4"))
        listSession.add(SessionModel(2,"DnD: Затерянный город", dateString, "Подольск", "3/4"))
        listSession.add(SessionModel(3,"Компания Затерянный рудник Фандельвера", dateString, "Омск", "2/4"))
        listSession.add(SessionModel(4,"Играем в Бэнг", dateString, "Энск", "3/8"))
        return listSession
    }
}