package com.example.rickandmorty.ui.locations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.example.rickandmorty.data.ResponseResult
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
    /*--------------------------------------------------------------------------------------------------------------*/
    private val _queries = MutableLiveData(mutableMapOf("isRefresh" to "true"))
    val queries: LiveData<MutableMap<String, String>> = _queries

    fun setFilter(queries: MutableMap<String, String>) {
        _queries.value = queries
    }

    fun requestLocations(queries: Map<String, String>): Flow<PagingData<LocationData>> {
        return repository.getLocations(queries)
    }
    /*--------------------------------------------------------------------------------------------------------------*/
    private val _locationDetails = MutableLiveData<ResponseResult<LocationData>>()
    val locationDetails: LiveData<ResponseResult<LocationData>> = _locationDetails

    fun requestLocationDetails(id: Int, name: String) {
        viewModelScope.launch {
            _locationDetails.value = repository.getLocationDetails(id, name)
        }
    }
    /*--------------------------------------------------------------------------------------------------------------*/
    private val _locationResidents = MutableLiveData<ResponseResult<List<CharacterData>>>()
    val locationResidents: LiveData<ResponseResult<List<CharacterData>>> = _locationResidents

    fun requestLocationResidents(characterUrlList: List<String>) {
        viewModelScope.launch {
            _locationResidents.value = repository.getLocationResidents(characterUrlList)
        }
    }
    /*--------------------------------------------------------------------------------------------------------------*/
    fun searchLocations(query: String): Flow<PagingData<LocationData>> {
        return repository.searchLocations(query)
    }
}
