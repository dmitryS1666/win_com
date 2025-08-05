package com.cyber90.events

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
import com.cyber90.events.ui.LoadingFragment
import com.cyber90.events.ui.SettingsFragment
import com.cyber90.events.ui.welcome.WelcomeFragment
import com.cyber90.events.ui.dashboard.DashboardFragment
import com.cyber90.events.ui.event.AllEventsFragment
import com.cyber90.events.ui.event.CreateEventFragment
import com.cyber90.events.ui.event.EditEventFragment
import com.cyber90.events.ui.event.LiveControlPanelFragment
import com.cyber90.events.ui.event.ViewEventFragment
import com.cyber90.events.ui.result.ResultsFragment
import com.cyber90.events.ui.team.CreateTeamFragment
import com.cyber90.events.ui.team.EditTeamFragment
import com.cyber90.events.ui.team.TeamsManagerFragment
import com.cyber90.events.ui.team.ViewTeamFragment
import com.cyber90.events.ui.theme.Cyber90EventsTheme
import java.util.Locale
import com.cyber90.events.R

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
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT

        setContentView(R.layout.activity_main)

        // Инициализация bottomNav один раз
        bottomNav = findViewById(R.id.bottomNavInclude)

        // Принудительно установить английскую локаль (можно оставить, но лучше продумать локализацию)
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
            openFragment(AllEventsFragment())
            updateNavIcons("events")
        }

        navTeams.setOnClickListener {
            showBottomNav()
            openFragment(TeamsManagerFragment())
            updateNavIcons("teams")
        }

        navSet.setOnClickListener {
            showBottomNav()
            openFragment(SettingsFragment())
            updateNavIcons("set")
        }

        supportFragmentManager.addOnBackStackChangedListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.mainFragmentContainer)

            when (fragment) {
                is DashboardFragment -> updateNavIcons("dashboard")

                is AllEventsFragment,
                is CreateEventFragment,
                is ViewEventFragment,
                is EditEventFragment,
                is LiveControlPanelFragment,
                is ResultsFragment -> updateNavIcons("events")

                is TeamsManagerFragment,
                is CreateTeamFragment,
                is EditTeamFragment,
                is ViewTeamFragment -> updateNavIcons("teams")

                is SettingsFragment -> updateNavIcons("set")
                else -> {
                    // Можно сбросить иконки или скрыть навигацию, если нужен кастом
                }
            }
        }
    }

    fun updateNavIcons(activeFragment: String) {
        resetNavIcons()
        when (activeFragment) {
            "dashboard" -> dashboardIcon.setImageResource(R.drawable.icon_dashboard_active)
            "events" -> eventIcon.setImageResource(R.drawable.icon_event_active)
            "teams" -> teamsIcon.setImageResource(R.drawable.icon_team_active)
            "set" -> setIcon.setImageResource(R.drawable.settings_active) // исправлено
        }
    }

    private fun resetNavIcons() {
        dashboardIcon.setImageResource(R.drawable.icon_dashboard)
        eventIcon.setImageResource(R.drawable.icon_event)
        teamsIcon.setImageResource(R.drawable.icon_team)
        setIcon.setImageResource(R.drawable.settings)
    }

    fun openFragment(fragment: Fragment) {
        // Здесь подумайте, действительно ли нужно очищать весь backstack?
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainFragmentContainer, fragment)
            .addToBackStack(fragment::class.java.name)
            .commit()
    }

    fun showBottomNav() {
        bottomNav.visibility = View.VISIBLE
    }

    fun hideBottomNav() {
        bottomNav.visibility = View.GONE
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager
        if (fragmentManager.backStackEntryCount > 1) {
            fragmentManager.popBackStack()
            fragmentManager.executePendingTransactions()

            val currentFragment = fragmentManager.findFragmentById(R.id.mainFragmentContainer)
            if (currentFragment is WelcomeFragment) {
                hideBottomNav()
            } else {
                showBottomNav()
            }
        } else {
            finish()
        }
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
        hideBottomNav()
        val fragment = WelcomeFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainFragmentContainer, fragment)
            .commit()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
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
    Cyber90EventsTheme {
        Greeting("Android")
    }
}