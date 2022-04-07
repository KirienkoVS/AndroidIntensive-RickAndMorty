package com.example.rickandmorty.ui.locations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.rickandmorty.R
import com.example.rickandmorty.databinding.LocationDetailsFragmentBinding
import com.example.rickandmorty.isOnline
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LocationDetailsFragment : Fragment() {

    private var _binding: LocationDetailsFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LocationViewModel by viewModels()

    private lateinit var id: TextView
    private lateinit var name: TextView
    private lateinit var type: TextView
    private lateinit var dimension: TextView
    private lateinit var created: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView

    private var isOnline = true
    private var locationID = 0
    private var locationName = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = LocationDetailsFragmentBinding.inflate(inflater, container, false)

        isOnline = isOnline(requireContext())

        arguments?.apply {
            locationID = this.getInt(LOCATION_ID)
            locationName = this.getString(LOCATION_NAME) ?: error("Should provide location name")
        }

        bindViews()
        setViews()
        initRecyclerView()
        showProgressBar()

        return binding.root
    }

    private fun bindViews() {
        with(binding) {
            id = locationId
            name = locationName
            type = locationType
            dimension = locationDimension
            created = locationCreated
            progressBar = residentsProgressBar
            recyclerView = locationResidentsRecyclerview
        }
    }

    private fun setViews() {
        viewModel.requestLocationDetails(locationID, locationName)?.let { locationLiveData ->
            locationLiveData.observe(viewLifecycleOwner) { location->
                if (location != null) {
                    id.text = location.id.toString()
                    name.text = location.name
                    type.text = location.type.ifBlank { "unknown" }
                    dimension.text = location.dimension.ifBlank { "unknown" }
                    created.text = location.created.subSequence(0, 10)
                } else {
                    binding.emptyLocation.apply {
                        visibility = View.VISIBLE
                        text = resources.getString(R.string.no_locations)
                    }
                    viewModel.setProgressBarVisibility(false)
                }
            }
        }
    }

    private fun initRecyclerView() {
        val recyclerViewAdapter = LocationDetailsAdapter()

        viewModel.requestLocationDetails(locationID, locationName)?.let { locationLiveData ->
            locationLiveData.observe(viewLifecycleOwner) { location ->
                if (location != null) {
                    val residentsUrlList = location.residents
                    if (residentsUrlList.isNotEmpty()) {
                        viewModel.requestLocationCharacters(residentsUrlList, isOnline)?.let { characterLiveData ->
                            characterLiveData.observe(viewLifecycleOwner) { characterList ->
                                if (characterList.isNotEmpty()) {
                                    recyclerViewAdapter.residentsList = characterList
                                    recyclerView.adapter = recyclerViewAdapter
                                    viewModel.setProgressBarVisibility(false)
                                } else {
                                    binding.emptyLocation.apply {
                                        visibility = View.VISIBLE
                                        text = resources.getString(R.string.no_residents)
                                    }
                                    viewModel.setProgressBarVisibility(false)
                                }
                            }
                        }
                    } else {
                        binding.emptyLocation.visibility = View.VISIBLE
                        viewModel.setProgressBarVisibility(false)
                    }
                }
            }
        }
    }

    private fun showProgressBar() {
        viewModel.isProgressBarVisible.observe(viewLifecycleOwner) { isVisible ->
            when(isVisible) {
                true -> progressBar.visibility = View.VISIBLE
                false -> progressBar.visibility = View.GONE
            }
        }
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
