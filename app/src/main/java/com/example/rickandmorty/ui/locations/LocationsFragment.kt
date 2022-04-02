package com.example.rickandmorty.ui.locations

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.rickandmorty.Injection
import com.example.rickandmorty.R
import com.example.rickandmorty.databinding.LocationsFragmentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest

class LocationsFragment : Fragment()  {

    private var _binding: LocationsFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LocationViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var locationPagingAdapter: LocationPagingAdapter
    private lateinit var locationFilterMap: MutableMap<String, String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        locationFilterMap = mutableMapOf()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = LocationsFragmentBinding.inflate(inflater, container, false)

        initializeViewModel()
        setUpLocationPagingAdapter()

        return binding.root
    }

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(
            this,
            Injection.provideLocationViewModelFactory(requireContext())
        ).get(LocationViewModel::class.java)
    }

    private fun setUpLocationPagingAdapter() {
        recyclerView = binding.locationRecyclerview
        locationPagingAdapter = LocationPagingAdapter()
        recyclerView.adapter = locationPagingAdapter

        viewModel.queries.observe(viewLifecycleOwner) { queries ->
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                viewModel.requestLocations(queries).collectLatest {
                    locationPagingAdapter.submitData(it)
                }
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
        }.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.filter -> {
                showFilterDialog()
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