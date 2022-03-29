package com.example.rickandmorty.ui.episodes

import androidx.lifecycle.*
import androidx.paging.PagingData
import com.example.rickandmorty.data.episodes.EpisodeRepository
import com.example.rickandmorty.model.CharacterData
import com.example.rickandmorty.model.EpisodeData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class EpisodeViewModel(private val repository: EpisodeRepository): ViewModel() {

    private val _queries = MutableLiveData<MutableMap<String, String>>()
    val queries: LiveData<MutableMap<String, String>> = _queries

    init {
        _queries.value = mutableMapOf(
            "name" to "",
            "episode" to ""
        )
    }

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

    fun requestEpisodeCharacters(characterUrlList: List<String>): LiveData<List<CharacterData>>? {
        viewModelScope.launch {
            episodeCharacters = repository.getEpisodeCharacters(characterUrlList)
        }
        return episodeCharacters
    }

}


class EpisodeViewModelFactory(private val repository: EpisodeRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EpisodeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EpisodeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}