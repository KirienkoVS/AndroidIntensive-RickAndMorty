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
import com.example.rickandmorty.databinding.LocationDetailsFragmentBinding

class LocationDetailsFragment : Fragment() {

    private var _binding: LocationDetailsFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LocationViewModel

    private lateinit var id: TextView
    private lateinit var name: TextView
    private lateinit var type: TextView
    private lateinit var dimension: TextView
    private lateinit var url: TextView
    private lateinit var created: TextView

    private var locationID = 0
    private var isOnline = true
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = LocationDetailsFragmentBinding.inflate(inflater, container, false)

        isOnline = Injection.isOnline(requireContext())
        locationID = arguments?.getInt(LOCATION_ID) ?: error("Should provide location ID")

        initializeViewModel()
        bindViews()
        setViews()
        setRecyclerView()

        return binding.root
    }

    private fun initializeViewModel() {
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
            url = locationUrl
            created = locationCreated
            recyclerView = locationResidentsRecyclerview
        }
    }

    private fun setViews() {
        viewModel.requestLocationDetails(locationID)?.let {
            it.observe(viewLifecycleOwner) { location->
                id.text = location.id.toString()
                name.text = location.name
                type.text = location.type.ifBlank { "unknown" }
                dimension.text = location.dimension.ifBlank { "unknown" }
                url.text = location.url
                created.text = location.created.subSequence(0, 10)
            }
        }
    }

    private fun setRecyclerView() {
        viewModel.requestLocationDetails(locationID)?.let { locationLiveData ->
            locationLiveData.observe(viewLifecycleOwner) { locationData ->
                val residentsUrlList = locationData.residents

                viewModel.requestLocationCharacters(residentsUrlList, isOnline)?.let { characterLiveData ->
                    characterLiveData.observe(viewLifecycleOwner) { characterDataList ->
                        val recyclerViewAdapter = LocationDetailsAdapter()
                        recyclerViewAdapter.residentsList = characterDataList
                        recyclerView.adapter = recyclerViewAdapter
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
    }
}