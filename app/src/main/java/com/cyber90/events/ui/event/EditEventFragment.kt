package com.cyber90.events.ui.event

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.cyber90.events.MainActivity
import com.cyber90.events.R
import com.cyber90.events.data.database.AppDatabase
import com.cyber90.events.data.entity.EventEntity
import com.cyber90.events.data.entity.ParticipantEntity
import com.cyber90.events.data.entity.TeamParticipantEntity
import com.cyber90.events.data.repository.DataRepository
import com.cyber90.events.viewmodel.DashboardViewModel
import com.cyber90.events.viewmodel.TeamViewModel
import com.cyber90.events.util.GameCategories
import com.cyber90.events.util.GameModes
import com.cyber90.events.viewmodel.DashboardViewModelFactory

class EditEventFragment : Fragment() {

    private lateinit var viewModel: DashboardViewModel
    private var eventId: Int = -1
    private lateinit var editPlayerName: EditText
    private lateinit var spinnerRole: Spinner
    private lateinit var spinnerTeam: Spinner
    private lateinit var participantList: LinearLayout
    private val addedParticipants = mutableListOf<ParticipantEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventId = arguments?.getInt(ARG_EVENT_ID) ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_edit_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val application = requireActivity().application
        val db = AppDatabase.getDatabase(application)
        val dataRepository = DataRepository(db.eventDao(), db.participantDao(), db.teamParticipantDao(), db.teamDao())

        val factory = DashboardViewModelFactory(application, dataRepository)
        viewModel = ViewModelProvider(this, factory)[DashboardViewModel::class.java]

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
        val categoryAdapter =
            ArrayAdapter(requireContext(), R.layout.spinner_item_white, GameCategories.list)
        categoryAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_white)
        categorySpinner.adapter = categoryAdapter

        val modeAdapter =
            ArrayAdapter(requireContext(), R.layout.spinner_item_white, GameModes.list)
        modeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_white)
        modeSpinner.adapter = modeAdapter

        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        viewModel.getParticipantsByEventId(eventId).observe(viewLifecycleOwner) { list ->
            list.forEach { participant ->
                addedParticipants.add(participant)
                addParticipantCard(participant)
            }
        }

        editPlayerName = view.findViewById(R.id.editPlayerName)
        spinnerRole = view.findViewById(R.id.spinnerRole)
        spinnerTeam = view.findViewById(R.id.spinnerTeam)
        participantList = view.findViewById(R.id.participantList)

        setupSpinners()

        view.findViewById<Button>(R.id.buttonAddParticipant).setOnClickListener {
            addParticipantUI()
        }

        if (eventId != -1) {
            viewModel.getEventById(eventId).observe(viewLifecycleOwner) { event ->
                if (event.status != "PLANNED") {
                    Toast.makeText(requireContext(), "Can't edit ongoing/completed events", Toast.LENGTH_SHORT).show()
                    (activity as? MainActivity)?.openFragment(ViewEventFragment.newInstance(eventId))
                    return@observe
                }

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

            // Запускаем корутин для операций с БД
            lifecycleScope.launch {
                viewModel.updateEvent(updatedEvent)
                viewModel.deleteParticipantsByEventId(updatedEvent.id)

                val teamViewModel = ViewModelProvider(requireActivity())[TeamViewModel::class.java]

                for (participant in addedParticipants) {
                    participant.eventId = updatedEvent.id
                    viewModel.insertParticipant(participant)

                    participant.team?.let { teamName ->
                        val team = teamViewModel.getTeamByName(teamName)
                        if (team != null) {
                            val teamParticipant = TeamParticipantEntity(
                                teamId = team.id.toLong(),
                                name = participant.nickname,
                                role = participant.role
                            )
                            teamViewModel.insertTeamParticipant(teamParticipant)
                        }
                    }
                }

                (activity as? MainActivity)?.openFragment(AllEventsFragment())
            }
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

    private fun setupSpinners() {
        val roles = listOf("HOST", "PILOT", "ENGINEER")
        spinnerRole.adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_white, roles)

        // Загрузить команды из ViewModel
        viewModel.teams.observe(viewLifecycleOwner) { teamList ->
            val teamNames = teamList.map { it.name }
            spinnerTeam.adapter =
                ArrayAdapter(requireContext(), R.layout.spinner_item_white, teamNames)
        }
    }

    private fun addParticipantUI() {
        val name = editPlayerName.text.toString().trim()
        val role = spinnerRole.selectedItem.toString()
        val team = spinnerTeam.selectedItem?.toString()

        if (name.isBlank()) {
            Toast.makeText(requireContext(), "Enter name", Toast.LENGTH_SHORT).show()
            return
        }

        val participant = ParticipantEntity(
            eventId = 0, // проставишь позже при сохранении
            nickname = name,
            team = team,
            role = role
        )

        addedParticipants.add(participant)

        // UI блок участника
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(12, 12, 12, 12)
            background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_bg)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 10, 0, 0)
            layoutParams = params
        }

        val textView = TextView(requireContext()).apply {
            text = "$name - $role (${team ?: "No Team"})"
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val deleteButton = ImageButton(requireContext()).apply {
            setImageResource(R.drawable.ic_delete)
            background = null
            setColorFilter(Color.WHITE)
            setOnClickListener {
                participantList.removeView(container)
                addedParticipants.remove(participant)
            }
        }

        container.addView(textView)
        container.addView(deleteButton)
        participantList.addView(container)

        // очистить поля
        editPlayerName.setText("")
    }

    private fun addParticipantCard(participant: ParticipantEntity) {
        val card = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 12)
            background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_bg)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 10, 0, 0)
            layoutParams = params
        }

        // Горизонтальный блок с именем и кнопкой удаления
        val topRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER_VERTICAL
        }

        val nameView = TextView(requireContext()).apply {
            text = "Player: ${participant.nickname}"
            setTextColor(Color.WHITE)
            textSize = 13f
            setPadding(15, 0, 0, 0)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val deleteButton = ImageButton(requireContext()).apply {
            setImageResource(R.drawable.ic_delete)
            background = null
            setColorFilter(Color.WHITE)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 24 // опустить кнопку вниз на 24px — подбери нужное значение
            }

            setOnClickListener {
                participantList.removeView(card)
                addedParticipants.remove(participant)
            }
        }

        topRow.addView(nameView)
        topRow.addView(deleteButton)

        val teamView = TextView(requireContext()).apply {
            text = "Team: ${participant.team ?: "No Team"}"
            setTextColor(Color.LTGRAY)
            textSize = 13f
            setPadding(15, 0, 0, 0)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = -5 // поднимаем на 5px вверх
            }
        }

        val roleView = TextView(requireContext()).apply {
            text = "Role: ${participant.role}"
            setTextColor(Color.LTGRAY)
            textSize = 13f
            setPadding(15, 5, 0, 15)
        }

            // Собираем карточку
        card.addView(topRow)
        card.addView(teamView)
        card.addView(roleView)
        participantList.addView(card)
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
