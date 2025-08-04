package win.com.ui.event

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import win.com.R
import win.com.data.database.AppDatabase
import win.com.data.entity.EventEntity
import win.com.data.entity.TeamParticipantEntity
import win.com.data.repository.EventRepository
import win.com.ui.participants.ParticipantAdapter

class ResultDetailsFragment : Fragment() {

    private var eventId: Int = -1
    private lateinit var titleView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ParticipantAdapter
    private lateinit var repository: EventRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventId = arguments?.getInt("event_id") ?: -1
        repository = EventRepository(
            dao = AppDatabase.getDatabase(requireContext()).eventDao(),
            participantDao = AppDatabase.getDatabase(requireContext()).participantDao(),
            resultDao = AppDatabase.getDatabase(requireContext()).liveResultDao()
        )// Инициализируй как нужно у себя
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_result_details, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        titleView = view.findViewById(R.id.eventTitle)
        recyclerView = view.findViewById(R.id.participantsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ParticipantAdapter(mutableListOf()) { participant ->
            // Если нужно, обработай удаление участника
            // Например, repository.deleteParticipant(participant)
        }
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            val event: EventEntity? = repository.getEventByIdNow(eventId)
            repository.getParticipantsByEventId(eventId).observe(viewLifecycleOwner) { participantList ->
                adapter.updateParticipants((participantList ?: emptyList()) as List<TeamParticipantEntity>)
            }
            event?.let {
                titleView.text = "${it.name} | ${it.date}"
            }
        }
    }

    companion object {
        fun newInstance(eventId: Int): ResultDetailsFragment {
            val fragment = ResultDetailsFragment()
            val args = Bundle()
            args.putInt("event_id", eventId)
            fragment.arguments = args
            return fragment
        }
    }
}
