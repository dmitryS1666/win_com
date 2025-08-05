package com.cyber90.events.ui.team

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.cyber90.events.MainActivity
import com.cyber90.events.R
import com.cyber90.events.data.entity.TeamEntity
import com.cyber90.events.data.entity.TeamParticipantEntity
import com.cyber90.events.viewmodel.TeamViewModel

class EditTeamFragment : Fragment() {

    private var teamId: Int = -1
    private lateinit var viewModel: TeamViewModel

    // Список участников в UI, как в CreateTeamFragment
    private val participants = mutableListOf<TeamParticipantUiModel>()

    private lateinit var participantContainer: LinearLayout
    private lateinit var editPlayerName: EditText
    private lateinit var roleSpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var nameInput: EditText
    private lateinit var backButton: ImageView
    private lateinit var addParticipantButton: Button

    companion object {
        private const val ARG_TEAM_ID = "team_id"

        fun newInstance(teamId: Int): EditTeamFragment {
            val fragment = EditTeamFragment()
            val args = Bundle()
            args.putInt(ARG_TEAM_ID, teamId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        teamId = arguments?.getInt(ARG_TEAM_ID) ?: -1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_edit_team, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[TeamViewModel::class.java]

        nameInput = view.findViewById(R.id.editTeamName)
        saveButton = view.findViewById(R.id.saveButton)
        backButton = view.findViewById(R.id.backButton)
        participantContainer = view.findViewById(R.id.participantList)
        editPlayerName = view.findViewById(R.id.editPlayerName)
        roleSpinner = view.findViewById(R.id.spinnerRole)
        addParticipantButton = view.findViewById(R.id.buttonAddParticipant)

        backButton.setOnClickListener {
            (activity as? MainActivity)?.openFragment(TeamsManagerFragment())
        }

        setupRoleSpinner()

        viewModel.getTeamById(teamId).observe(viewLifecycleOwner) { team ->
            team?.let {
                nameInput.setText(it.name)
                loadParticipants(it.id)
            }
        }

        addParticipantButton.setOnClickListener {
            addParticipant()
        }

        saveButton.setOnClickListener {
            saveTeam()
        }
    }

    private fun setupRoleSpinner() {
        val roles = listOf("PILOT", "HOST", "ENGINEER")
        roleSpinner.adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_white, roles)
    }

    private fun loadParticipants(teamId: Int) {
        // Очистим текущие данные
        participants.clear()
        participantContainer.removeAllViews()

        viewModel.getParticipants(teamId).observe(viewLifecycleOwner) { participantsList ->
            participantsList.forEach {
                participants.add(TeamParticipantUiModel(it.name, it.role))
            }
            refreshParticipantsUI()
        }
    }

    private fun refreshParticipantsUI() {
        participantContainer.removeAllViews()

        participants.forEach { participant ->
            val participantLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(16, 12, 16, 12)
                setBackgroundColor(Color.parseColor("#1A2239"))
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 10)
                layoutParams = params
                background = resources.getDrawable(R.drawable.rounded_bg, null)
                gravity = Gravity.CENTER_VERTICAL
            }

            val participantText = TextView(requireContext()).apply {
                text = "Player: ${participant.playerName}\nRole: ${participant.role}"
                setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setPadding(15, 0, 0, 0)
                textSize = 16f
            }

            val deleteButton = ImageButton(requireContext()).apply {
                setImageResource(R.drawable.ic_delete)
                background = null
                setColorFilter(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setOnClickListener {
                    participants.remove(participant)
                    refreshParticipantsUI()
                }
            }

            participantLayout.addView(participantText)
            participantLayout.addView(deleteButton)

            participantContainer.addView(participantLayout)
        }
    }

    private fun addParticipant() {
        val player = editPlayerName.text.toString().trim()
        val role = roleSpinner.selectedItem.toString()

        if (player.isBlank()) {
            Toast.makeText(requireContext(), "Player name can't be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val participant = TeamParticipantUiModel(playerName = player, role = role)
        participants.add(participant)
        refreshParticipantsUI()

        // Очистить поле ввода
        editPlayerName.setText("")
    }

    @OptIn(UnstableApi::class)
    private fun saveTeam() {
        val teamName = nameInput.text.toString().trim()

        if (teamName.isBlank()) {
            Toast.makeText(requireContext(), "Team name can't be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedTeam = TeamEntity(id = teamId, name = teamName)

        Toast.makeText(requireContext(), "Team id: " + updatedTeam.id.toString() + "/teamId: " + teamId.toString(), Toast.LENGTH_SHORT).show()

        val participantsToSave = participants.map {
            TeamParticipantEntity(teamId = teamId.toLong(), name = it.playerName, role = it.role)
        }

        Log.d("SAVE", "Saving team: $updatedTeam")
        Log.d("SAVE", "Participants: $participantsToSave")

        // Обновляем команду и участников в базе
        viewModel.saveTeamAndParticipants(updatedTeam, participantsToSave)

        (activity as? MainActivity)?.openFragment(TeamsManagerFragment())
    }

    data class TeamParticipantUiModel(
        val playerName: String,
        val role: String
    )
}
