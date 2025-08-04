package win.com.ui.participants

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import win.com.R
import win.com.data.entity.TeamParticipantEntity

class ParticipantAdapter(
    private val participants: MutableList<TeamParticipantEntity>,
    private val onRemove: (TeamParticipantEntity) -> Unit
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

    fun updateParticipants(newParticipants: List<TeamParticipantEntity>) {
        participants.clear()                  // очищаем старый список
        participants.addAll(newParticipants) // добавляем новые элементы
        notifyDataSetChanged()               // говорим адаптеру перерисовать список
    }

    inner class ParticipantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
