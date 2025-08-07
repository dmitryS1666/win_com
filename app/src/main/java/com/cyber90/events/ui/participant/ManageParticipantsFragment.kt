package com.cyber90.events.ui.teams

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyber90.events.MainActivity
import com.cyber90.events.R
import com.cyber90.events.data.entity.TeamEntity
import com.cyber90.events.data.entity.TeamParticipantEntity
import com.cyber90.events.ui.event.AllEventsFragment
import com.cyber90.events.ui.team.TeamParticipantAdapter
import com.cyber90.events.viewmodel.TeamViewModel

class ManageParticipantsFragment : Fragment() {

    private lateinit var viewModel: TeamViewModel
    private lateinit var adapter: TeamParticipantAdapter

    private lateinit var backButton: ImageView
    private lateinit var etName: EditText
    private lateinit var spinnerRole: Spinner
    private lateinit var spinnerTeam: Spinner
    private lateinit var btnAdd: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnStartEvent: Button

    private val participants = mutableListOf<TeamParticipantEntity>()
    private var selectedTeam: TeamEntity? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_manage_participants, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[TeamViewModel::class.java]

        // Получаем вьюшки
        backButton = view.findViewById(R.id.backButton)
        etName = view.findViewById(R.id.etName)
        spinnerRole = view.findViewById(R.id.spinnerRole)
        spinnerTeam = view.findViewById(R.id.spinnerTeam)
        btnAdd = view.findViewById(R.id.btnAdd)
        recyclerView = view.findViewById(R.id.recyclerView)
        btnStartEvent = view.findViewById(R.id.btnStartEvent)

        setupUI()
        observeTeams()
    }

    private fun setupUI() {
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        val roles = listOf("PILOT", "NAVIGATOR", "ENGINEER")
        spinnerRole.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            roles
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        adapter = TeamParticipantAdapter(participants) { participant ->
            participants.remove(participant)
            adapter.notifyDataSetChanged()
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        btnAdd.setOnClickListener {
            val name = etName.text.toString().trim()
            val role = spinnerRole.selectedItem.toString()
            val team = selectedTeam ?: return@setOnClickListener

            if (name.isNotEmpty()) {
                val participant = TeamParticipantEntity(
                    teamId = team.id.toLong(),
                    name = name,
                    role = role
                )
                participants.add(participant)
                adapter.notifyDataSetChanged()
                etName.text.clear()
            }
        }

        btnStartEvent.setOnClickListener {
            // TODO: реализовать логику старта события
        }
    }

    private fun observeTeams() {
        viewModel.allTeams.observe(viewLifecycleOwner) { teamList ->
            if (teamList.isNullOrEmpty()) return@observe

            val teamNames = teamList.map { it.name }

            val teamAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                teamNames
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            spinnerTeam.adapter = teamAdapter
            spinnerTeam.setSelection(0)
            selectedTeam = teamList.first()

            spinnerTeam.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    selectedTeam = teamList[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedTeam = null
                }
            }
        }
    }
}
