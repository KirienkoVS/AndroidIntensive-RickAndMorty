package com.example.rickandmorty.data.characters

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.*
import com.example.rickandmorty.api.RickAndMortyApi
import com.example.rickandmorty.db.AppDatabase
import com.example.rickandmorty.model.CharacterData
import com.example.rickandmorty.model.EpisodeData
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalPagingApi::class)
class CharacterRepository(
    private val api: RickAndMortyApi,
    private val database: AppDatabase
) {

    fun getCharacters(queries: Map<String, String>): Flow<PagingData<CharacterData>> {

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

    fun getCharacterDetails(id: Int): LiveData<CharacterData> {
        return database.characterDao().getCharacterDetails(id)
    }

    suspend fun getCharacterEpisodes(episodeUrlList: List<String>): LiveData<List<EpisodeData>> {
        var query = ""

        episodeUrlList.forEach { episodeUrl ->
            val episodeNumber = episodeUrl.substringAfterLast("/")
            query += "$episodeNumber,"
        }
        
        val episodeData: LiveData<List<EpisodeData>> = liveData {
            val response = api.requestSingleEpisode(query).map {
                EpisodeData(id = it.id, name = it.name, airDate = it.air_date, episodeNumber = it.episode,
                    characters = it.characters, url = it.url, created = it.created)
            }
            emit(response)
        }

        return episodeData
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}