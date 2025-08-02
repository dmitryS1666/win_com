package win.com.ui.team

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
import win.com.MainActivity
import win.com.R
import win.com.data.entity.TeamEntity
import win.com.ui.teams.TeamViewModel

class TeamsManagerFragment : Fragment() {

    private lateinit var viewModel: TeamViewModel
    private lateinit var adapter: TeamAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_teams_manager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[TeamViewModel::class.java]

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewTeams)
        val createBtn = view.findViewById<Button>(R.id.buttonCreateTeam)
        val backButton = view.findViewById<ImageView>(R.id.backButton)

        // Возврат назад
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        adapter = TeamAdapter(
            listOf(),
            onEdit = { team -> navigateToEdit(team) },
            onDelete = { team -> confirmDelete(team) },
            onView = { team -> navigateToTeamPlayers(team) }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewModel.allTeams.observe(viewLifecycleOwner) {
            adapter.updateList(it)
        }

        createBtn.setOnClickListener {
            (activity as? MainActivity)?.openFragment(CreateTeamFragment())
            (activity as? MainActivity)?.updateNavIcons("teams")
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
