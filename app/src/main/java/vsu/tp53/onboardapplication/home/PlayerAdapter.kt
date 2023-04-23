package vsu.tp53.onboardapplication.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vsu.tp53.onboardapplication.R

class PlayerAdapter (_newPlayers: MutableList<PlayerModel>) : RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>() {

    private var newPlayers: MutableList<PlayerModel> = _newPlayers

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val playersItems: View = LayoutInflater.from(parent.context).inflate(R.layout.player_item, parent, false)
        return PlayerViewHolder(playersItems)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.playerName.text = newPlayers[position].getName()
        holder.playerReputation.text = newPlayers[position].getReputation().toString()

//        val bundle = Bundle()
//
//        bundle.putInt("player_reputation", newPlayers[position].getReputation())
//        bundle.putString("player_name", newPlayers[position].getName())
//        holder.itemView.setOnClickListener {
//            it.findNavController().navigate(R.id.sessionFragment, bundle)
//        }
    }

    override fun getItemCount(): Int {
        return newPlayers.size
    }


    class PlayerViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var playerName: TextView = itemView.findViewById(R.id.playerName)
        var playerReputation: TextView = itemView.findViewById(R.id.playerReputation)
    }
}