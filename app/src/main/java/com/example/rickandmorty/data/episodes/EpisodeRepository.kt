package com.example.rickandmorty.data.episodes

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.*
import com.example.rickandmorty.api.RickAndMortyApi
import com.example.rickandmorty.db.AppDatabase
import com.example.rickandmorty.model.CharacterData
import com.example.rickandmorty.model.EpisodeData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class EpisodeRepository @Inject constructor(
    private val api: RickAndMortyApi,
    private val database: AppDatabase
) {

    fun getEpisodes(queries: Map<String, String>): Flow<PagingData<EpisodeData>> {

        val name = if (queries.get("name").isNullOrBlank()) "empty" else "%${queries.get("name")}%"
        val episode = if (queries.get("episode").isNullOrBlank()) "empty" else "%${queries.get("episode")}%"

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

    suspend fun getEpisodeDetails(id: Int): EpisodeData {
        return withContext(Dispatchers.IO) {
            database.episodeDao().getEpisodeDetails(id)
        }
    }

    fun getEpisodeCharacters(characterUrlList: List<String>, isOnline: Boolean): LiveData<List<CharacterData>> {
        var apiQuery = ""
        val dbQuery = mutableListOf<Int>()

        characterUrlList.forEach { characterUrl ->
            val characterID = characterUrl.substringAfterLast("/").toInt()
            apiQuery += "$characterID,"
            dbQuery.add(characterID)
        }

        return if (isOnline) {
            try {
                liveData {
                    val apiResponse = api.requestSingleCharacter(apiQuery).map {
                        CharacterData(
                            id = it.id, name = it.name, species = it.species, status = it.status, gender = it.gender,
                            image = it.image, type = it.type, created = it.created, originName = it.origin.name,
                            locationName = it.location.name, episode = it.episode)
                    }
                    database.characterDao().insertCharacters(apiResponse)
                    emit(apiResponse)
                }
            } catch (exception: Exception) {
                error(exception)
            }
        } else {
            try {
                database.characterDao().getLocationOrEpisodeCharacters(dbQuery)
            } catch (exception: Exception) {
                error(exception)
            }
        }
    }

    fun searchEpisodes(query: String): Flow<PagingData<EpisodeData>> {
        val dbQuery = if (query.isBlank()) "empty" else "%${query}%"

        fun pagingSourceFactory(): () -> PagingSource<Int, EpisodeData> {
            return { database.episodeDao().episodesBySearch(query = if (dbQuery == "empty") null else dbQuery)
            }
        }

        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = pagingSourceFactory(),
        ).flow

    }

    companion object {
        const val PAGE_SIZE = 20
    }
}
