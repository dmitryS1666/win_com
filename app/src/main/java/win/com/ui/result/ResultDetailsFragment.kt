package win.com.ui.event

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import win.com.MainActivity
import win.com.R
import win.com.data.database.AppDatabase
import win.com.data.repository.EventRepository
import win.com.ui.participants.ParticipantAdapter
import win.com.ui.result.ResultsFragment

class ResultDetailsFragment : Fragment() {

    private var eventId: Int = -1
    private lateinit var titleView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ParticipantAdapter
    private lateinit var resultAdapter: ParticipantResultAdapter
    private lateinit var repository: EventRepository
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventId = arguments?.getInt("event_id") ?: -1
        repository = EventRepository(
            dao = AppDatabase.getDatabase(requireContext()).eventDao(),
            participantDao = AppDatabase.getDatabase(requireContext()).participantDao(),
            teamParticipantDao = AppDatabase.getDatabase(requireContext()).teamParticipantDao(),
            resultDao = AppDatabase.getDatabase(requireContext()).liveResultDao()
        )// Инициализируй как нужно у себя
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_result_details, container, false)

    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        titleView = view.findViewById(R.id.eventTitle)
        recyclerView = view.findViewById(R.id.participantsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        backButton = view.findViewById(R.id.backButton)

        backButton.setOnClickListener {
            (activity as? MainActivity)?.openFragment(ResultsFragment())
        }

        resultAdapter = ParticipantResultAdapter(emptyList(), emptyList())
        recyclerView.adapter = resultAdapter

        lifecycleScope.launch {
            val event = repository.getEventByIdNow(eventId)

            repository.getParticipantsByEventId(eventId).observe(viewLifecycleOwner) { participants ->
                repository.getResultsByEventId(eventId).observe(viewLifecycleOwner) { results ->
                    resultAdapter.updateData(participants ?: emptyList(),
                        (results ?: emptyList())
                    )
                }
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
