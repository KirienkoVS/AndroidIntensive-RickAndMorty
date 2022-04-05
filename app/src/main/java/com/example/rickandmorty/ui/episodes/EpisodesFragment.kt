package com.example.rickandmorty.ui.episodes

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.rickandmorty.R
import com.example.rickandmorty.databinding.EpisodesFragmentBinding
import com.example.rickandmorty.initLoadStateAdapter
import com.example.rickandmorty.isOnline
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EpisodesFragment : Fragment()  {

    private var _binding: EpisodesFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EpisodeViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var pagingAdapter: EpisodePagingAdapter
    private lateinit var episodeFilterMap: MutableMap<String, String>
    private lateinit var emptyTextView: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout

    private var isOnline = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        episodeFilterMap = mutableMapOf()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = EpisodesFragmentBinding.inflate(inflater, container, false)

        isOnline = isOnline(requireContext())

        bindViews()
        initPagingAdapter()
        initSwipeToRefresh()
        initLoadStateAdapter(
            isOnline, emptyTextView, viewLifecycleOwner, recyclerView, activity, swipeRefresh, pagingAdapter
        )

        return binding.root
    }

    private fun bindViews() {
        recyclerView = binding.episodeRecyclerview
        emptyTextView = binding.emptyTextView
        swipeRefresh = binding.swipeRefresh
    }

    private fun initPagingAdapter() {
        pagingAdapter = EpisodePagingAdapter()
        displayEpisodes()
    }

    private fun initSwipeToRefresh() {
        binding.swipeRefresh.setOnRefreshListener { pagingAdapter.refresh() }
    }

    private fun displayEpisodes() {
        viewModel.queries.observe(viewLifecycleOwner) { queries ->
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                viewModel.requestEpisodes(queries).collectLatest {
                    pagingAdapter.submitData(it)
                }
            }
        }
    }

    private fun displayFoundEpisodes(search: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchEpisodes(search).collectLatest {
                pagingAdapter.submitData(it)
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun showFilterDialog() {
        val inflater = requireActivity().layoutInflater
        val filterLayout = inflater.inflate(R.layout.episodes_filter, null)
        val customTitle = inflater.inflate(R.layout.dialog_title, null)
        val nameEditText = filterLayout.findViewById<EditText>(R.id.episode_name_edit_text)
        val numberEditText = filterLayout.findViewById<EditText>(R.id.episode_number_edit_text)
        val dialog = MaterialAlertDialogBuilder(requireContext())

        val filterList = mutableListOf<EditText>(
            nameEditText,
            numberEditText
        )

        // restores editTexts text
        filterList.forEach { editText ->
            episodeFilterMap.entries.forEach { filter ->
                if (editText.transitionName == filter.key) {
                    editText.setText(filter.value)
                }
            }
        }

        // dialog builder
        with(dialog) {
            setView(filterLayout)
            setCustomTitle(customTitle)
            setPositiveButton("Apply") { _, _ ->
                episodeFilterMap.put("name", nameEditText.text.toString())
                episodeFilterMap.put("episode", numberEditText.text.toString())
                viewModel.setFilter(episodeFilterMap)
            }
            setNegativeButton("Cancel") { _, _ -> }
            setNeutralButton("Clear", null)
            create().apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                        episodeFilterMap.clear()
                        nameEditText.text.clear()
                        numberEditText.text.clear()
                    }
                }
            }
        }.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.filter -> {
                showFilterDialog()
                true
            }
            R.id.menu_refresh -> {
                pagingAdapter.refresh()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.filter_menu, menu)

        val searchItem = menu.findItem(R.id.search_action)
        val searchView = searchItem.actionView as SearchView
        val searchViewCloseButton = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)

        searchViewCloseButton.setOnClickListener {
            searchView.apply {
                onActionViewCollapsed()
                searchItem.collapseActionView()
            }
            displayEpisodes()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isNotBlank()) {
                    displayFoundEpisodes(newText.lowercase())
                }
                return false
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
