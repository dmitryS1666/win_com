package win.com

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import win.com.ui.LoadingFragment
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

class SplashActivity : AppCompatActivity() {

    private lateinit var bannerView: ImageView
    private lateinit var progressBar: ProgressBar
    private val prefs by lazy {
        getSharedPreferences("banner_prefs", MODE_PRIVATE)
    }

    private lateinit var clickBlocker: View

    private val handler = Handler(Looper.getMainLooper())
    private var isActive = false

    private val client = OkHttpClient.Builder()
        .callTimeout(10, TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_WinCom)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        bannerView = findViewById(R.id.bannerImageView)
        progressBar = findViewById(R.id.progressBar)
        clickBlocker = findViewById(R.id.clickBlocker)
        clickBlocker.visibility = View.VISIBLE

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.splashRoot, LoadingFragment())
                .commit()
        }

//        val testIntent = Intent(this, BannerWebActivity::class.java)
//        testIntent.putExtra("url", "https://qshuimpvz.com/dRnBXT")
//        startActivity(testIntent)
//        finish()

        checkConnectionAndLoadBanner()
    }

    @OptIn(UnstableApi::class)
    private fun checkConnectionAndLoadBanner() {
        Thread {
            val isConnected = checkInternetAccess()
            runOnUiThread {
//                if (isConnected) {
//                    for ((key, value) in prefs.all) {
//                        Log.d("RESPONSE", "$key: $value")
//                    }
//                    val cached = prefs.getString("banner_json", null)
//                    if (cached != null) {
//                        handleBanner(JSONObject(cached))
//                    } else {
//                        fetchBanner()
//                    }
//                } else {
//                    goToMain()
                    handler.postDelayed({ goToMain() }, 1000) // или 1500 мс
//                }
            }
        }.start()
    }

    private fun checkInternetAccess(): Boolean {
        return try {
            val url = URL("https://clients3.google.com/generate_204")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("User-Agent", "Android")
            conn.setRequestProperty("Connection", "close")
            conn.connectTimeout = 1500
            conn.readTimeout = 1500
            conn.connect()
            conn.responseCode == 204
        } catch (e: Exception) {
            false
        }
    }

    private fun fetchBanner() {
        val request = Request.Builder()
            .url("https://WinCom365.com/dontStop365")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                goToMain()
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (!isActive || isFinishing || isDestroyed) return

                if (body != null) {
                    prefs.edit().putString("banner_json", body).apply()
                    runOnUiThread {
                        handleBanner(JSONObject(body))
                    }
                } else {
                    runOnUiThread { handler.postDelayed({ goToMain() }, 1000) }
                }
            }
        })
    }

    private fun handleBanner(json: JSONObject) {
        val action = json.optString("pushLink", null)
        val imageUrl = json.optString("dontStop", null)

        // 👉 Если 2 параметра - сразу переходим
        if (!action.isNullOrEmpty() && !imageUrl.isNullOrEmpty()) {
            if (action.startsWith("http")) {
                // В WebView
                val intent = Intent(this, BannerWebActivity::class.java)
                intent.putExtra("url", action)
                startActivity(intent)
                finish()
            } else {
                // Внешний браузер
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(action)))
                } catch (e: Exception) {
                    // Не получилось открыть — fallback
                }
                finish()
            }
            return
        }

        // 👉 Заглушка (1 параметр) — показываем баннер
        if (!imageUrl.isNullOrEmpty()) {
            showBanner(imageUrl, action)
        } else {
            goToMain()
        }
    }

    private fun showBanner(imageUrl: String, action: String?) {
        Glide.with(this)
            .load(imageUrl)
            .centerCrop()
            .into(object : CustomTarget<Drawable>() {
                @OptIn(UnstableApi::class)
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    if (!isActive || isFinishing || isDestroyed) return

                    bannerView.setImageDrawable(resource)
                    bannerView.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    clickBlocker.visibility = View.GONE

                    bannerView.setOnClickListener {
                        Log.d("setOnClickListener", "$action")
                        if (action != null && action.startsWith("http")) {
                            val intent = Intent(this@SplashActivity, BannerWebActivity::class.java)
                            intent.putExtra("url", action)
                            startActivity(intent)
                        } else {
                            val intent = Intent(this@SplashActivity, MainActivity::class.java)
                            intent.putExtra("skip_loading", true)
                            startActivity(intent)
                            finish()
                        }
                        finish()
                    }
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    goToMain()
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private fun goToMain() {
        clickBlocker.visibility = View.GONE
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onResume() {
        super.onResume()
        isActive = true

        // 👉 Возврат из внешнего браузера — показать баннер, если он есть
        val cached = prefs.getString("banner_json", null)
        if (cached != null) {
            val json = JSONObject(cached)
            val action = json.optString("pushLink", null)
            val imageUrl = json.optString("dontStop", null)
            if (!action.isNullOrEmpty() && !imageUrl.isNullOrEmpty()) {
                showBanner(imageUrl, action)
            }
        }
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

