package com.cyber90.events.ui.event

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyber90.events.MainActivity
import com.cyber90.events.R
import com.cyber90.events.data.database.AppDatabase
import com.cyber90.events.data.repository.DataRepository
import com.cyber90.events.viewmodel.DashboardViewModel
import com.cyber90.events.viewmodel.DashboardViewModelFactory

class AllEventsFragment : Fragment() {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AllEventsAdapter
    private var participantCounts: Map<Int, Int> = emptyMap()
    private lateinit var emptyView: TextView

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
        emptyView = view.findViewById(R.id.emptyView)

        val factory = DashboardViewModelFactory(application, dataRepository)
        viewModel = ViewModelProvider(this, factory)[DashboardViewModel::class.java]

        val backButton = view.findViewById<ImageView>(R.id.backButton)

        // Возврат назад
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
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

            if (events.isNullOrEmpty()) {
                emptyView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }
    }
}
