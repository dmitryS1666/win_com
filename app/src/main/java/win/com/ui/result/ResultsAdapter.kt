package win.com.ui.result

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import win.com.R
import win.com.data.entity.EventEntity
import win.com.data.entity.TeamParticipantEntity

class ResultsAdapter(
    private val participants: MutableList<TeamParticipantEntity>,
    private val onClick: (EventEntity) -> Unit
) : RecyclerView.Adapter<ResultsAdapter.ResultViewHolder>() {

    private var events: List<EventEntity> = emptyList()

    fun submitList(list: List<EventEntity>) {
        events = list
        notifyDataSetChanged()
    }

    fun updateParticipants(newParticipants: List<TeamParticipantEntity>) {
        participants.clear()
        participants.addAll(newParticipants)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event_result, parent, false)
        return ResultViewHolder(view)
    }

    override fun getItemCount(): Int = events.size

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val event = events[position]
        holder.bind(event)
    }

    inner class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameView = itemView.findViewById<TextView>(R.id.eventName)
        private val dateView = itemView.findViewById<TextView>(R.id.eventDate)
        private val participantsView = itemView.findViewById<TextView>(R.id.participantsCount)

        fun bind(event: EventEntity) {
            nameView.text = event.name
            dateView.text = event.date // форматируй если нужно
            // TODO: можно добавить количество участников (если есть)

            itemView.setOnClickListener { onClick(event) }
        }
    }
}
