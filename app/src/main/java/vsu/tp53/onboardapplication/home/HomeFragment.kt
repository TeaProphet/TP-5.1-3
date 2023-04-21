package vsu.tp53.onboardapplication.home

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        recyclerView = binding.recyclerSessionsSession
        recyclerView.layoutManager = LinearLayoutManager(activity)

        sessionAdapter = SessionAdapter(getDataSession() as MutableList<SessionModel>)
        recyclerView.adapter = sessionAdapter

        binding.addButton.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.add_button)
                .setAction("Action", null).show()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDataSession(): List<SessionModel> {
        val listSession: MutableList<SessionModel> = java.util.ArrayList()
        val date: LocalDate = LocalDate.now()
        val dateString = date.year.toString()+ " " + date.month.value.toString() + " " + date.dayOfMonth.toString()

        listSession.add(SessionModel(1,"Сессия 1", dateString, "Воронеж", "1/4"))
        listSession.add(SessionModel(2,"Сессия 2", dateString, "Подольск", "10000/100000"))

        return listSession
    }
}