package com.cyber90.events.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.BuildConfig
import com.cyber90.events.MainActivity
import com.cyber90.events.R
import com.cyber90.events.data.database.AppDatabase
import com.cyber90.events.data.repository.DataRepository
import com.cyber90.events.ui.dashboard.DashboardFragment
import com.cyber90.events.ui.privacy.PrivacyWebViewActivity
import com.cyber90.events.viewmodel.DashboardViewModel
import com.cyber90.events.viewmodel.DashboardViewModelFactory
import java.util.UUID

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

    @RequiresApi(Build.VERSION_CODES.R)
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

        val appPrefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        clearDataLayout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Clear All Data")
                .setMessage("Are you sure you want to delete all local data?")
                .setPositiveButton("Yes") { _, _ ->
                    viewModel.clearAllData()
                    appPrefs.edit().clear().apply()
                    Toast.makeText(requireContext(), "Data cleared", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        shareLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, "Check out this app: https://Cyber90Events365.com")
            startActivity(Intent.createChooser(intent, "Share via"))
        }

        rateLink.setOnClickListener {
            val uri = Uri.parse("market://details?id=${requireContext().packageName}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "Google Play not found", Toast.LENGTH_SHORT).show()
            }
        }

        privacyPolicyLink.setOnClickListener {
            val bannerPrefs = requireContext().getSharedPreferences("banner_prefs", Context.MODE_PRIVATE)
            val userId = bannerPrefs.getString("user_id", UUID.randomUUID().toString())
            val installer = getInstallerPackageName(requireContext())

            val url = "https://cyber90.xyz/privacy/?installer=$installer&id_user=$userId"

            val intent = Intent(requireContext(), PrivacyWebViewActivity::class.java)
            intent.putExtra("url", url)
            startActivity(intent)
        }

        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun getInstallerPackageName(context: Context): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName ?: "unknown"
            } else {
                context.packageManager.getInstallerPackageName(context.packageName) ?: "unknown"
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) "debug" else "unknown"
        }
    }
}
