package com.cyber90.events.ui.event

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.cyber90.events.R
import com.cyber90.events.data.entity.ParticipantEntity
import com.cyber90.events.data.entity.ResultEntity

class ParticipantResultAdapter(
    private var participants: List<ParticipantEntity>,
    private var results: List<ResultEntity>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    fun updateData(participants: List<ParticipantEntity>, results: List<ResultEntity>) {
        this.participants = participants
        this.results = results
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = participants.size + 1 // +1 for header

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_HEADER else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_table_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_participant_result, parent, false)
            ResultViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ResultViewHolder && position > 0) {
            val participant = participants[position - 1]
            val result = results.firstOrNull { it.participantId == participant.id.toLong() }

            holder.bind(participant, result)

            val isLast = position == itemCount - 1

            // 🎨 Устанавливаем фон для строки: тёмный или скруглённый
            holder.itemView.setBackgroundResource(
                if (isLast) R.drawable.table_footer_background
                else R.color.dark_row_background // цвет #1E2A48
            )
        }
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerName: TextView = itemView.findViewById(R.id.playerName)
        private val playerRole: TextView = itemView.findViewById(R.id.playerRole)
        private val lapTime: TextView = itemView.findViewById(R.id.lapTime)
        private val finalPosition: TextView = itemView.findViewById(R.id.finalPosition)


        @OptIn(UnstableApi::class)
        fun bind(participant: ParticipantEntity, result: ResultEntity?) {
            Log.d("DEBUG_RESULTS", "Participants: $participant")

            playerName.text = participant.nickname
            playerRole.text = participant.role
            lapTime.text = participant.lapTime ?: "--:--"
            finalPosition.text = participant.pos ?: "--"
        }
    }
}

