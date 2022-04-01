package com.example.rickandmorty.ui.locations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
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
    private lateinit var created: TextView

    private var locationID = 0
    private var locationName = ""
    private var isOnline = true
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = LocationDetailsFragmentBinding.inflate(inflater, container, false)

        isOnline = Injection.isOnline(requireContext())
        arguments?.apply {
            locationID = this.getInt(LOCATION_ID)
            locationName = this.getString(LOCATION_NAME) ?: error("Should provide location name")
        }

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
            created = locationCreated
            recyclerView = locationResidentsRecyclerview
        }
    }

    private fun setViews() {
        viewModel.requestLocationDetails(locationID, locationName)?.let { locationLiveData ->
            locationLiveData.observe(viewLifecycleOwner) { location->
                if (location == null) {
                    Toast.makeText(activity, "Data not available!", Toast.LENGTH_LONG).show()
                } else {
                    id.text = location.id.toString()
                    name.text = location.name
                    type.text = location.type.ifBlank { "unknown" }
                    dimension.text = location.dimension.ifBlank { "unknown" }
                    created.text = location.created.subSequence(0, 10)
                }
            }
        }
    }

    private fun setRecyclerView() {
        viewModel.requestLocationDetails(locationID, locationName)?.let { locationLiveData ->
            locationLiveData.observe(viewLifecycleOwner) { location ->
                if (location == null) {
                    Toast.makeText(activity, "Data not available!", Toast.LENGTH_LONG).show()
                } else {
                    val residentsUrlList = location.residents
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