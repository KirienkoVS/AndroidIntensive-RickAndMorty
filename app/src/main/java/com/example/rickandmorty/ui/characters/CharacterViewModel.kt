package com.example.rickandmorty.ui.characters

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.rickandmorty.data.CharacterRepository
import com.example.rickandmorty.model.CharacterData
import kotlinx.coroutines.flow.Flow

class CharacterViewModel(private val repository: CharacterRepository): ViewModel() {

    private val _filter = MutableLiveData("")
    val filter: LiveData<String> = _filter

    private var filterGroup: String = ""

    fun setFilter(filter: String, filterGroup: String) {
        _filter.value = filter
        this.filterGroup = filterGroup
    }

    fun requestCharacters(filter: String, filterGroup: String): Flow<PagingData<CharacterData>> {
        return repository.getCharacters(filter, filterGroup).cachedIn(viewModelScope)
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
