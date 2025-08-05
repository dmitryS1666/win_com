package com.cyber90.events.ui.participants

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cyber90.events.R
import com.cyber90.events.data.entity.ParticipantEntity

class ParticipantAdapter(
    private val participants: MutableList<ParticipantEntity>,
    private val onRemove: (ParticipantEntity) -> Unit
) : RecyclerView.Adapter<ParticipantAdapter.ParticipantViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_participant, parent, false)
        return ParticipantViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        val participant = participants[position]
        holder.bind(participant)
    }

    override fun getItemCount(): Int = participants.size

    fun updateParticipants(newParticipants: List<ParticipantEntity>) {
        participants.clear()                  // очищаем старый список
        participants.addAll(newParticipants) // добавляем новые элементы
        notifyDataSetChanged()               // говорим адаптеру перерисовать список
    }

    inner class ParticipantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textName: TextView = itemView.findViewById(R.id.textName)
        private val textTeam: TextView = itemView.findViewById(R.id.textTeam)
        private val textRole: TextView = itemView.findViewById(R.id.textRole)
        private val buttonDelete: Button = itemView.findViewById(R.id.buttonDelete)

        fun bind(participant: ParticipantEntity) {
            textName.text = "Name: ${participant.nickname}"
            textTeam.text = "Team ID: ${participant.team}"
            textRole.text = "Role: ${participant.role}"
            buttonDelete.setOnClickListener {
                onRemove(participant)
            }
        }
    }
}
