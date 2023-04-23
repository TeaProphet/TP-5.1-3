package vsu.tp53.onboardapplication.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import vsu.tp53.onboardapplication.R
import vsu.tp53.onboardapplication.databinding.FragmentHomeBinding
import java.time.LocalDate

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var sessionAdapter: SessionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        recyclerView = binding.recyclerSessionsSession
        recyclerView.layoutManager = LinearLayoutManager(activity)

        sessionAdapter = SessionAdapter(getDataSession() as MutableList<SessionModel>)
        recyclerView.adapter = sessionAdapter

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
        val dateString = date.year.toString()+ "." + date.month.value.toString() + "." + date.dayOfMonth.toString()

        listSession.add(SessionModel(1,"Битвы героев", dateString, "Воронеж", "1/4"))
        listSession.add(SessionModel(2,"DnD: Затерянный город", dateString, "Подольск", "3/4"))
        listSession.add(SessionModel(3,"Компания Затерянный рудник Фандельвера", dateString, "Омск", "2/4"))
        listSession.add(SessionModel(4,"Играем в Бэнг", dateString, "Энск", "3/8"))
        return listSession
    }
}