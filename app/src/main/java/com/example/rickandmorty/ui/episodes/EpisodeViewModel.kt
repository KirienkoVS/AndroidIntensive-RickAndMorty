package com.example.rickandmorty.ui.episodes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.example.rickandmorty.data.episodes.EpisodeRepository
import com.example.rickandmorty.model.CharacterData
import com.example.rickandmorty.model.EpisodeData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EpisodeViewModel @Inject constructor(private val repository: EpisodeRepository): ViewModel() {

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

    fun requestEpisodes(queries: Map<String, String>): Flow<PagingData<EpisodeData>> {
        return repository.getEpisodes(queries)
    }
    /*--------------------------------------------------------------------------------------------------------------*/
    private val _episodeDetails = MutableLiveData<EpisodeData>()
    val episodeDetails: LiveData<EpisodeData> = _episodeDetails

    fun requestEpisodeDetails(id: Int) {
        viewModelScope.launch {
            _episodeDetails.value = repository.getEpisodeDetails(id)
        }
    }
    /*--------------------------------------------------------------------------------------------------------------*/
    private var episodeCharacters: LiveData<List<CharacterData>>? = null

    fun requestEpisodeCharacters(characterUrlList: List<String>, isOnline: Boolean): LiveData<List<CharacterData>>? {
        viewModelScope.launch {
            episodeCharacters = repository.getEpisodeCharacters(characterUrlList, isOnline)
        }
        return episodeCharacters
    }
    /*--------------------------------------------------------------------------------------------------------------*/
    fun searchEpisodes(query: String): Flow<PagingData<EpisodeData>> {
        return repository.searchEpisodes(query)
    }
}
