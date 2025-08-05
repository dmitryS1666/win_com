package com.cyber90.events.ui.team

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cyber90.events.R
import com.cyber90.events.data.entity.TeamParticipantEntity

class TeamParticipantAdapter(
    private val participants: MutableList<TeamParticipantEntity>,
    private val onRemove: (TeamParticipantEntity) -> Unit
) : RecyclerView.Adapter<TeamParticipantAdapter.TeamParticipantViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamParticipantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_participant, parent, false)
        return TeamParticipantViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeamParticipantViewHolder, position: Int) {
        val participant = participants[position]
        holder.bind(participant)
    }

    override fun getItemCount(): Int = participants.size

    fun updateTeamParticipants(newParticipants: List<TeamParticipantEntity>) {
        participants.clear()                  // очищаем старый список
        participants.addAll(newParticipants) // добавляем новые элементы
        notifyDataSetChanged()               // говорим адаптеру перерисовать список
    }

    inner class TeamParticipantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textName: TextView = itemView.findViewById(R.id.textName)
        private val textTeam: TextView = itemView.findViewById(R.id.textTeam)
        private val textRole: TextView = itemView.findViewById(R.id.textRole)
        private val buttonDelete: Button = itemView.findViewById(R.id.buttonDelete)

        fun bind(participant: TeamParticipantEntity) {
            textName.text = "Name: ${participant.name}"
            textTeam.text = "Team ID: ${participant.teamId}"
            textRole.text = "Role: ${participant.role}"
            buttonDelete.setOnClickListener {
                onRemove(participant)
            }
        }
    }
}
