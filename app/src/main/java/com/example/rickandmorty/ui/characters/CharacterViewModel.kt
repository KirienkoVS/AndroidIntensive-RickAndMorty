package com.example.rickandmorty.ui.characters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.rickandmorty.api.RickAndMortyApi
import com.example.rickandmorty.data.CharacterPagingSource
import com.example.rickandmorty.model.CharacterData
import kotlinx.coroutines.flow.Flow

class CharacterViewModel: ViewModel() {

    private var api: RickAndMortyApi = RickAndMortyApi.create()

    fun requestCharacters(): Flow<PagingData<CharacterData>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = { CharacterPagingSource(api) }
        ).flow.cachedIn(viewModelScope)
    }

    companion object {
        const val PAGE_SIZE = 20
    }

}
