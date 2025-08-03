package win.com.ui.event

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import win.com.MainActivity
import win.com.R
import win.com.data.database.AppDatabase
import win.com.data.repository.DataRepository
import win.com.ui.dashboard.DashboardFragment
import win.com.viewmodel.DashboardViewModel
import win.com.viewmodel.DashboardViewModelFactory

class AllEventsFragment : Fragment() {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AllEventsAdapter
    private var participantCounts: Map<Int, Int> = emptyMap()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_all_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val application = requireActivity().application
        val db = AppDatabase.getDatabase(application)
        val dataRepository = DataRepository(db.eventDao(), db.participantDao(), db.teamParticipantDao(), db.teamDao())

        val factory = DashboardViewModelFactory(application, dataRepository)
        viewModel = ViewModelProvider(this, factory)[DashboardViewModel::class.java]

        val backButton = view.findViewById<ImageView>(R.id.backButton)

        // Возврат назад
        backButton.setOnClickListener {
            (activity as? MainActivity)?.openFragment(DashboardFragment())
        }

        viewModel.participantCountsByEvent.observe(viewLifecycleOwner) { map ->
            participantCounts = map
            adapter.updateParticipantCounts(map)
        }

        recyclerView = view.findViewById(R.id.recyclerAllEvents)
        adapter = AllEventsAdapter(
            onViewClick = { event ->
                // Например, открыть ViewEventFragment (если он есть)
                // Или просто вывести лог:
                // Log.d("AllEvents", "View event: ${event.name}")
                (activity as? MainActivity)?.openFragment(ViewEventFragment.newInstance(event.id))
            },
            onDeleteClick = { event ->
                // Показываем диалог
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Event")
                    .setMessage("Are you sure you want to delete \"${event.name}\"?")
                    .setPositiveButton("Yes") { _, _ ->
                        viewModel.deleteEvent(event.id)  // реализуй этот метод в DashboardViewModel
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewModel.allEvents.observe(viewLifecycleOwner) { events ->
            adapter.submitList(events)
        }
    }
}
