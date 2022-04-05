package com.example.rickandmorty.ui.locations

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
import com.example.rickandmorty.databinding.LocationsFragmentBinding
import com.example.rickandmorty.initLoadStateAdapter
import com.example.rickandmorty.isOnline
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LocationsFragment : Fragment()  {

    private var _binding: LocationsFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LocationViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var pagingAdapter: LocationPagingAdapter
    private lateinit var locationFilterMap: MutableMap<String, String>
    private lateinit var emptyTextView: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout

    private var isOnline = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        locationFilterMap = mutableMapOf()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = LocationsFragmentBinding.inflate(inflater, container, false)

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
        recyclerView = binding.locationRecyclerview
        emptyTextView = binding.emptyTextView
        swipeRefresh = binding.swipeRefresh
    }

    private fun initPagingAdapter() {
        pagingAdapter = LocationPagingAdapter()
        displayLocations()
    }

    private fun initSwipeToRefresh() {
        binding.swipeRefresh.setOnRefreshListener { pagingAdapter.refresh() }
    }

    private fun displayLocations() {
        viewModel.queries.observe(viewLifecycleOwner) { queries ->
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                viewModel.requestLocations(queries).collectLatest {
                    pagingAdapter.submitData(it)
                }
            }
        }
    }

    private fun displayFoundLocations(search: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchLocations(search).collectLatest {
                pagingAdapter.submitData(it)
            }
        }
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
            displayLocations()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isNotBlank()) {
                    displayFoundLocations(newText.lowercase())
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
