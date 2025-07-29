package win.com.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.fragment.app.Fragment
import win.com.BannerWebActivity
import win.com.MusicPlayerManager
import win.com.R

class SettingsFragment : Fragment() {

    private lateinit var switchSound: Switch

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        switchSound = view.findViewById(R.id.switchSound)
        val privacySection: View = view.findViewById(R.id.btnPrivacy)

        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val soundEnabled = prefs.getBoolean("sound_enabled", true)
        switchSound.isChecked = soundEnabled

        switchSound.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("sound_enabled", isChecked).apply()

            if (isChecked) {
                MusicPlayerManager.start(requireContext())
            } else {
                MusicPlayerManager.stop()
            }
        }

        privacySection.setOnClickListener {
            val url = "https://WinCom365.com/policy"
            val intent = Intent(context, BannerWebActivity::class.java)
            intent.putExtra("url", url)
            startActivity(intent)
        }
    }
}
