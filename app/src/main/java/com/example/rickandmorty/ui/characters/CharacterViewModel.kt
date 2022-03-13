package com.example.rickandmorty.ui.characters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.rickandmorty.data.CharacterRepository
import com.example.rickandmorty.model.CharacterData
import kotlinx.coroutines.flow.Flow

class CharacterViewModel(private val repository: CharacterRepository): ViewModel() {

    fun requestCharacters(): Flow<PagingData<CharacterData>> {
        return repository.requestCharacters().cachedIn(viewModelScope)
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
