package com.example.rickandmorty.ui.locations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.rickandmorty.R
import com.example.rickandmorty.data.Status
import com.example.rickandmorty.databinding.LocationDetailsFragmentBinding
import com.example.rickandmorty.isOnline
import com.example.rickandmorty.model.LocationData
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LocationDetailsFragment : Fragment() {

    private var _binding: LocationDetailsFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LocationViewModel by viewModels()

    private var isOnline = true
    private var locationID = 0
    private var locationName = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = LocationDetailsFragmentBinding.inflate(inflater, container, false)

        arguments?.apply {
            locationID = this.getInt(LOCATION_ID)
            locationName = this.getString(LOCATION_NAME) ?: error("Should provide location name")
        }

        isOnline = isOnline(requireContext())
        viewModel.requestLocationDetails(locationID, locationName)

        showProgressBar()
        displayLocationDetails()

        return binding.root
    }

    private fun displayLocationDetails() {
        viewModel.locationDetails.observe(viewLifecycleOwner) { response ->
            when (response.status) {
                Status.SUCCESS -> {
                    response.response?.let { location ->
                        setUpViews(location)
                        setUpRecyclerView(location)
                    }
                }
                Status.ERROR -> showInternetConnectionError()
            }
        }
    }

    private fun setUpViews(location: LocationData) {
        with(binding) {
            locationId.text = location.id.toString()
            locationName.text = location.name
            locationType.text = location.type.ifBlank { "unknown" }
            locationDimension.text = location.dimension.ifBlank { "unknown" }
            locationCreated.text = location.created.subSequence(0, 10)
        }
    }

    private fun setUpRecyclerView(location: LocationData) {
        val residentsUrlList = location.residents
        if (residentsUrlList.isNotEmpty()) {
            viewModel.requestLocationCharacters(residentsUrlList, isOnline)
                ?.observe(viewLifecycleOwner) { characterList ->
                    if (characterList.isNotEmpty()) {
                        val adapter = LocationDetailsAdapter()
                        adapter.residentsList = characterList
                        binding.recyclerView.adapter = adapter
                        viewModel.setProgressBarVisibility(false)
                    } else showInternetConnectionError()
                }
        } else showEmptyLocationError()
    }

    private fun showProgressBar() {
        viewModel.isProgressBarVisible.observe(viewLifecycleOwner) { isVisible ->
            when (isVisible) {
                true -> binding.progressBar.visibility = View.VISIBLE
                false -> binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showInternetConnectionError() {
        binding.emptyLocation.apply {
            visibility = View.VISIBLE
            text = resources.getString(R.string.no_results)
        }
        viewModel.setProgressBarVisibility(false)
    }

    private fun showEmptyLocationError() {
        binding.emptyLocation.apply {
            visibility = View.VISIBLE
            text = resources.getString(R.string.empty_location)
        }
        viewModel.setProgressBarVisibility(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val LOCATION_ID = "locationID"
        const val LOCATION_NAME = "locationName"
    }
}
