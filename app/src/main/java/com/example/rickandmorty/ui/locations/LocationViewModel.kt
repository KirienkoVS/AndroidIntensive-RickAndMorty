package com.example.rickandmorty.ui.locations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.example.rickandmorty.data.locations.LocationRepository
import com.example.rickandmorty.model.CharacterData
import com.example.rickandmorty.model.LocationData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(private val repository: LocationRepository): ViewModel() {

    private val _isProgressBarVisible = MutableLiveData(true)
    val isProgressBarVisible: LiveData<Boolean> = _isProgressBarVisible

    fun setProgressBarVisibility(isVisible: Boolean) {
        _isProgressBarVisible.value = isVisible
    }

    private val _queries = MutableLiveData<MutableMap<String, String>>(mutableMapOf())
    val queries: LiveData<MutableMap<String, String>> = _queries

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

    fun searchLocations(query: String): Flow<PagingData<LocationData>> {
        return repository.searchLocations(query)
    }

}
