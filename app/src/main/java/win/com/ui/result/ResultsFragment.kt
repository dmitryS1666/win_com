package win.com.ui.result

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import win.com.MainActivity
import win.com.R
import win.com.ui.event.AllEventsFragment
import win.com.ui.event.ResultDetailsFragment
import win.com.viewmodel.ResultsViewModel

class ResultsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchInput: EditText
    private lateinit var backButton: ImageView
    private lateinit var viewModel: ResultsViewModel
    private lateinit var adapter: ResultsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_results, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Инициализация вью
        recyclerView = view.findViewById(R.id.resultsRecyclerView)
        searchInput = view.findViewById(R.id.searchInput)
        backButton = view.findViewById(R.id.backButton)

        adapter = ResultsAdapter(mutableListOf()) { event ->
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.mainFragmentContainer, ResultDetailsFragment.newInstance(event.id))
                .addToBackStack(null)
                .commit()
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(this)[ResultsViewModel::class.java]

        viewModel.finishedEvents.observe(viewLifecycleOwner) { events ->
            adapter.submitList(events ?: emptyList())  // null-safe вызов
        }

        // Аналогично для participants
        viewModel.participants.observe(viewLifecycleOwner) { list ->
            adapter.updateParticipants(list ?: emptyList())
        }

        // Поиск
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.setFilter(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    companion object {
        fun newInstance() = ResultsFragment()
    }
}
