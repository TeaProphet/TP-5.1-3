package vsu.tp53.onboardapplication.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import vsu.tp53.onboardapplication.R
import vsu.tp53.onboardapplication.model.SessionBody

class SessionAdapter (_newSessions: MutableList<SessionBody>) : RecyclerView.Adapter<SessionAdapter.SessionViewHolder>() {

    private var newSessions: MutableList<SessionBody> = _newSessions

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val sessionsItems: View = LayoutInflater.from(parent.context).inflate(R.layout.session_item, parent, false)
        return SessionViewHolder(sessionsItems)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.sessionID.text = newSessions[position].sessionId.toString()
        holder.sessionName.text = newSessions[position].name
        holder.sessionDate.text = newSessions[position].date_time.toString()
        holder.sessionCity.text = newSessions[position].city_address
        holder.sessionPlayers.text = newSessions[position].players_max.toString()
        holder.sessionPlayers.text = newSessions[position].players_max.toString()

        val bundle = Bundle()

        bundle.putInt("id", newSessions[position].sessionId)
        holder.itemView.setOnClickListener {
            it.findNavController().navigate(R.id.sessionFragment, bundle)
        }
    }

    override fun getItemCount(): Int {
        return newSessions.size
    }


    class SessionViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var sessionID: TextView = itemView.findViewById(R.id.sessionID)
        var sessionName: TextView = itemView.findViewById(R.id.sessionName)
        var sessionDate: TextView = itemView.findViewById(R.id.sessionDateAndTime)
        var sessionCity: TextView = itemView.findViewById(R.id.sessionCity)
        var sessionPlayers: TextView = itemView.findViewById(R.id.sessionPlayers)
    }
}