package vsu.tp53.onboardapplication.home

import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import vsu.tp53.onboardapplication.R
import vsu.tp53.onboardapplication.databinding.FragmentSessionBinding
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class SessionFragment : Fragment() {

    private lateinit var binding: FragmentSessionBinding
    private lateinit var playersRecyclerView: RecyclerView
    private lateinit var playersAdapter: PlayerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSessionBinding.inflate(inflater, container, false)
        println(requireArguments().getString("id"))
        binding.inSessionID.setText(requireArguments().getString("id"))
        binding.inSessionName.setText(requireArguments().getString("session_name"))
        val cur_time = LocalTime.now()
        val t_formatter = DateTimeFormatter.ofPattern("HH:mm")
        binding.inSessionDate.setText(requireArguments().getString("date") + " " + cur_time.format(t_formatter))
        binding.inSessionCity.setText(requireArguments().getString("city"))
        binding.inSessionGames.setText("DnD")
        binding.inSessionPlayers.setText(requireArguments().getString("players"))

        playersRecyclerView = binding.recyclerPlayersPlayer
        playersRecyclerView.layoutManager = LinearLayoutManager(activity)

        playersAdapter = PlayerAdapter(getDataPlayer() as MutableList<PlayerModel>)
        playersRecyclerView.adapter = playersAdapter

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

    private fun getDataPlayer(): List<PlayerModel> {
        val listPlayers: MutableList<PlayerModel> = java.util.ArrayList()

        listPlayers.add(PlayerModel("Владислав", 1))
        listPlayers.add(PlayerModel("Юлия", 1))
        listPlayers.add(PlayerModel("Екатерина", 1))
        return listPlayers
    }
}