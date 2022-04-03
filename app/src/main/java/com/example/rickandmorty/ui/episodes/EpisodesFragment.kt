package com.example.rickandmorty.ui.episodes

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.example.rickandmorty.Injection
import com.example.rickandmorty.R
import com.example.rickandmorty.databinding.EpisodesFragmentBinding
import com.example.rickandmorty.ui.LoadStateAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest

class EpisodesFragment : Fragment()  {

    private var _binding: EpisodesFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: EpisodeViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var episodePagingAdapter: EpisodePagingAdapter
    private lateinit var episodeFilterMap: MutableMap<String, String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        episodeFilterMap = mutableMapOf()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = EpisodesFragmentBinding.inflate(inflater, container, false)

        initViewModel()
        initPagingAdapter()
        initSwipeToRefresh()

        return binding.root
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(
            this,
            Injection.provideEpisodeViewModelFactory(requireContext())
        ).get(EpisodeViewModel::class.java)
    }

    private fun initPagingAdapter() {
        recyclerView = binding.episodeRecyclerview
        episodePagingAdapter = EpisodePagingAdapter()

        viewModel.queries.observe(viewLifecycleOwner) { queries ->
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                viewModel.requestEpisodes(queries).collectLatest {
                    episodePagingAdapter.submitData(it)
                }
            }
        }

        val header = LoadStateAdapter { episodePagingAdapter.retry() }

        recyclerView.adapter = episodePagingAdapter.withLoadStateHeaderAndFooter(
            header = header,
            footer = LoadStateAdapter { episodePagingAdapter.retry() }
        )
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            episodePagingAdapter.loadStateFlow.collectLatest { loadStates ->
                binding.swipeRefresh.isRefreshing = loadStates.mediator?.refresh is LoadState.Loading
            }
        }
        episodePagingAdapter.addLoadStateListener { loadState ->
            val isListEmpty = loadState.refresh is LoadState.Error && episodePagingAdapter.itemCount == 0
            binding.emptyTextView.isVisible = isListEmpty
            header.loadState = loadState.mediator
                ?.refresh
                ?.takeIf { it is LoadState.Error && episodePagingAdapter.itemCount > 0 }
                ?: loadState.prepend
            recyclerView.isVisible = loadState.source.refresh is LoadState.NotLoading ||
                    loadState.mediator?.refresh is LoadState.NotLoading
            val errorState = loadState.source.append as? LoadState.Error
                ?: loadState.source.prepend as? LoadState.Error
                ?: loadState.append as? LoadState.Error
                ?: loadState.prepend as? LoadState.Error
            errorState?.let { Toast.makeText(activity, "${it.error}", Toast.LENGTH_LONG).show() }
        }
    }

    private fun initSwipeToRefresh() {
        binding.swipeRefresh.setOnRefreshListener { episodePagingAdapter.refresh() }
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
                episodePagingAdapter.refresh()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.filter_menu, menu)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}