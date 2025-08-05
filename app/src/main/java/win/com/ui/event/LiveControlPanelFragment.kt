package win.com.ui.event

import androidx.lifecycle.Observer
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import win.com.MainActivity
import win.com.R
import win.com.data.database.AppDatabase
import win.com.data.repository.EventRepository
import win.com.ui.result.ResultsFragment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class LiveControlPanelFragment : Fragment() {

    private var eventId: Int = -1
    private lateinit var playerRowsContainer: LinearLayout
    private lateinit var repository: EventRepository
    private lateinit var eventTitleAndDateView : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventId = arguments?.getInt("event_id") ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_live_control_panel, container, false)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        playerRowsContainer = view.findViewById(R.id.playerRowsContainer)
        eventTitleAndDateView = view.findViewById(R.id.eventTitleAndDate)

        repository = EventRepository(
            dao = AppDatabase.getDatabase(requireContext()).eventDao(),
            participantDao = AppDatabase.getDatabase(requireContext()).participantDao(),
            teamParticipantDao = AppDatabase.getDatabase(requireContext()).teamParticipantDao(),
            resultDao = AppDatabase.getDatabase(requireContext()).liveResultDao()
        )

        val backButton = view.findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            (activity as? MainActivity)?.openFragment(AllEventsFragment())
        }

        repository.getById(eventId).observe(viewLifecycleOwner) { event ->
            if (event != null) {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                val parsedDate: Date? = try {
                    inputFormat.parse(event.date)
                } catch (e: Exception) {
                    null
                }

                val formattedDate = parsedDate?.let { outputFormat.format(it) } ?: event.date

                eventTitleAndDateView.text = "${event.name} | $formattedDate"
            } else {
                eventTitleAndDateView.text = "Unknown Event"
            }
        }

        repository.getParticipantsByEventId(eventId).observe(viewLifecycleOwner) { participants ->
            playerRowsContainer.removeAllViews()
            participants.forEach { participant ->
                addPlayerRow(
                    player = participant.nickname,
                    role = participant.role,
                    lapTime = participant.lapTime ?: "",   // или "" если null
                    result = if (participant.pos.isNullOrEmpty()) "-" else "Finished",
                    pos = participant.pos ?: "-"
                )
            }
            updateLastRowBackground()
        }

        view.findViewById<Button>(R.id.buttonResetAll).setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Reset All")
                .setMessage("Are you sure you want to reset all results?")
                .setPositiveButton("Yes") { _, _ ->
                    repository.getParticipantsByEventId(eventId).observe(viewLifecycleOwner) { participants ->
                        playerRowsContainer.removeAllViews()
                        participants.forEach { p ->
                            addPlayerRow(p.nickname, p.role, "", "", "")
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        view.findViewById<Button>(R.id.buttonFinishEvent).setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Finish Event")
                .setMessage("Are you sure you want to finish this event?")
                .setPositiveButton("Yes") { _, _ ->
                    lifecycleScope.launch {
                        // Обновляем статус события
                        val event = repository.getEventByIdNow(eventId)
                        if (event != null) {
                            repository.update(event.copy(status = "FINISHED"))
                            if (isAdded) {
                                (activity as? MainActivity)?.openFragment(ResultsFragment.newInstance())
                            }
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        view.findViewById<Button>(R.id.buttonSaveScreenshot).setOnClickListener {
            val bitmap = view.findViewById<LinearLayout>(R.id.livePanelRoot).drawToBitmap()
            saveBitmapToGallery(bitmap)
        }

        view.findViewById<Button>(R.id.buttonExportResults).setOnClickListener {
            Toast.makeText(requireContext(), "PDF export is not implemented yet", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun addPlayerRow(player: String, role: String, lapTime: String, result: String, pos: String) {
        val row = layoutInflater.inflate(R.layout.item_player_row, playerRowsContainer, false) as LinearLayout

        val playerName = row.findViewById<TextView>(R.id.textPlayer)
        val roleView = row.findViewById<TextView>(R.id.textRole)
        val timeView = row.findViewById<TextView>(R.id.textLapTime)
        val resultView = row.findViewById<TextView>(R.id.textResult)
        val posView = row.findViewById<TextView>(R.id.textPosition)

        val btnTime = row.findViewById<ImageButton>(R.id.buttonAddTime)
        val btnFinish = row.findViewById<ImageButton>(R.id.buttonFinishPlayer)

        playerName.text = player
        roleView.text = role
        timeView.text = lapTime
        resultView.text = result
        posView.text = pos

        btnTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val dialog = TimePickerDialog(
                ContextThemeWrapper(requireContext(), R.style.CustomTimePickerDialogTheme1),
                { _, h, m ->
                    val formatted = String.format("%02d:%02d", h, m)
                    timeView.text = formatted

                    lifecycleScope.launch {
                        repository.updateLapTime(eventId, playerName.text.toString(), formatted)
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            dialog.show()
        }

        btnFinish.setOnClickListener {
            repository.getParticipantsByEventId(eventId).observeOnce(viewLifecycleOwner) { participants ->
                val positions = (1..participants.size).map { it.toString() }.toTypedArray()
                var selectedPositionIndex = -1

                AlertDialog.Builder(requireContext())
                    .setTitle("Select Final Position")
                    .setSingleChoiceItems(positions, selectedPositionIndex) { _, which ->
                        selectedPositionIndex = which
                    }
                    .setPositiveButton("OK") { dialog, _ ->
                        if (selectedPositionIndex != -1) {
                            val posInput = positions[selectedPositionIndex]
                            posView.text = posInput
                            resultView.text = "Finished"

                            when (posInput) {
                                "1" -> posView.setTextColor(Color.YELLOW)
                                "2" -> posView.setTextColor(Color.LTGRAY)
                                "3" -> posView.setTextColor(Color.rgb(205, 127, 50))
                                else -> posView.setTextColor(Color.WHITE)
                            }

                            lifecycleScope.launch {
                                repository.updatePosition(eventId, playerName.text.toString(), posInput)
                            }
                        } else {
                            Toast.makeText(requireContext(), "Please select a position", Toast.LENGTH_SHORT).show()
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }

        playerRowsContainer.addView(row)
    }

    fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }

    private fun updateLastRowBackground() {
        val childCount = playerRowsContainer.childCount
        if (childCount > 0) {
            // Сбросить фон у всех строк в обычный
            for (i in 0 until childCount) {
                val row = playerRowsContainer.getChildAt(i)
                row.setBackgroundResource(R.drawable.bg_player_row)
            }
            // Установить фон с закруглением у последней строки
            val lastRow = playerRowsContainer.getChildAt(childCount - 1)
            lastRow.setBackgroundResource(R.drawable.bg_player_row_rounded_bottom)
        }
    }

    private fun saveBitmapToGallery(bitmap: Bitmap) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val filename = "live_table_$timeStamp.png"
        val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "LiveEvents")
        dir.mkdirs()
        val file = File(dir, filename)
        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            Toast.makeText(requireContext(), "Saved to gallery: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun newInstance(eventId: Int): LiveControlPanelFragment {
            val fragment = LiveControlPanelFragment()
            val args = Bundle()
            args.putInt("event_id", eventId)
            fragment.arguments = args
            return fragment
        }
    }
}
