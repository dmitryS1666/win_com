package com.cyber90.events

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.io.File
import java.io.IOException
import android.Manifest
import android.hardware.camera2.CameraManager

class BannerWebActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var noInternetLayout: View
    private lateinit var webContainer: FrameLayout
    private lateinit var reloadButton: ImageButton

    private val FILE_CHOOSER_REQUEST_CODE = 1000
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var originalSystemUiVisibility = 0

    private var cameraImageUri: Uri? = null
    private var lastPhotoFile: File? = null
    private lateinit var fileChooserLauncher: ActivityResultLauncher<Intent>

    private val CAMERA_PERMISSION_REQUEST = 101
    private var pendingFileChooserParams: WebChromeClient.FileChooserParams? = null
    private var pendingPermissionRequest: PermissionRequest? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Иммерсивный UI
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller?.let {
            it.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            it.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        setContentView(R.layout.activity_banner_web)

        webView = findViewById(R.id.bannerWebView)
        noInternetLayout = findViewById(R.id.noInternetLayout)
        webContainer = findViewById(R.id.webContainer)
        reloadButton = noInternetLayout.findViewById(R.id.reloadButton)

        fileChooserLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val resultUris: Array<Uri>? = when {
                result.resultCode != RESULT_OK -> null
                result.data == null || result.data?.data == null -> {
                    lastPhotoFile?.let { arrayOf(Uri.fromFile(it)) }
                }
                else -> WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
            }

            filePathCallback?.onReceiveValue(resultUris)
            filePathCallback = null
            cameraImageUri = null
        }

        val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        val cameraIds = cameraManager.cameraIdList
        Log.d("CameraDebug", "Available cameras: ${cameraIds.joinToString()}")

        setupWebView()

        val url = intent.getStringExtra("url") ?: run {
            finish()
            return
        }

        if (isOnline()) {
            showWebView()
            webView.loadUrl(url)
        } else {
            showNoInternet()
        }

        reloadButton.setOnClickListener {
            if (isOnline()) {
                showWebView()
                webView.reload()
            } else {
                Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show()
            }
        }

        onBackPressedDispatcher.addCallback(this) {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                finish()
            }
        }
    }

    private fun createImageFile(): File? {
        return try {
            val timeStamp = System.currentTimeMillis()
            val file = File.createTempFile("IMG_$timeStamp", ".jpg", cacheDir)
            lastPhotoFile = file
            file
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }

    private fun setupWebView() {
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            loadsImagesAutomatically = true
            useWideViewPort = true
            loadWithOverviewMode = true
            builtInZoomControls = false
            displayZoomControls = false
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            allowFileAccess = true
            allowContentAccess = true

            mediaPlaybackRequiresUserGesture = false
            domStorageEnabled = true
        }

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                // Очищаем старый callback
                this@BannerWebActivity.filePathCallback?.onReceiveValue(null)
                this@BannerWebActivity.filePathCallback = filePathCallback

                if (ContextCompat.checkSelfPermission(this@BannerWebActivity, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    // Сохраняем параметры выбора файла до разрешения
                    pendingFileChooserParams = fileChooserParams
                    ActivityCompat.requestPermissions(
                        this@BannerWebActivity,
                        arrayOf(Manifest.permission.CAMERA),
                        CAMERA_PERMISSION_REQUEST
                    )
                    return true // <--- ВАЖНО: НЕ продолжаем сейчас!
                }

                // Разрешение уже есть — сразу запускаем
                launchFileChooser(fileChooserParams)
                return true
            }

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (customView != null) {
                    callback?.onCustomViewHidden()
                    return
                }
                customView = view
                customViewCallback = callback

                val decor = window.decorView as FrameLayout
                originalSystemUiVisibility = decor.systemUiVisibility
                decor.addView(view, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                decor.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        )
                actionBar?.hide()
            }

            override fun onHideCustomView() {
                val decor = window.decorView as FrameLayout
                customView?.let { decor.removeView(it) }
                customView = null
                decor.systemUiVisibility = originalSystemUiVisibility
                customViewCallback?.onCustomViewHidden()
                customViewCallback = null
                actionBar?.show()
            }

            override fun onPermissionRequest(request: PermissionRequest?) {
                val origin = request?.origin?.toString()
                val resources = request?.resources

                Log.d("WebViewPermission", "Запрошенные ресурсы: ${resources?.joinToString()} от $origin")

                if (resources?.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE) == true) {
                    // Сохраняем request до разрешения
                    pendingPermissionRequest = request

                    if (ContextCompat.checkSelfPermission(this@BannerWebActivity, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this@BannerWebActivity,
                            arrayOf(Manifest.permission.CAMERA),
                            CAMERA_PERMISSION_REQUEST
                        )
                    } else {
                        request.grant(resources)
                    }
                } else {
                    // Автоматически одобряем другие типы (например, AUDIO)
                    request?.grant(resources)
                }
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                Log.d("BannerWebActivity", "URL click: $url")

                return when {
                    url.startsWith("dln://") -> {
                        val newUrl = url.replace("dln://", "https://")
                        openExternalBrowser(newUrl)
                        true
                    }
                    url.startsWith("tg://") || url.startsWith("viber://") ||
                            url.startsWith("whatsapp://") || url.startsWith("privat24:") ||
                            url.startsWith("https://diia.app") -> {
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(this@BannerWebActivity, "App not found", Toast.LENGTH_SHORT).show()
                        }
                        true
                    }
                    else -> false
                }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                if (request?.isForMainFrame == true) {
                    val failingUrl = request.url.toString()
                    val description = error?.description?.toString() ?: "Неизвестная ошибка"
                    val errorCode = error?.errorCode

                    Log.e("WebViewError", "Ошибка при переходе по $failingUrl: $description (код $errorCode)")

                    Toast.makeText(
                        this@BannerWebActivity,
                        "Failed to open the link:\n$failingUrl\n\nReason: $description",
                        Toast.LENGTH_LONG
                    ).show()

                    // Не вызываем stopLoading() и не скрываем WebView — пусть пользователь продолжает навигацию
                }
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                handler?.proceed() // В реальном приложении лучше показывать предупреждение!
            }
        }
    }

    private fun launchFileChooser(fileChooserParams: WebChromeClient.FileChooserParams?) {
        // Проверяем разрешение ещё раз, на всякий случай
        val intentArray = if (
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        ) {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val photoFile = createImageFile()
            if (photoFile != null) {
                cameraImageUri = FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
                arrayOf(takePictureIntent)
            } else {
                emptyArray()
            }
        } else {
            emptyArray()
        }

        val contentSelectionIntent = fileChooserParams?.createIntent()

        val chooserIntent = Intent(Intent.ACTION_CHOOSER).apply {
            putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
            putExtra(Intent.EXTRA_TITLE, "Выберите файл")
            putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
        }

        fileChooserLauncher.launch(chooserIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            val results = if (resultCode == RESULT_OK && data != null) {
                WebChromeClient.FileChooserParams.parseResult(resultCode, data)
            } else null
            filePathCallback?.onReceiveValue(results)
            filePathCallback = null
        }
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
        if (!isOnline()) showNoInternet()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        webView.restoreState(savedInstanceState)
    }

    private fun showNoInternet() {
//        webView.visibility = View.GONE
        noInternetLayout.visibility = View.VISIBLE
    }

    private fun checkCameraPermissionAndLoadUrl(url: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
        } else {
            webView.loadUrl(url)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pendingPermissionRequest?.grant(pendingPermissionRequest?.resources)
            } else {
                pendingPermissionRequest?.deny()
                Toast.makeText(this, "Camera permission not granted", Toast.LENGTH_SHORT).show()
            }
            pendingPermissionRequest = null
        }
    }

    private fun showWebView() {
        webView.visibility = View.VISIBLE
        noInternetLayout.visibility = View.GONE
    }

    private fun isOnline(): Boolean {
        val cm = getSystemService<ConnectivityManager>()
        val network = cm?.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    private fun openExternalBrowser(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}
