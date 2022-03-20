package com.example.rickandmorty.ui.characters

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagingData
import com.example.rickandmorty.data.CharacterRepository
import com.example.rickandmorty.model.CharacterData
import kotlinx.coroutines.flow.Flow

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
        Log.d("Queries ${this.javaClass.name}", "${queries.entries}")
    }

    fun requestCharacters(queries: Map<String, String>): Flow<PagingData<CharacterData>> {
        Log.d("Queries ${this.javaClass.name}", "${queries.entries}")
        return repository.getCharacters(queries)/*.cachedIn(viewModelScope)*/
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
