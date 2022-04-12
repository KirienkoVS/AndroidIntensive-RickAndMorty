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

    private val _queries = MutableLiveData<MutableMap<String, String>>(mutableMapOf("isRefresh" to "true"))
    val queries: LiveData<MutableMap<String, String>> = _queries

    fun setFilter(queries: MutableMap<String, String>) {
        _queries.value = queries
    }

    fun requestEpisodes(queries: Map<String, String>): Flow<PagingData<EpisodeData>> {
        return repository.getEpisodes(queries)
    }


    private var episodeDetails: LiveData<EpisodeData>? = null

    fun requestEpisodeDetails(id: Int): LiveData<EpisodeData>? {
        viewModelScope.launch {
            episodeDetails = repository.getEpisodeDetails(id)
        }
        return episodeDetails
    }


    private var episodeCharacters: LiveData<List<CharacterData>>? = null

    fun requestEpisodeCharacters(characterUrlList: List<String>, isOnline: Boolean): LiveData<List<CharacterData>>? {
        viewModelScope.launch {
            episodeCharacters = repository.getEpisodeCharacters(characterUrlList, isOnline)
        }
        return episodeCharacters
    }

    fun searchEpisodes(query: String): Flow<PagingData<EpisodeData>> {
        return repository.searchEpisodes(query)
    }

}
