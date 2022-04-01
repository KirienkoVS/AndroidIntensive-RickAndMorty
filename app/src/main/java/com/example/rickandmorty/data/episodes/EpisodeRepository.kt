package com.example.rickandmorty.data.episodes

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.*
import com.example.rickandmorty.api.RickAndMortyApi
import com.example.rickandmorty.db.AppDatabase
import com.example.rickandmorty.model.CharacterData
import com.example.rickandmorty.model.EpisodeData
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalPagingApi::class)
class EpisodeRepository(
    private val api: RickAndMortyApi,
    private val database: AppDatabase
) {

    fun getEpisodes(queries: Map<String, String>): Flow<PagingData<EpisodeData>> {

        val name = if (queries.get("name").isNullOrBlank()) {
            "empty"
        } else "%${queries.get("name")}%"

        val episode = if (queries.get("episode").isNullOrBlank()) {
            "empty"
        } else "%${queries.get("episode")}%"

        fun pagingSourceFactory(): () -> PagingSource<Int, EpisodeData> {
            return { database.episodeDao().episodesByFilter(
                name = if (name == "empty") null else name,
                episode = if (episode == "empty") null else episode)
            }
        }

        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = pagingSourceFactory(),
            remoteMediator = EpisodeRemoteMediator(queries, api, database)
        ).flow

    }

    fun getEpisodeDetails(id: Int): LiveData<EpisodeData> {
        return database.episodeDao().getEpisodeDetails(id)
    }

    fun getEpisodeCharacters(characterUrlList: List<String>, isOnline: Boolean): LiveData<List<CharacterData>> {

        var apiQuery = ""
        val dbQuery = mutableListOf<Int>()

        characterUrlList.forEach { characterUrl ->
            val characterID = characterUrl.substringAfterLast("/").toInt()
            apiQuery += "$characterID,"
            dbQuery.add(characterID)
        }

        val characterData = if (isOnline) {
            liveData {
                val apiResponse = api.requestSingleCharacter(apiQuery).map {
                    CharacterData(
                        id = it.id, name = it.name, species = it.species, status = it.status, gender = it.gender,
                        image = it.image, type = it.type, created = it.created, originName = it.origin.name,
                        locationName = it.location.name, episode = it.episode
                    )
                }
                database.characterDao().insertCharacters(apiResponse)
                emit(apiResponse)
            }
        } else {
            database.characterDao().getLocationOrEpisodeCharacters(dbQuery)
        }

        return characterData
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}