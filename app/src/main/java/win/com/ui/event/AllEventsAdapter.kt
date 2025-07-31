package win.com.ui.event

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import win.com.R
import win.com.data.entity.EventEntity

class AllEventsAdapter : ListAdapter<EventEntity, AllEventsAdapter.EventViewHolder>(
    object : DiffUtil.ItemCallback<EventEntity>() {
        override fun areItemsTheSame(oldItem: EventEntity, newItem: EventEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: EventEntity, newItem: EventEntity) = oldItem == newItem
    }
) {
    inner class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.itemTitle)
        val date: TextView = view.findViewById(R.id.itemDate)
        val mode: TextView = view.findViewById(R.id.itemMode)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(v)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        holder.title.text = event.name
        holder.date.text = "📅 ${event.date} ${event.time}"
        holder.mode.text = "${event.category} • ${event.mode}"
    }
}
