package com.cyber90.events.ui.privacy

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.cyber90.events.MainActivity
import com.cyber90.events.R
import com.github.mikephil.charting.BuildConfig
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class PrivacyWebViewActivity : AppCompatActivity() {

    companion object {
        private const val PREFS_NAME = "banner_prefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_ACCEPTED = "isPrivacyAccepted"
        private const val PRIVACY_BASE_URL = "https://cyber90.xyz/privacy/"

        @OptIn(UnstableApi::class)
        fun launch(context: Context, url: String) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val userId = prefs.getString(KEY_USER_ID, null) ?: UUID.randomUUID().toString().also {
                prefs.edit().putString(KEY_USER_ID, it).apply()
            }

            val isAccepted = prefs.getBoolean(KEY_ACCEPTED, false)

            if (isAccepted) {
                // Сразу в аппу, без показа privacy
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
                return
            }

            // Получаем installer
            val installer = if (BuildConfig.DEBUG) {
                "debug"
            } else {
                try {
                    context.packageManager.getInstallerPackageName(context.packageName) ?: "unknown"
                } catch (e: Exception) {
                    "unknown"
                }
            }

            // Запуск корутины для получения google ad id
            (context as? AppCompatActivity)?.lifecycleScope?.launch {
                val adId = getGoogleAdId(context)

                // Формируем URL с параметрами
                val url = buildString {
                    append(PRIVACY_BASE_URL)
                    append("?installer=").append(installer)
                    append("&id_user=").append(userId)
                    append("&id_google=").append(adId)
                    if (!isAccepted) append("&isPrivacyAccepted=false")
                }

                // Заголовки
                val acceptLanguage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    context.resources.configuration.locales[0].toLanguageTag()
                } else {
                    @Suppress("DEPRECATION")
                    context.resources.configuration.locale.toLanguageTag()
                }

                val userAgent = WebSettings.getDefaultUserAgent(context)

                val headers = mapOf(
                    "Accept-Language" to acceptLanguage,
                    "User-Agent" to userAgent
                )

                // Логируем
                Log.i("PrivacyWebViewLaunch", "Launching Privacy URL: $url")
                Log.i("PrivacyWebViewLaunch", "Headers:")
                headers.forEach { (k, v) -> Log.i("PrivacyWebViewLaunch", "  $k: $v") }
                Log.i(
                    "PrivacyWebViewLaunch",
                    "installer: $installer, userId: $userId, isAccepted: $isAccepted, googleAdId: $adId"
                )

                // Запускаем активити и передаем URL и заголовки через intent
                val intent = Intent(context, PrivacyWebViewActivity::class.java).apply {
                    putExtra("url", url)
                    putExtra("headers", HashMap(headers))
                }
                context.startActivity(intent)
            } ?: run {
                // Если не AppCompatActivity (без корутин), запускаем без adId (fallback)
                val urlFallback =
                    "$PRIVACY_BASE_URL?installer=$installer&id_user=$userId&id_google=unknown" +
                            if (!isAccepted) "&isPrivacyAccepted=false" else ""

                val headersFallback = mapOf(
                    "Accept-Language" to "en-US",
                    "User-Agent" to "AndroidWebView"
                )

                Log.i("PrivacyWebViewLaunch", "Launching Privacy URL (fallback): $urlFallback")
                Log.i("PrivacyWebViewLaunch", "Headers (fallback): $headersFallback")

                val intent = Intent(context, PrivacyWebViewActivity::class.java).apply {
                    putExtra("url", urlFallback)
                    putExtra("headers", HashMap(headersFallback))
                }
                context.startActivity(intent)
            }
        }

        @OptIn(UnstableApi::class)
        private suspend fun getGoogleAdId(context: Context): String = withContext(Dispatchers.IO) {
            try {
                AdvertisingIdClient.getAdvertisingIdInfo(context).id ?: "unknown"
            } catch (e: Exception) {
                Log.e("PrivacyWebViewLaunch", "Failed to get Google Ad ID", e)
                "unknown"
            }
        }
    }

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Cyber90Events)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_webview)

        val url = intent.getStringExtra("url") ?: run {
            finish()
            return
        }

        webView = findViewById(R.id.webview)
        val prefs = getSharedPreferences("banner_prefs", MODE_PRIVATE)

        // Accept-Language and User-Agent
        val acceptLanguage = resources.configuration.locales.get(0).toLanguageTag()
        val userAgent = WebSettings.getDefaultUserAgent(this)

        val headers = mapOf(
            "Accept-Language" to acceptLanguage,
            "User-Agent" to userAgent
        )

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.userAgentString = userAgent

        webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun onUserPrivacyAccepted() {
                runOnUiThread {
                    prefs.edit().putBoolean(KEY_ACCEPTED, true).apply()

                    val intent = Intent(this@PrivacyWebViewActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)

//                    finish()
                }
            }
        }, "android")

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                return false
            }
        }

        webView.loadUrl(url, headers)
    }
}
