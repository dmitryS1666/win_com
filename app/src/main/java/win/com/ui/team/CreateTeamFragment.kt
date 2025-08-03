package win.com.ui.team

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import win.com.MainActivity
import win.com.R
import win.com.data.entity.TeamEntity
import win.com.data.entity.TeamParticipantEntity
import win.com.viewmodel.TeamViewModel

class CreateTeamFragment : Fragment() {

    private lateinit var viewModel: TeamViewModel
    private lateinit var roleSpinner: Spinner
    private lateinit var editPlayerName: EditText
    private lateinit var participantContainer: LinearLayout
    private lateinit var teamNameInput: EditText
    private lateinit var saveButton: Button
    private lateinit var backButton: ImageView

    private val participants = mutableListOf<TeamParticipantUiModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_create_team, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(requireActivity())[TeamViewModel::class.java]

        teamNameInput = view.findViewById(R.id.editTeamName)
        roleSpinner = view.findViewById(R.id.spinnerRole)
        editPlayerName = view.findViewById(R.id.editPlayerName)
        participantContainer = view.findViewById(R.id.participantList)
        saveButton = view.findViewById(R.id.buttonCreateTeam)
        backButton = view.findViewById(R.id.backButton)

        backButton.setOnClickListener {
            (activity as? MainActivity)?.openFragment(TeamsManagerFragment())
        }

        setupSpinners()

        view.findViewById<Button>(R.id.buttonAddParticipant).setOnClickListener {
            addParticipant()
        }

        saveButton.setOnClickListener {
            saveTeam()
        }
    }

    private fun setupSpinners() {
        val roles = listOf("PILOT", "HOST", "ENGINEER")

        roleSpinner.adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_white, roles)
    }

    private fun addParticipant() {
        val role = roleSpinner.selectedItem.toString()
        val player = editPlayerName.text.toString().trim()

        val participant = TeamParticipantUiModel(playerName = player, role = role)
        participants.add(participant)

        // Контейнер для участника: горизонтальный LinearLayout с фоном и скруглениями
        val participantLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#1A2239"))
            setPadding(16, 12, 16, 12)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 10, 0, 25)
            layoutParams = params
            background = resources.getDrawable(R.drawable.rounded_bg, null) // если есть drawable с закруглениями
            gravity = Gravity.CENTER_VERTICAL
        }

        // Текст с инфо по участнику
        val participantText = TextView(requireContext()).apply {
            text = "Player: $player\n\nRole: $role"
            setTextColor(Color.WHITE)
            setPadding(25, 15, 0, 15)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        // Кнопка удаления — можно использовать ImageButton с иконкой "крестик"
        val deleteButton = ImageButton(requireContext()).apply {
            setImageResource(R.drawable.ic_delete) // иконка "удалить" (крестик)
            background = null // убрать фон по умолчанию кнопки
            setColorFilter(Color.WHITE) // белый цвет иконки
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                // Удаляем участника из списка и из UI
                participants.remove(participant)
                participantContainer.removeView(participantLayout)
            }
        }

        participantLayout.addView(participantText)
        participantLayout.addView(deleteButton)

        participantContainer.addView(participantLayout)
    }

    private fun saveTeam() {
        val teamName = teamNameInput.text.toString().trim()

        if (teamName.isBlank()) {
            Toast.makeText(requireContext(), "Enter team name", Toast.LENGTH_SHORT).show()
            return
        }

        val team = TeamEntity(name = teamName)
        val participantsToSave = participants.map {
            TeamParticipantEntity(teamId = 0L, name = it.playerName, role = it.role)
        }

        viewModel.insertWithParticipants(team, participantsToSave)

        Toast.makeText(requireContext(), "Team created!", Toast.LENGTH_SHORT).show()

        (activity as? MainActivity)?.openFragment(TeamsManagerFragment())
    }
}
