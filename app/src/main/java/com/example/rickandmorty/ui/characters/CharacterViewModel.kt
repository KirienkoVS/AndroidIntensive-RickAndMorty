package com.example.rickandmorty.ui.characters

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.example.rickandmorty.data.characters.CharacterRepository
import com.example.rickandmorty.model.CharacterData
import com.example.rickandmorty.model.EpisodeData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterViewModel @Inject constructor(private val repository: CharacterRepository): ViewModel() {

    private val _queries = MutableLiveData<MutableMap<String, String>>()
    val queries: LiveData<MutableMap<String, String>> = _queries

    init {
        _queries.value = mutableMapOf(
            "name" to "",
            "species" to "",
            "status" to "",
            "gender" to "",
            "type" to ""
        )
    }

    fun setFilter(queries: MutableMap<String, String>) {
        _queries.value = queries
    }

    fun requestCharacters(queries: Map<String, String>): Flow<PagingData<CharacterData>> {
        return repository.getCharacters(queries)
    }


    private var characterDetails: LiveData<CharacterData>? = null

    fun requestCharacterDetails(id: Int): LiveData<CharacterData>? {
        viewModelScope.launch {
            characterDetails = repository.getCharacterDetails(id)
        }
        return characterDetails
    }


    private var characterEpisodes: LiveData<List<EpisodeData>>? = null

    fun requestCharacterEpisodes(episodeUrlList: List<String>, isOnline: Boolean): LiveData<List<EpisodeData>>? {
        viewModelScope.launch {
            characterEpisodes = repository.getCharacterEpisodes(episodeUrlList, isOnline)
        }
        return characterEpisodes
    }

    fun requestCharacterLocation(location: String, origin: String, isOnline: Boolean) {
        viewModelScope.launch {
            repository.getCharacterLocation(location, origin, isOnline)
        }
    }

    fun searchCharacters(query: String): Flow<PagingData<CharacterData>> {
        return repository.searchCharacters(query)
    }

}
