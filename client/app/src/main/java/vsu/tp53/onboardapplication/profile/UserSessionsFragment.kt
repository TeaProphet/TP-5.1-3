package vsu.tp53.onboardapplication.profile

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import vsu.tp53.onboardapplication.databinding.FragmentUserSessionsBinding
import vsu.tp53.onboardapplication.home.SessionAdapter
import vsu.tp53.onboardapplication.home.SessionModel
import java.time.LocalDate

/**
 * A simple [Fragment] subclass.
 * Use the [UserSessionsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserSessionsFragment : Fragment() {

    private lateinit var binding: FragmentUserSessionsBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var sessionAdapter: SessionAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentUserSessionsBinding.inflate(inflater, container, false)

        recyclerView = binding.recyclerUserSessions
        recyclerView.layoutManager = LinearLayoutManager(activity)

        sessionAdapter = SessionAdapter(getDataSession() as MutableList<SessionModel>)
        recyclerView.adapter = sessionAdapter

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDataSession(): List<SessionModel> {
        val listSession: MutableList<SessionModel> = java.util.ArrayList()
        val date: LocalDate = LocalDate.now()
        val dateString = date.year.toString()+ " " + date.month.value.toString() + " " + date.dayOfMonth.toString()

        listSession.add(SessionModel(1,"Битвы героев", dateString, "Воронеж", "1/4"))
        listSession.add(SessionModel(2,"DnD: Затерянный город", dateString, "Подольск", "3/4"))

        return listSession
    }
}