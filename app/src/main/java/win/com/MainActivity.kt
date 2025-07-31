package win.com

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import win.com.ui.LoadingFragment
import win.com.ui.SettingsFragment
import win.com.ui.welcome.WelcomeFragment
import win.com.ui.WorkoutFragment
import win.com.ui.WorkoutPlanConstants
import win.com.ui.dashboard.DashboardFragment
import win.com.ui.event.CreateEventFragment
import win.com.ui.theme.WinComTheme
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNav: View
    private lateinit var navDashBoard: LinearLayout
    private lateinit var navEvent: LinearLayout
    private lateinit var navTeams: LinearLayout
    private lateinit var navSet: LinearLayout

    private lateinit var dashboardIcon: ImageView
    private lateinit var eventIcon: ImageView
    private lateinit var teamsIcon: ImageView
    private lateinit var setIcon: ImageView

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MusicPlayerManager.start(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT

        WorkoutPlanConstants.loadIndividualPlans(this)
        setContentView(R.layout.activity_main)

        // Принудительно установить английскую локаль
        val locale = Locale("en")
        Locale.setDefault(locale)

        if (savedInstanceState == null) {
            val skipLoading = intent.getBooleanExtra("skip_loading", false)
            if (!skipLoading) {
                openLoadingFragment()
            } else {
                openMainFragment()
            }
        }

        hideSystemUI()

        // Инициализация элементов навигации
        navDashBoard = findViewById(R.id.navDashBoard)
        navEvent = findViewById(R.id.navEvent)
        navTeams = findViewById(R.id.navTeams)
        navSet = findViewById(R.id.navSet)

        // Инициализация иконок
        dashboardIcon = navDashBoard.findViewById(R.id.iconDashboard)
        eventIcon = navEvent.findViewById(R.id.iconEvent)
        teamsIcon = navTeams.findViewById(R.id.iconTeams)
        setIcon = navSet.findViewById(R.id.iconSet)

        // Обработчики кликов для каждого элемента нижней панели
        navDashBoard.setOnClickListener {
            showBottomNav()
            openFragment(DashboardFragment())
            updateNavIcons("dashboard")
        }

        navEvent.setOnClickListener {
            showBottomNav()
            openFragment(CreateEventFragment())
            updateNavIcons("events")
        }

        navTeams.setOnClickListener {
            showBottomNav()
            openFragment(WorkoutFragment())
            updateNavIcons("teams")
        }

        navSet.setOnClickListener {
            showBottomNav()
            openFragment(SettingsFragment())
            updateNavIcons("set")
        }
    }

    fun updateNavIcons(activeFragment: String) {
        // Сбросить все иконки
        resetNavIcons()

        when (activeFragment) {
            "dashboard" -> {
                dashboardIcon.setImageResource(R.drawable.icon_dashboard_active)
            }
            "events" -> {
                eventIcon.setImageResource(R.drawable.icon_event_active)
            }
            "teams" -> {
                teamsIcon.setImageResource(R.drawable.icon_team_active)
            }
            "set" -> {
                setIcon.setImageResource(R.drawable.settings_acitve)
            }
        }
    }

    private fun resetNavIcons() {
        // Сбросить все иконки
        dashboardIcon.setImageResource(R.drawable.icon_dashboard)
        eventIcon.setImageResource(R.drawable.icon_event)
        teamsIcon.setImageResource(R.drawable.icon_team)
        setIcon.setImageResource(R.drawable.settings)
    }

    // Метод для замены фрагмента
    fun openFragment(fragment: Fragment) {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainFragmentContainer, fragment)
            .commit()
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )

        hideBottomNav()
    }

    // Метод для отображения панели навигации
    fun showBottomNav() {
        bottomNav = findViewById(R.id.bottomNavInclude)
        bottomNav.visibility = View.VISIBLE
    }

    // Метод для скрытия панели навигации
    fun hideBottomNav() {
        bottomNav = findViewById(R.id.bottomNavInclude)
        bottomNav.visibility = View.GONE
    }

    private fun openLoadingFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainFragmentContainer, LoadingFragment())
            .commit()
    }

    fun openDashboardFragment() {
        if (isFinishing || isDestroyed) return

        showBottomNav()

        supportFragmentManager.beginTransaction()
            .replace(R.id.mainFragmentContainer, DashboardFragment())
            .commit()

        window.decorView.post {
            if (!isFinishing && !isDestroyed) {
                updateNavIcons("dashboard")
            }
        }
    }

    fun openMainFragment() {
        val sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE)
        val isFirstLaunch = sharedPreferences.getBoolean("isFirstLaunch", true)

        hideBottomNav()

        val fragment = WelcomeFragment()
//        if (isFirstLaunch) {
//            WelcomeFragment()
//        } else {
//            DashboardFragment()
//        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.mainFragmentContainer, fragment)
            .commit()
    }

    fun openWorkoutFragment() {
        showBottomNav()

        supportFragmentManager.beginTransaction()
            .replace(R.id.mainFragmentContainer, WorkoutFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        // Подождём, пока фрагмент реально заменится
        supportFragmentManager.executePendingTransactions()

        val currentFragment = supportFragmentManager.findFragmentById(R.id.mainFragmentContainer)

        if (currentFragment is WelcomeFragment) {
            hideBottomNav()
        } else {
            showBottomNav()
        }
    }

    override fun onStop() {
        super.onStop()
        MusicPlayerManager.stop()
    }

    override fun onStart() {
        super.onStart()
        MusicPlayerManager.start(this)
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI() // Снова скрываем системные кнопки при возвращении
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WinComTheme {
        Greeting("Android")
    }
}