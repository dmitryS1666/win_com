package com.cyber90.events.ui.team

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyber90.events.MainActivity
import com.cyber90.events.R
import com.cyber90.events.data.entity.TeamEntity
import com.cyber90.events.data.entity.TeamParticipantEntity
import com.cyber90.events.ui.dashboard.DashboardFragment
import com.cyber90.events.viewmodel.TeamViewModel

class TeamsManagerFragment : Fragment() {

    private lateinit var viewModel: TeamViewModel
    private lateinit var adapter: TeamAdapter
    private var participants: List<TeamParticipantEntity> = emptyList()
    private var participantsCountMap: Map<Long, Int> = emptyMap()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_teams_manager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[TeamViewModel::class.java]

        viewModel.participantsCountByTeam.observe(viewLifecycleOwner) { list ->
            participantsCountMap = list.associate { it.teamId to it.count }
            adapter.updateParticipantsCount(participantsCountMap)
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewTeams)
        val createBtn = view.findViewById<Button>(R.id.buttonCreateTeam)
        val backButton = view.findViewById<ImageView>(R.id.backButton)

        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        adapter = TeamAdapter(
            listOf(),
            participants,
            onEdit = { team -> navigateToEdit(team) },
            onDelete = { team -> confirmDelete(team) },
            onView = { team -> navigateToTeamPlayers(team) }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Обновляем список команд
        viewModel.allTeams.observe(viewLifecycleOwner) {
            adapter.updateList(it)
        }

        // Обновляем список участников
        viewModel.allParticipants.observe(viewLifecycleOwner) {
            participants = it
            adapter.updateTeamParticipants(it)
        }

        createBtn.setOnClickListener {
            (activity as? MainActivity)?.openFragment(CreateTeamFragment())
        }
    }

    private fun navigateToEdit(team: TeamEntity) {
        (activity as? MainActivity)?.openFragment(EditTeamFragment.newInstance(team.id))
    }

    private fun navigateToTeamPlayers(team: TeamEntity) {
        (activity as? MainActivity)?.openFragment(ViewTeamFragment.newInstance(team.id))
    }

    private fun confirmDelete(team: TeamEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Team")
            .setMessage("Are you sure you want to delete this team?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteTeam(team)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
