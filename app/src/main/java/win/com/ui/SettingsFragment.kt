package win.com.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import win.com.BannerWebActivity
import win.com.MainActivity
import win.com.R
import win.com.data.database.AppDatabase
import win.com.data.repository.DataRepository
import win.com.ui.dashboard.DashboardFragment
import win.com.viewmodel.DashboardViewModel
import win.com.viewmodel.DashboardViewModelFactory

class SettingsFragment : Fragment() {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var clearDataLayout: View
    private lateinit var shareLink: TextView
    private lateinit var rateLink: TextView
    private lateinit var privacyPolicyLink: TextView
    private lateinit var backButton: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val application = requireActivity().application
        val db = AppDatabase.getDatabase(application)
        val dataRepository = DataRepository(db.eventDao(), db.participantDao(), db.teamParticipantDao(), db.teamDao())

        val factory = DashboardViewModelFactory(application, dataRepository)
        viewModel = ViewModelProvider(this, factory)[DashboardViewModel::class.java]

        // Найди элементы
        clearDataLayout = view.findViewById(R.id.clearDataLayout)
        shareLink = view.findViewById(R.id.shareLink)
        rateLink = view.findViewById(R.id.rateLink)
        privacyPolicyLink = view.findViewById(R.id.privacyPolicyLink)
        backButton = view.findViewById(R.id.backButton)

        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        clearDataLayout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Clear All Data")
                .setMessage("Are you sure you want to delete all local data?")
                .setPositiveButton("Yes") { _, _ ->
                    viewModel.clearAllData()
                    prefs.edit().clear().apply()
                    Toast.makeText(requireContext(), "Data cleared", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        shareLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, "Check out this app: https://WinCom365.com")
            startActivity(Intent.createChooser(intent, "Share via"))
        }

        rateLink.setOnClickListener {
            val uri = Uri.parse("market://details?id=${requireContext().packageName}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        privacyPolicyLink.setOnClickListener {
            val intent = Intent(requireContext(), BannerWebActivity::class.java)
            intent.putExtra("url", "https://WinCom365.com/policy")
            startActivity(intent)
        }

        backButton.setOnClickListener {
            (activity as? MainActivity)?.openFragment(DashboardFragment())
        }
    }
}
