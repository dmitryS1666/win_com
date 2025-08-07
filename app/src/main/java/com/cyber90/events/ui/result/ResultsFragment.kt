package com.cyber90.events.ui.result

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyber90.events.R
import com.cyber90.events.ui.event.ResultDetailsFragment
import com.cyber90.events.viewmodel.ResultsViewModel
import java.util.Calendar

class ResultsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var backButton: ImageView
    private lateinit var filterButton: ImageView
    private lateinit var viewModel: ResultsViewModel
    private lateinit var adapter: ResultsAdapter
    private lateinit var emptyView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_results, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Инициализация вью
        recyclerView = view.findViewById(R.id.resultsRecyclerView)
        backButton = view.findViewById(R.id.backButton)
        emptyView = view.findViewById(R.id.emptyView)

        adapter = ResultsAdapter(mutableListOf()) { event ->
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.mainFragmentContainer, ResultDetailsFragment.newInstance(event.id))
                .addToBackStack(null)
                .commit()
        }

        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        filterButton = view.findViewById(R.id.filterButton)

        filterButton.setOnClickListener {
            showFilterMenu(it)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(this)[ResultsViewModel::class.java]

        viewModel.finishedEvents.observe(viewLifecycleOwner) { events ->
            adapter.submitList(events ?: emptyList())  // null-safe вызов

            if (events.isNullOrEmpty()) {
                emptyView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }

        // Аналогично для participants
        viewModel.participants.observe(viewLifecycleOwner) { list ->
            adapter.updateParticipants(list ?: emptyList())
        }
    }

    private fun showFilterMenu(anchor: View) {
        val themedContext = ContextThemeWrapper(requireContext(), R.style.AppPopupMenu)
        val popup = PopupMenu(themedContext, anchor)
        popup.menuInflater.inflate(R.menu.filter_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.filter_by_date -> {
                    showDatePicker()
                    true
                }
                R.id.filter_by_name -> {
                    showTextInputDialog("Enter team name") { name ->
                        viewModel.setFilterByName(name)
                    }
                    true
                }
                R.id.filter_by_player -> {
                    showTextInputDialog("Enter player name") { player ->
                        viewModel.setFilterByPlayer(player)
                    }
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun showTextInputDialog(hint: String, onOk: (String) -> Unit) {
        val editText = EditText(requireContext())
        editText.hint = hint

        AlertDialog.Builder(requireContext())
            .setTitle(hint)
            .setView(editText)
            .setPositiveButton("OK") { _, _ ->
                onOk(editText.text.toString())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDatePicker() {
        val today = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.CustomDatePickerDialogTheme,
            { _, year, month, dayOfMonth ->
                val selectedDate = "%04d/%02d/%02d".format(year, month + 1, dayOfMonth)
                viewModel.setFilterByDate(selectedDate)
            },
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    companion object {
        fun newInstance() = ResultsFragment()
    }
}
