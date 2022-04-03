package com.example.rickandmorty.ui.locations

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.example.rickandmorty.R
import com.example.rickandmorty.databinding.LocationsFragmentBinding
import com.example.rickandmorty.isOnline
import com.example.rickandmorty.ui.LoadStateAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class LocationsFragment : Fragment()  {

    private var _binding: LocationsFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LocationViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var locationPagingAdapter: LocationPagingAdapter
    private lateinit var locationFilterMap: MutableMap<String, String>

    private var isOnline = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        locationFilterMap = mutableMapOf()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = LocationsFragmentBinding.inflate(inflater, container, false)

        isOnline = isOnline(requireContext())

        initPagingAdapter()
        initSwipeToRefresh()

        return binding.root
    }

    private fun initPagingAdapter() {
        recyclerView = binding.locationRecyclerview
        locationPagingAdapter = LocationPagingAdapter()

        viewModel.queries.observe(viewLifecycleOwner) { queries ->
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                viewModel.requestLocations(queries).collectLatest {
                    locationPagingAdapter.submitData(it)
                }
            }
        }

        val header = LoadStateAdapter { locationPagingAdapter.retry() }

        recyclerView.adapter = locationPagingAdapter.withLoadStateHeaderAndFooter(
            header = header,
            footer = LoadStateAdapter { locationPagingAdapter.retry() }
        )
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            locationPagingAdapter.loadStateFlow.collectLatest { loadStates ->
                binding.swipeRefresh.isRefreshing = loadStates.mediator?.refresh is LoadState.Loading
            }
        }
        locationPagingAdapter.addLoadStateListener { loadState ->
            val isListEmpty = loadState.refresh is LoadState.Error && locationPagingAdapter.itemCount == 0

            if (isListEmpty && isOnline) {
                binding.emptyTextView.apply {
                    visibility = View.VISIBLE
                    text = resources.getString(R.string.no_match)
                }
            } else if (isListEmpty) {
                binding.emptyTextView.isVisible = isListEmpty
            } else binding.emptyTextView.visibility = View.GONE

            header.loadState = loadState.mediator
                ?.refresh
                ?.takeIf { it is LoadState.Error && locationPagingAdapter.itemCount > 0 }
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
        binding.swipeRefresh.setOnRefreshListener { locationPagingAdapter.refresh() }
    }

    @SuppressLint("InflateParams")
    private fun showFilterDialog() {
        val inflater = requireActivity().layoutInflater
        val filterLayout = inflater.inflate(R.layout.location_filter, null)
        val customTitle = inflater.inflate(R.layout.dialog_title, null)
        val nameEditText = filterLayout.findViewById<EditText>(R.id.location_name_edit_text)
        val typeEditText = filterLayout.findViewById<EditText>(R.id.location_type_edit_text)
        val dimensionEditText = filterLayout.findViewById<EditText>(R.id.location_dimension_edit_text)
        val dialog = MaterialAlertDialogBuilder(requireContext())

        val filterList = mutableListOf<EditText>(
            nameEditText,
            typeEditText,
            dimensionEditText
        )

        // restores editTexts text
        filterList.forEach { editText ->
            locationFilterMap.entries.forEach { filter ->
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
                locationFilterMap.put("name", nameEditText.text.toString())
                locationFilterMap.put("type", typeEditText.text.toString())
                locationFilterMap.put("dimension", dimensionEditText.text.toString())
                viewModel.setFilter(locationFilterMap)
            }
            setNegativeButton("Cancel") { _, _ -> }
            setNeutralButton("Clear", null)
            create().apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                        locationFilterMap.clear()
                        nameEditText.text.clear()
                        typeEditText.text.clear()
                        dimensionEditText.text.clear()
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
                locationPagingAdapter.refresh()
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
