package com.cyber90.events.ui.event

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.cyber90.events.MainActivity
import com.cyber90.events.R
import com.cyber90.events.data.database.AppDatabase
import com.cyber90.events.data.repository.DataRepository
import com.cyber90.events.viewmodel.DashboardViewModel
import com.cyber90.events.viewmodel.DashboardViewModelFactory

class ViewEventFragment : Fragment() {

    private lateinit var viewModel: DashboardViewModel
    private var eventId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventId = arguments?.getInt(ARG_EVENT_ID) ?: -1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_view_event, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val application = requireActivity().application
        val db = AppDatabase.getDatabase(application)
        val dataRepository = DataRepository(db.eventDao(), db.participantDao(), db.teamParticipantDao(), db.teamDao())
        val startEventButton = view.findViewById<Button>(R.id.startEventButton)

        // ⚡ Находим бейдж статуса
        val badge = view.findViewById<TextView>(R.id.eventStatusBadge)

        val factory = DashboardViewModelFactory(application, dataRepository)
        viewModel = ViewModelProvider(this, factory)[DashboardViewModel::class.java]

        val title = view.findViewById<TextView>(R.id.viewEventTitle)
        val date = view.findViewById<TextView>(R.id.viewEventDate)
        val time = view.findViewById<TextView>(R.id.viewEventTime)
        val category = view.findViewById<TextView>(R.id.viewEventCategory)
        val mode = view.findViewById<TextView>(R.id.viewEventMode)
        val players = view.findViewById<TextView>(R.id.viewEventPlayers)

        val backButton = view.findViewById<ImageView>(R.id.backButton)
        val createButton = view.findViewById<Button>(R.id.createButton)
        val editButton = view.findViewById<TextView>(R.id.editButton)
        val editIcon = view.findViewById<ImageView>(R.id.editIcon)
        val deleteIcon = view.findViewById<ImageView>(R.id.iconDelete)
        val participantContainer = view.findViewById<LinearLayout>(R.id.participantContainer)

        // Назад
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Редактирование (кнопка или иконка)
        val openEditFragment = {
            (activity as? MainActivity)?.openFragment(EditEventFragment.newInstance(eventId))
        }

        editButton.setOnClickListener { openEditFragment() }
        editIcon.setOnClickListener { openEditFragment() }

        // Удаление события
        deleteIcon.setOnClickListener {
            viewModel.deleteEvent(eventId)
            Toast.makeText(requireContext(), "Event deleted", Toast.LENGTH_SHORT).show()
            (activity as? MainActivity)?.openFragment(AllEventsFragment())
        }

        // Заполнение данных
        viewModel.getEventById(eventId).observe(viewLifecycleOwner) { event ->
            event?.let {
                title.text = event.name
                date.text = event.date
                time.text = event.time
                category.text = event.category
                mode.text = event.mode
                players.text = "${event.maxParticipants} participants"

                if (event.status == "PLANNED") {
                    startEventButton.visibility = View.VISIBLE
                } else {
                    startEventButton.visibility = View.GONE
                    editButton.visibility = View.GONE
                    editIcon.visibility = View.GONE
                }

                if (event.status == "ONGOING") {
                    createButton.text = "Live Control"
                    // добавь openFragment(LiveControlPanelFragment.newInstance(eventId)) в onClick
                }

                // 🏷️ Устанавливаем бейдж по статусу
                when (event.status) {
                    "ONGOING" -> {
                        badge.text = "Ongoing"
                        badge.setBackgroundResource(R.drawable.badge_green)
                        badge.visibility = View.VISIBLE
                    }
                    "COMPLETED" -> {
                        badge.text = "Completed"
                        badge.setBackgroundResource(R.drawable.badge_gray)
                        badge.visibility = View.VISIBLE
                    }
                    else -> badge.visibility = View.GONE
                }

                createButton.setOnClickListener {
                    if (event.status == "ONGOING") {
                        (activity as? MainActivity)?.openFragment(LiveControlPanelFragment.newInstance(eventId))
//                        (activity as? MainActivity)?.updateNavIcons("events")
                    } else {
                        (activity as? MainActivity)?.openFragment(CreateEventFragment())
//                        (activity as? MainActivity)?.updateNavIcons("events")
                    }
                }
            }
        }

        startEventButton.setOnClickListener {
            lifecycleScope.launch {
                val participants = viewModel.getParticipantsByEventIdOnce(eventId)

                if (participants.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "To start the event, you must add participants", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val ongoingEvent = viewModel.getEventByIdOnce(eventId)
                if (ongoingEvent != null) {
                    val updatedEvent = ongoingEvent.copy(status = "ONGOING")
                    viewModel.updateEvent(updatedEvent)
                    Toast.makeText(requireContext(), "Event started", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.getParticipantsByEventId(eventId).observe(viewLifecycleOwner) { participants ->
            participantContainer.removeAllViews()

            if (participants.isNullOrEmpty()) {
                val noParticipants = TextView(requireContext()).apply {
                    text = "No participants yet"
                    setTextColor(resources.getColor(android.R.color.white, null))
                    textSize = 16f
                }
                participantContainer.addView(noParticipants)
            } else {
                for (p in participants) {
                    val row = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            topMargin = 6
                        }
                    }

                    val nicknameView = TextView(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                        text = p.nickname
                        setTextColor(resources.getColor(android.R.color.white, null))
                    }

                    val teamView = TextView(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                        text = p.team ?: "-"
                        setTextColor(resources.getColor(android.R.color.white, null))
                    }

                    val roleView = TextView(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                        text = p.role
                        setTextColor(resources.getColor(android.R.color.white, null))
                    }

                    row.addView(nicknameView)
                    row.addView(teamView)
                    row.addView(roleView)

                    participantContainer.addView(row)
                }
            }
        }
    }

    companion object {
        private const val ARG_EVENT_ID = "event_id"

        fun newInstance(eventId: Int): ViewEventFragment {
            val fragment = ViewEventFragment()
            val args = Bundle()
            args.putInt(ARG_EVENT_ID, eventId)
            fragment.arguments = args
            return fragment
        }
    }
}
