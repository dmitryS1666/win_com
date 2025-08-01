package win.com.ui.event

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import win.com.R
import win.com.data.entity.EventEntity
import win.com.ui.dashboard.DashboardViewModel
import win.com.util.GameCategories
import win.com.util.GameModes

class EditEventFragment : Fragment() {

    private lateinit var viewModel: DashboardViewModel
    private var eventId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventId = arguments?.getInt(ARG_EVENT_ID) ?: -1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_edit_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        val nameInput = view.findViewById<EditText>(R.id.editEventName)
        val dateInput = view.findViewById<EditText>(R.id.editEventDate)
        val timeInput = view.findViewById<EditText>(R.id.editEventTime)
        val categorySpinner = view.findViewById<Spinner>(R.id.editEventCategory)
        val modeSpinner = view.findViewById<Spinner>(R.id.editEventMode)
        val roundsInput = view.findViewById<EditText>(R.id.editEventRounds)
        val maxParticipantsInput = view.findViewById<EditText>(R.id.editEventPlayers)
        val saveButton = view.findViewById<Button>(R.id.saveButton)
        val backButton = view.findViewById<ImageView>(R.id.backButton)

        // Устанавливаем адаптеры для спиннеров с твоими списками
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, GameCategories.list)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter

        val modeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, GameModes.list)
        modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        modeSpinner.adapter = modeAdapter

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        if (eventId != -1) {
            viewModel.getEventById(eventId).observe(viewLifecycleOwner) { event ->
                nameInput.setText(event.name)
                dateInput.setText(event.date)
                timeInput.setText(event.time)
                roundsInput.setText(event.rounds.toString())
                maxParticipantsInput.setText(event.maxParticipants.toString())

                // Здесь уже безопасно выставлять выбранные позиции
                categorySpinner.setSelection(getSpinnerIndex(categorySpinner, event.category))
                modeSpinner.setSelection(getSpinnerIndex(modeSpinner, event.mode))
            }
        }

        saveButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val date = dateInput.text.toString().trim()
            val time = timeInput.text.toString().trim()
            val category = categorySpinner.selectedItem.toString()
            val rounds = roundsInput.text.toString().toIntOrNull()
            val mode = modeSpinner.selectedItem.toString()
            val maxParticipants = maxParticipantsInput.text.toString().toIntOrNull()

            if (name.isBlank() || date.isBlank() || time.isBlank() || maxParticipants == null || rounds == null) {
                Toast.makeText(requireContext(), "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedEvent = EventEntity(
                id = eventId,
                name = name,
                date = date,
                time = time,
                category = category,
                mode = mode,
                rounds = rounds,
                maxParticipants = maxParticipants,
                isPrivate = false
            )

            viewModel.updateEvent(updatedEvent)
            parentFragmentManager.popBackStack()
        }
    }

    private fun getSpinnerIndex(spinner: Spinner, value: String): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString().equals(value, ignoreCase = true)) {
                return i
            }
        }
        return 0
    }

    companion object {
        private const val ARG_EVENT_ID = "event_id"

        fun newInstance(eventId: Int): EditEventFragment {
            val fragment = EditEventFragment()
            val args = Bundle()
            args.putInt(ARG_EVENT_ID, eventId)
            fragment.arguments = args
            return fragment
        }
    }
}
