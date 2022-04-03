package com.example.rickandmorty.ui.locations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.rickandmorty.Injection
import com.example.rickandmorty.R
import com.example.rickandmorty.databinding.LocationDetailsFragmentBinding

class LocationDetailsFragment : Fragment() {

    private var _binding: LocationDetailsFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LocationViewModel

    private lateinit var id: TextView
    private lateinit var name: TextView
    private lateinit var type: TextView
    private lateinit var dimension: TextView
    private lateinit var created: TextView
    private lateinit var recyclerView: RecyclerView

    private var isOnline = true
    private var locationID = 0
    private var locationName = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = LocationDetailsFragmentBinding.inflate(inflater, container, false)

        isOnline = Injection.isOnline(requireContext())

        arguments?.apply {
            locationID = this.getInt(LOCATION_ID)
            locationName = this.getString(LOCATION_NAME) ?: error("Should provide location name")
        }

        initiViewModel()
        bindViews()
        setViews()
        initRecyclerView()

        return binding.root
    }

    private fun initiViewModel() {
        viewModel = ViewModelProvider(
            this,
            Injection.provideLocationViewModelFactory(requireContext())
        ).get(LocationViewModel::class.java)
    }

    private fun bindViews() {
        with(binding) {
            id = locationId
            name = locationName
            type = locationType
            dimension = locationDimension
            created = locationCreated
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
                                } else {
                                    binding.emptyLocation.apply {
                                        visibility = View.VISIBLE
                                        text = resources.getString(R.string.no_residents)
                                    }
                                }
                            }
                        }
                    } else {
                        binding.emptyLocation.visibility = View.VISIBLE
                    }
                }
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