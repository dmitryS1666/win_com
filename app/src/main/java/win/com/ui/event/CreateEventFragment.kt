package win.com.ui.event

import win.com.R
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import win.com.MainActivity
import win.com.data.entity.EventEntity
import win.com.util.GameCategories
import win.com.util.GameModes
import win.com.viewmodel.CreateEventViewModel

class CreateEventFragment : Fragment() {

    private lateinit var viewModel: CreateEventViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_create_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[CreateEventViewModel::class.java]

        val nameInput = view.findViewById<EditText>(R.id.eventNameInput)
        val dateInput = view.findViewById<EditText>(R.id.eventDateInput)
        val timeInput = view.findViewById<EditText>(R.id.eventTimeInput)
        val categorySpinner = view.findViewById<Spinner>(R.id.categorySpinner)
        val modeSpinner = view.findViewById<Spinner>(R.id.modeSpinner)
        val roundsInput = view.findViewById<EditText>(R.id.roundsInput)
        val maxParticipantsInput = view.findViewById<EditText>(R.id.maxParticipantsInput)
        val privateCheckbox = view.findViewById<CheckBox>(R.id.privateCheckbox)
        val isPrivate = privateCheckbox.isChecked

        val createButton = view.findViewById<Button>(R.id.createButton)
        val resetButton = view.findViewById<TextView>(R.id.resetButton)
        val backButton = view.findViewById<ImageView>(R.id.backButton)

        backButton.setOnClickListener {
            // Возврат назад
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        context?.let {
            categorySpinner.adapter = ArrayAdapter(it, android.R.layout.simple_spinner_dropdown_item, GameCategories.list)
            modeSpinner.adapter = ArrayAdapter(it, android.R.layout.simple_spinner_dropdown_item, GameModes.list)
        }

        val calendar = Calendar.getInstance()

        dateInput.setOnClickListener {
            DatePickerDialog(requireContext(),
                { _, y, m, d ->
                    val date = String.format("%02d/%02d/%04d", d, m + 1, y)
                    dateInput.setText(date)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        timeInput.setOnClickListener {
            TimePickerDialog(requireContext(),
                { _, h, m ->
                    val time = String.format("%02d:%02d", h, m)
                    timeInput.setText(time)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        createButton.setOnClickListener {
            val name = nameInput.text.toString()
            val date = dateInput.text.toString()
            val time = timeInput.text.toString()
            val category = categorySpinner.selectedItem.toString()
            val mode = modeSpinner.selectedItem.toString()
            val rounds = roundsInput.text.toString().toIntOrNull() ?: 1
            val max = maxParticipantsInput.text.toString().toIntOrNull() ?: 20

            if (name.isBlank() || date.isBlank() || time.isBlank()) {
                Toast.makeText(requireContext(), "Fill all required fields!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val event = EventEntity(
                name = name,
                date = date,
                time = time,
                category = category,
                mode = mode,
                rounds = rounds,
                maxParticipants = max,
                isPrivate = isPrivate
            )

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.createEvent(event) {
                    if (!isAdded) return@createEvent
                    Toast.makeText(requireContext(), "Событие \"$name\" создано!", Toast.LENGTH_SHORT).show()
                    (activity as? MainActivity)?.openFragment(AllEventsFragment())
                }
            }
        }

        resetButton.setOnClickListener {
            nameInput.text.clear()
            dateInput.text.clear()
            timeInput.text.clear()
            roundsInput.text.clear()
            maxParticipantsInput.text.clear()
            privateCheckbox.isChecked = false
            categorySpinner.setSelection(0)
            modeSpinner.setSelection(0)
        }
    }
}
