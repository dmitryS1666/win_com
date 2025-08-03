package win.com.ui.team
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import win.com.R
import win.com.data.entity.TeamEntity
import win.com.data.entity.TeamParticipantEntity

class TeamAdapter(
    private var teams: List<TeamEntity>,
    private var participants: List<TeamParticipantEntity>,
    private val onEdit: (TeamEntity) -> Unit,
    private val onDelete: (TeamEntity) -> Unit,
    private val onView: (TeamEntity) -> Unit,
    private var participantsCountMap: Map<Long, Int> = emptyMap()
) : RecyclerView.Adapter<TeamAdapter.TeamViewHolder>() {

    inner class TeamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.textTeamName)
        val size = itemView.findViewById<TextView>(R.id.textTeamSize)
        val editBtn = itemView.findViewById<ImageView>(R.id.buttonEdit)
        val deleteBtn = itemView.findViewById<ImageView>(R.id.buttonDelete)
        val viewBtn = itemView.findViewById<TextView>(R.id.buttonView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_team, parent, false)
        return TeamViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateParticipantsCount(newMap: Map<Long, Int>) {
        participantsCountMap = newMap
        notifyDataSetChanged()
    }

    override fun getItemCount() = teams.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        val team = teams[position]
        holder.name.text = "Team: ${team.name}"

        val count = participantsCountMap[team.id.toLong()] ?: 0
        holder.size.text = count.toString()

        holder.editBtn.setOnClickListener { onEdit(team) }
        holder.deleteBtn.setOnClickListener { onDelete(team) }
        holder.viewBtn.setOnClickListener { onView(team) }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<TeamEntity>) {
        teams = newList
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateParticipants(newParticipants: List<TeamParticipantEntity>) {
        participants = newParticipants
        notifyDataSetChanged()
    }
}
