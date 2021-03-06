package com.example.rickandmorty.ui.characters

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.example.rickandmorty.data.ResponseResult
import com.example.rickandmorty.data.characters.CharacterRepository
import com.example.rickandmorty.model.CharacterData
import com.example.rickandmorty.model.EpisodeData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterViewModel @Inject constructor(private val repository: CharacterRepository): ViewModel() {

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

    fun requestCharacters(queries: Map<String, String>): Flow<PagingData<CharacterData>> {
        return repository.getCharacters(queries)
    }
    /*--------------------------------------------------------------------------------------------------------------*/
    private val _characterDetails = MutableLiveData<CharacterData>()
    val characterDetails: LiveData<CharacterData> = _characterDetails

    fun requestCharacterDetails(id: Int) {
        viewModelScope.launch {
            _characterDetails.value = repository.getCharacterDetails(id)
        }
    }
    /*--------------------------------------------------------------------------------------------------------------*/
    private val _characterEpisodes = MutableLiveData<ResponseResult<List<EpisodeData>>>()
    val characterEpisodes: LiveData<ResponseResult<List<EpisodeData>>> = _characterEpisodes

    fun requestCharacterEpisodes(episodeUrlList: List<String>) {
        viewModelScope.launch {
            _characterEpisodes.value = repository.getCharacterEpisodes(episodeUrlList)
        }
    }
    /*--------------------------------------------------------------------------------------------------------------*/
    fun searchCharacters(query: String): Flow<PagingData<CharacterData>> {
        return repository.searchCharacters(query)
    }
}
