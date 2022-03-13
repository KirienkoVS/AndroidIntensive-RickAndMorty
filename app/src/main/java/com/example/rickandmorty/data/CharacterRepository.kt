package com.example.rickandmorty.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.rickandmorty.api.RickAndMortyApi
import com.example.rickandmorty.db.CharacterDatabase
import com.example.rickandmorty.model.CharacterData
import kotlinx.coroutines.flow.Flow

class CharacterRepository(
    private val api: RickAndMortyApi,
    private val database: CharacterDatabase
) {

    @OptIn(ExperimentalPagingApi::class)
    fun requestCharacters(): Flow<PagingData<CharacterData>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = { database.characterDao().allCharacters() },
            remoteMediator = CharacterRemoteMediator(api, database)
        ).flow
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}