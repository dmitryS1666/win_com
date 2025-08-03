package win.com.ui.event

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import win.com.R
import win.com.data.entity.EventEntity

class AllEventsAdapter(
    private val onViewClick: (EventEntity) -> Unit,
    private val onDeleteClick: (EventEntity) -> Unit,
    private var participantCounts: Map<Int, Int> = emptyMap()
) : ListAdapter<EventEntity, AllEventsAdapter.EventViewHolder>(
    object : DiffUtil.ItemCallback<EventEntity>() {
        override fun areItemsTheSame(oldItem: EventEntity, newItem: EventEntity) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: EventEntity, newItem: EventEntity) =
            oldItem == newItem
    }
) {
    inner class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.itemTitle)
        val date: TextView = view.findViewById(R.id.itemDate)
        val time: TextView = view.findViewById(R.id.itemTime)
        val category: TextView = view.findViewById(R.id.itemCategory)
        val mode: TextView = view.findViewById(R.id.itemMode)
        val players: TextView = view.findViewById(R.id.itemPlayers)
        val iconView: View = view.findViewById(R.id.iconView)
        val iconDelete: View = view.findViewById(R.id.iconDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(v)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        holder.title.text = event.name
        holder.date.text = event.date
        holder.time.text = event.time
        holder.category.text = event.category
        holder.mode.text = event.mode

        val currentCount = participantCounts[event.id] ?: 0
        holder.players.text = "$currentCount / ${event.maxParticipants}"

        holder.iconView.setOnClickListener {
            onViewClick(event)
        }

        holder.iconDelete.setOnClickListener {
            onDeleteClick(event)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateParticipantCounts(newMap: Map<Int, Int>) {
        participantCounts = newMap
        notifyDataSetChanged()
    }
}
