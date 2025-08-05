package com.cyber90.events.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import android.webkit.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.cyber90.events.SplashActivity // заглушка
import java.util.*
import android.util.Log
import android.webkit.WebSettings
import androidx.annotation.RequiresApi
import androidx.core.os.LocaleListCompat
import com.cyber90.events.R
import com.github.mikephil.charting.BuildConfig
import com.google.android.gms.ads.identifier.AdvertisingIdClient

class PrivacyWebViewFragment : Fragment() {

    companion object {
        private const val PREFS_NAME = "privacy_prefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_ACCEPTED = "privacy_accepted"
        private const val PRIVACY_URL = "https://cyber90.xyz/privacy/"
    }

    private lateinit var webView: WebView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_privacy_webview, container, false)

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        webView = view.findViewById(R.id.webView)

        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isAccepted = prefs.getBoolean(KEY_ACCEPTED, false)

        if (isAccepted) {
            goToSplash()
            return
        }

        // Генерация или получение userId
        val userId = prefs.getString(KEY_USER_ID, null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY_USER_ID, it).apply()
        }

        lifecycleScope.launch {
            val installer = getInstallerPackageName(requireContext())
            val googleAdId = getGoogleAdId(requireContext())
            val acceptLang = getAcceptLanguage()
            val userAgent = getUserAgent()

            val url = "$PRIVACY_URL?installer=$installer&id_user=$userId&id_google=$googleAdId&isPrivacyAccepted=false"

            withContext(Dispatchers.Main) {
                webView.settings.javaScriptEnabled = true
                webView.settings.domStorageEnabled = true
                webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE

                webView.addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onUserPrivacyAccepted() {
                        prefs.edit().putBoolean(KEY_ACCEPTED, true).apply()
                        goToSplash()
                    }
                }, "android")

                webView.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        return false
                    }

                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest
                    ): WebResourceResponse? {
                        return super.shouldInterceptRequest(view, request)
                    }
                }

                val headers = mapOf(
                    "Accept-Language" to acceptLang,
                    "User-Agent" to userAgent
                )

                webView.loadUrl(url, headers)
            }
        }
    }

    private fun goToSplash() {
        val intent = Intent(requireContext(), SplashActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun getInstallerPackageName(context: Context): String {
        return try {
            val pm = context.packageManager
            val installer = pm.getInstallSourceInfo(context.packageName).installingPackageName
            installer ?: "unknown"
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) "debug" else "unknown"
        }
    }

    private suspend fun getGoogleAdId(context: Context): String = withContext(Dispatchers.IO) {
        try {
            AdvertisingIdClient.getAdvertisingIdInfo(context).id ?: "null"
        } catch (e: Exception) {
            Log.e("PrivacyWebView", "Failed to get Ad ID", e)
            "null"
        }
    }

    private fun getAcceptLanguage(): String {
        val locale = LocaleListCompat.getAdjustedDefault()[0]
        return locale?.toLanguageTag() ?: "en-US"
    }

    private fun getUserAgent(): String {
        return WebSettings.getDefaultUserAgent(requireContext())
    }
}
