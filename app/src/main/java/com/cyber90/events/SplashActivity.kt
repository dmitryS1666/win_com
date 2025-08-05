package com.cyber90.events

import android.content.Intent
import android.os.*
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.cyber90.events.ui.LoadingFragment
import com.cyber90.events.ui.privacy.PrivacyWebViewActivity
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import okhttp3.*
import java.util.*
import java.util.concurrent.TimeUnit

class SplashActivity : AppCompatActivity() {

    private lateinit var bannerView: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var clickBlocker: View

    private val handler = Handler(Looper.getMainLooper())
    private var isActive = false

    private val prefs by lazy {
        getSharedPreferences("banner_prefs", MODE_PRIVATE)
    }

    private val client = OkHttpClient.Builder()
        .callTimeout(10, TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Cyber90Events)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        bannerView = findViewById(R.id.bannerImageView)
        progressBar = findViewById(R.id.progressBar)
        clickBlocker = findViewById(R.id.clickBlocker)
        clickBlocker.visibility = View.VISIBLE

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.splashRoot, LoadingFragment())
                .commit()
        }

        // Проверка privacy
        checkPrivacy()
    }

    private fun checkPrivacy() {
        val isAccepted = prefs.getBoolean("isPrivacyAccepted", false)
        val userId = prefs.getString("user_id", null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString("user_id", it).apply()
        }

        if (!isAccepted) {
            Thread {
                val installer = packageManager.getInstallerPackageName(packageName) ?: "debug"
                val adId = try {
                    AdvertisingIdClient.getAdvertisingIdInfo(this)?.id ?: "unknown"
                } catch (e: Exception) {
                    "unknown"
                }

                val url =
                    "https://cyber90.xyz/privacy/?installer=$installer&id_user=$userId&id_google=$adId"

                runOnUiThread {
                    PrivacyWebViewActivity.launch(this, url)
                    finish()
                }
            }.start()
        } else {
            handler.postDelayed({ goToMain() }, 1000)
        }
    }

    private fun goToMain() {
        clickBlocker.visibility = View.GONE
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onResume() {
        super.onResume()
        isActive = true
    }

    override fun onStart() {
        super.onStart()
        isActive = true
    }

    override fun onStop() {
        super.onStop()
        isActive = false
    }

    override fun onDestroy() {
        super.onDestroy()
        isActive = false
    }
}
