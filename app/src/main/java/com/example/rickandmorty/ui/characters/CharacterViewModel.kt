package com.example.rickandmorty.ui.characters

import androidx.lifecycle.*
import androidx.paging.PagingData
import com.example.rickandmorty.data.characters.CharacterRepository
import com.example.rickandmorty.model.CharacterData
import com.example.rickandmorty.model.EpisodeData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CharacterViewModel(private val repository: CharacterRepository): ViewModel() {

    private val _queries = MutableLiveData<MutableMap<String, String>>()
    val queries: LiveData<MutableMap<String, String>> = _queries

    init {
        _queries.value = mutableMapOf(
            "name" to "",
            "species" to "",
            "status" to "",
            "gender" to ""
        )
    }

    fun setFilter(queries: MutableMap<String, String>) {
        _queries.value = queries
    }

    fun requestCharacters(queries: Map<String, String>): Flow<PagingData<CharacterData>> {
        return repository.getCharacters(queries)/*.cachedIn(viewModelScope)*/
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

}


class CharacterViewModelFactory(private val repository: CharacterRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CharacterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CharacterViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
