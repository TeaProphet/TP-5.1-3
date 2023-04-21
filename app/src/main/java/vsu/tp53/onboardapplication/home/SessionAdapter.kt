package vsu.tp53.onboardapplication.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import vsu.tp53.onboardapplication.R

class SessionAdapter (_newEvents: MutableList<SessionModel>) : RecyclerView.Adapter<SessionAdapter.SessionViewHolder>() {

    private var newSessions: MutableList<SessionModel> = _newEvents

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val eventsItems: View = LayoutInflater.from(parent.context).inflate(R.layout.session_item, parent, false)
        return SessionViewHolder(eventsItems)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.sessionName.text = newSessions[position].getName()
        holder.sessionDate.text = newSessions[position].getDate()
        holder.sessionCity.text = newSessions[position].getCity()
        holder.sessionPlayers.text = newSessions[position].getPlayers()

        val bundle = Bundle()

        bundle.putInt("id", newSessions[position].getId())
        bundle.putString("name", newSessions[position].getName())
        bundle.putString("date", newSessions[position].getDate())
        bundle.putString("city", newSessions[position].getCity())
        bundle.putString("players", newSessions[position].getPlayers())

//        holder.itemView.setOnClickListener {
//            it.findNavController().navigate(R.id.action_eventsFragment_to_specificEventFragment, bundle)
//        }
    }

    override fun getItemCount(): Int {
        return newSessions.size
    }


    class SessionViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var sessionName: TextView = itemView.findViewById(R.id.sessionName)
        var sessionDate: TextView = itemView.findViewById(R.id.sessionDate)
        var sessionCity: TextView = itemView.findViewById(R.id.sessionCity)
        var sessionPlayers: TextView = itemView.findViewById(R.id.sessionPlayers)
    }
}