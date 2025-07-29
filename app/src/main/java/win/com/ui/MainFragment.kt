package win.com.ui

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import win.com.MainActivity
import win.com.R

class MainFragment : Fragment() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedPreferences = requireActivity().getSharedPreferences("UserData", MODE_PRIVATE)

        val isFirstLaunch = sharedPreferences.getBoolean("isFirstLaunch", true)
        val hasStarted = sharedPreferences.getBoolean("hasStarted", false)

        val view = inflater.inflate(R.layout.fragment_main, container, false)

        val startButton: Button = view.findViewById(R.id.startButton)

        // Обработчик нажатия на кнопку
        startButton.setOnClickListener {
            if (isFirstLaunch) {
                // Перенаправить на экран ввода параметров
                (activity as? MainActivity)?.hideBottomNav()
                (activity as? MainActivity)?.openFragment(ParamFragment())
                (activity as? MainActivity)?.updateNavIcons("param")

                sharedPreferences.edit()
                    .putBoolean("isFirstLaunch", false)
                    .putBoolean("hasStarted", true)
                    .apply()
            } else if (hasStarted) {
                // Перенаправить на экран тренировки
                (activity as? MainActivity)?.openWorkoutFragment()  // Переход через MainActivity
            } else {
                // Если не первый запуск, но не нажал старт (на всякий случай)
                (activity as? MainActivity)?.openWorkoutFragment()  // Переход через MainActivity
            }
        }

        return view
    }
}
