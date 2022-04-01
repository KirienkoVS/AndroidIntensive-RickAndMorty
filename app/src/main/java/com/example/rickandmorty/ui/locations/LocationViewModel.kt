package com.example.rickandmorty.ui.locations

import androidx.lifecycle.*
import androidx.paging.PagingData
import com.example.rickandmorty.data.locations.LocationRepository
import com.example.rickandmorty.model.CharacterData
import com.example.rickandmorty.model.LocationData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class LocationViewModel(private val repository: LocationRepository): ViewModel() {

    private val _queries = MutableLiveData<MutableMap<String, String>>()
    val queries: LiveData<MutableMap<String, String>> = _queries

    init {
        _queries.value = mutableMapOf(
            "name" to "",
            "type" to "",
            "dimension" to ""
        )
    }

    fun setFilter(queries: MutableMap<String, String>) {
        _queries.value = queries
    }

    fun requestLocations(queries: Map<String, String>): Flow<PagingData<LocationData>> {
        return repository.getLocations(queries)
    }


    private var locationDetails: LiveData<LocationData>? = null

    fun requestLocationDetails(id: Int, name: String): LiveData<LocationData>? {
        viewModelScope.launch {
            locationDetails = repository.getLocationDetails(id, name)
        }
        return locationDetails
    }


    private var locationCharacters: LiveData<List<CharacterData>>? = null

    fun requestLocationCharacters(characterUrlList: List<String>, isOnline: Boolean): LiveData<List<CharacterData>>? {
        viewModelScope.launch {
            locationCharacters = repository.getLocationResidents(characterUrlList, isOnline)
        }
        return locationCharacters
    }

}


class LocationViewModelFactory(private val repository: LocationRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}