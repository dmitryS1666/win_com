package win.com.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import win.com.MainActivity
import win.com.R
import win.com.ui.event.AllEventsFragment
import win.com.ui.event.CreateEventFragment
import win.com.ui.event.EditEventFragment
import win.com.ui.team.CreateTeamFragment
import win.com.viewmodel.DashboardViewModel

class DashboardFragment : Fragment() {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var imageSlider: ViewPager2
    private val images = listOf(
        R.drawable.slide_1,
        R.drawable.slide_2,
        R.drawable.slide_3
    )
    private lateinit var sliderDots: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): android.view.View {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        imageSlider = view.findViewById(R.id.imageSlider)
        sliderDots = view.findViewById(R.id.sliderDots1)
        imageSlider.adapter = ImageSliderAdapter(images)

        setupSliderDots(images.size)
        imageSlider.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position)
            }
        })

        viewModel.lastEvent.observe(viewLifecycleOwner) { event ->
            if (event != null) {
                val eventTitle = "🏁 ${event.name}"
                val eventInfoDate = "📅 ${event.date}"

                // Получаем участников
                viewModel.getParticipantsForEvent(event.id).observe(viewLifecycleOwner) { participants ->
                    val count = participants.size
                    val max = event.maxParticipants
                    val eventInfoPart = "👥 $count / $max"

                    view.findViewById<TextView>(R.id.eventInfoPart).text = eventInfoPart
                }

                view.findViewById<TextView>(R.id.eventTitle).text = eventTitle
                view.findViewById<TextView>(R.id.eventInfoDate).text = eventInfoDate

                view.findViewById<Button>(R.id.manageEventButton).setOnClickListener {
                    val fragment = EditEventFragment().apply {
                        arguments = Bundle().apply {
                            putInt("event_id", event.id)  // передаем ID события
                        }
                    }
                    (activity as? MainActivity)?.openFragment(fragment)
                }

            } else {
                view.findViewById<TextView>(R.id.eventTitle).text = "🏁 Нет события"
                view.findViewById<TextView>(R.id.eventInfoDate).text = "Данных нет"
                view.findViewById<TextView>(R.id.eventInfoPart).text = "Данных нет"
            }
        }

        // Кнопки
        view.findViewById<FrameLayout>(R.id.actionCreate).setOnClickListener {
            (activity as? MainActivity)?.openFragment(CreateEventFragment())
            (activity as? MainActivity)?.updateNavIcons("events")
        }

        view.findViewById<FrameLayout>(R.id.actionEvents).setOnClickListener {
            (activity as? MainActivity)?.openFragment(AllEventsFragment())
            (activity as? MainActivity)?.updateNavIcons("events")
        }

        view.findViewById<FrameLayout>(R.id.actionTeams).setOnClickListener {
            (activity as? MainActivity)?.openFragment(CreateTeamFragment())
            (activity as? MainActivity)?.updateNavIcons("teams")
        }

        view.findViewById<FrameLayout>(R.id.actionResults).setOnClickListener {
            // TODO: открыть ResultsFragment
        }
    }

    private fun setupSliderDots(count: Int) {
        sliderDots.removeAllViews()
        for (i in 0 until count) {
            val dot = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(16, 16).apply {
                    setMargins(8, 0, 8, 0)
                }
                setBackgroundResource(R.drawable.dot_inactive) // создаёшь drawable
            }
            sliderDots.addView(dot)
        }
        updateDots(0)
    }

    private fun updateDots(position: Int) {
        for (i in 0 until sliderDots.childCount) {
            val dot = sliderDots.getChildAt(i)
            dot.setBackgroundResource(
                if (i == position) R.drawable.dot_active else R.drawable.dot_inactive
            )
        }
    }
}
