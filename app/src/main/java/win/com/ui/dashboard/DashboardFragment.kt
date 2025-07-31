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
import win.com.ui.event.CreateEventFragment

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
            val eventTitle = if (event != null) "🏁 ${event.name}" else "🏁 Нет события"
            val eventInfoDate = if (event != null) "📅 ${event.date}" else "Данных нет"
            val eventInfoPart = if (event != null) "👥 0 / ${event.maxParticipants}" else "Данных нет"

            view.findViewById<TextView>(R.id.eventTitle).text = eventTitle
            view.findViewById<TextView>(R.id.eventInfoDate).text = eventInfoDate
            view.findViewById<TextView>(R.id.eventInfoPart).text = eventInfoPart

            view.findViewById<Button>(R.id.manageEventButton).setOnClickListener {
                // TODO: перейти на EventDetailFragment с event.id
                if (event != null) {
                    // navigation logic here
                } else {
                    // можно показать тост или ничего не делать
                }
            }
        }

        // Кнопки
        view.findViewById<FrameLayout>(R.id.actionCreate).setOnClickListener {
            (activity as? MainActivity)?.openFragment(CreateEventFragment())
        }

        view.findViewById<FrameLayout>(R.id.actionEvents).setOnClickListener {
            // TODO: открыть MyEventsFragment
        }

        view.findViewById<FrameLayout>(R.id.actionTeams).setOnClickListener {
            // TODO: открыть TeamsFragment
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
