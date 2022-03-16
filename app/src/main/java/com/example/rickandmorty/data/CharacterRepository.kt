package com.example.rickandmorty.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.rickandmorty.api.RickAndMortyApi
import com.example.rickandmorty.db.CharacterDatabase
import com.example.rickandmorty.model.CharacterData
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalPagingApi::class)
class CharacterRepository(
    private val api: RickAndMortyApi,
    private val database: CharacterDatabase
) {

    fun getCharacters(filter: String): Flow<PagingData<CharacterData>> {
        return if (filter.isBlank()) {
            Pager(
                config = PagingConfig(pageSize = PAGE_SIZE),
                pagingSourceFactory = { database.characterDao().getAllCharacters() },
                remoteMediator = CharacterRemoteMediator(api, database)
            ).flow
        } else {
            Pager(
                config = PagingConfig(pageSize = PAGE_SIZE),
                pagingSourceFactory = { database.characterDao().charactersByFilter(filter) },
                remoteMediator = CharacterRemoteMediator(api, database)
            ).flow
        }
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}