package com.example.rickandmorty.data

import android.util.Log
import androidx.paging.*
import com.example.rickandmorty.api.RickAndMortyApi
import com.example.rickandmorty.db.CharacterDatabase
import com.example.rickandmorty.model.CharacterData
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalPagingApi::class)
class CharacterRepository(
    private val api: RickAndMortyApi,
    private val database: CharacterDatabase
) {

    fun getCharacters(queries: Map<String, String>): Flow<PagingData<CharacterData>> {
        Log.d("Queries ${this.javaClass.name}", "${queries.entries}")

        val name = if (queries.get("name").isNullOrBlank()) {
            "empty"
        } else {
            "%${queries.get("name")}%"
        }
        val species = if (queries.get("species").isNullOrBlank()) "empty" else queries.get("species")
        val status = if (queries.get("status").isNullOrBlank()) "empty" else queries.get("status")
        val gender = if (queries.get("gender").isNullOrBlank()) "empty" else queries.get("gender")

        fun pagingSourceFactory(): () -> PagingSource<Int, CharacterData> {
            return { database.characterDao().charactersByFilter(
                name = if (name == "empty") null else name,
                species = if (species == "empty") null else species,
                status = if (status == "empty") null else status,
                gender = if (gender == "empty") null else gender)
            }
        }

        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = pagingSourceFactory(),
            remoteMediator = CharacterRemoteMediator(queries, api, database)
        ).flow

    }

    companion object {
        const val PAGE_SIZE = 20
    }
}