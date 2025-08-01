package win.com.ui.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import win.com.MainActivity
import win.com.R
import win.com.ui.dashboard.DashboardViewModel

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

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

        // Назад
        backButton.setOnClickListener {
            (activity as? MainActivity)?.openFragment(AllEventsFragment())
        }

        // Создание события
        createButton.setOnClickListener {
            (activity as? MainActivity)?.openFragment(CreateEventFragment())
            (activity as? MainActivity)?.updateNavIcons("events")
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
