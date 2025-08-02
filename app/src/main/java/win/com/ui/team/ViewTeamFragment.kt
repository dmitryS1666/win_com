package win.com.ui.team

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import win.com.MainActivity
import win.com.R
import win.com.ui.teams.TeamViewModel

class ViewTeamFragment : Fragment() {

    private var teamId: Int = -1
    private lateinit var viewModel: TeamViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        teamId = arguments?.getInt(ARG_TEAM_ID) ?: -1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_view_team, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[TeamViewModel::class.java]

        val teamName = view.findViewById<TextView>(R.id.viewTeamName)
        val participantsContainer = view.findViewById<LinearLayout>(R.id.viewTeamParticipants)
        val backButton = view.findViewById<ImageView>(R.id.backButton)

        // 3. Отступ снизу и сверху у названия команды
        teamName.apply {
            setTextColor(Color.WHITE)
            textSize = 22f // можно увеличить, если нужно
            // Используем отступы сверху и снизу в пикселях, преобразуем из dp в px
            val topBottomPaddingPx = (10 * resources.displayMetrics.density).toInt()
            val bottomPaddingPx = (20 * resources.displayMetrics.density).toInt()
            setPadding(paddingLeft, topBottomPaddingPx, paddingRight, bottomPaddingPx)
        }

        backButton.setOnClickListener {
            (activity as? MainActivity)?.openFragment(TeamsManagerFragment())
        }

        viewModel.getTeamById(teamId).observe(viewLifecycleOwner) { team ->
            team?.let {
                teamName.text = it.name

                viewModel.getParticipants(it.id).observe(viewLifecycleOwner) { participantsList ->

                    participantsContainer.removeAllViews()

                    // Заголовок таблицы
                    val headerLayout = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.HORIZONTAL
                    }
                    val headerPlayer = TextView(requireContext()).apply {
                        text = "Player"
                        setTypeface(typeface, android.graphics.Typeface.BOLD)
                        setTextColor(Color.WHITE)        // 2. Белый цвет
                        textSize = 18f                   // 1. Крупный текст для заголовка
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    }
                    val headerRole = TextView(requireContext()).apply {
                        text = "Role"
                        setTypeface(typeface, android.graphics.Typeface.BOLD)
                        setTextColor(Color.WHITE)        // 2. Белый цвет
                        textSize = 18f                   // 1. Крупный текст для заголовка
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    }
                    headerLayout.addView(headerPlayer)
                    headerLayout.addView(headerRole)
                    participantsContainer.addView(headerLayout)

                    if (participantsList.isEmpty()) {
                        val noParticipantsText = TextView(requireContext()).apply {
                            text = "No participants"
                            setTextColor(Color.WHITE)    // Белый текст
                            setPadding(0, 16, 0, 16)
                            textSize = 16f               // Немного крупнее
                        }
                        participantsContainer.addView(noParticipantsText)
                    } else {
                        for (participant in participantsList) {
                            val rowLayout = LinearLayout(requireContext()).apply {
                                orientation = LinearLayout.HORIZONTAL
                                setPadding(0, 8, 0, 8)
                            }
                            val playerText = TextView(requireContext()).apply {
                                text = participant.name
                                setTextColor(Color.WHITE)    // Белый текст
                                textSize = 16f               // Немного крупнее
                                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                            }
                            val roleText = TextView(requireContext()).apply {
                                text = participant.role
                                setTextColor(Color.WHITE)    // Белый текст
                                textSize = 16f               // Немного крупнее
                                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                            }
                            rowLayout.addView(playerText)
                            rowLayout.addView(roleText)
                            participantsContainer.addView(rowLayout)
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val ARG_TEAM_ID = "team_id"

        fun newInstance(teamId: Int): ViewTeamFragment {
            val fragment = ViewTeamFragment()
            val args = Bundle()
            args.putInt(ARG_TEAM_ID, teamId)
            fragment.arguments = args
            return fragment
        }
    }
}
