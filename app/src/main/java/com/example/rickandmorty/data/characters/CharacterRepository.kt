package com.example.rickandmorty.data.characters

import android.database.sqlite.SQLiteException
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.*
import com.example.rickandmorty.api.RickAndMortyApi
import com.example.rickandmorty.db.AppDatabase
import com.example.rickandmorty.model.CharacterData
import com.example.rickandmorty.model.EpisodeData
import com.example.rickandmorty.model.LocationData
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class CharacterRepository @Inject constructor(
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

    fun getCharacterEpisodes(episodeUrlList: List<String>, isOnline: Boolean): LiveData<List<EpisodeData>> {
        var apiQuery = ""
        val dbQuery = mutableListOf<Int>()

        episodeUrlList.forEach { episodeUrl ->
            val episodeID = episodeUrl.substringAfterLast("/").toInt()
            apiQuery += "$episodeID,"
            dbQuery.add(episodeID)
        }

        return if (isOnline) {
            try {
                liveData {
                    val apiResponse = api.requestSingleEpisode(apiQuery).map {
                        EpisodeData(id = it.id, name = it.name, airDate = it.air_date, episodeNumber = it.episode,
                            characters = it.characters, url = it.url, created = it.created)
                    }
                    database.episodeDao().insertEpisodes(apiResponse) // does it need?
                    emit(apiResponse)
                }
            } catch (exception: IOException) {
                error (exception)
            } catch (exception: HttpException) {
                error (exception)
            }
        } else {
            try {
                database.episodeDao().getCharacterEpisodes(dbQuery)
            } catch (exception: SQLiteException) {
                error (exception)
            }
        }
    }

    suspend fun getCharacterLocation(location: String, origin: String, isOnline: Boolean) {
        if (isOnline) {
            val locationResponse = api.requestLocations(location, type = "", dimension = "", page = 0).results.map {
                LocationData(id = it.id, name = it.name, type = it.type, dimension = it.dimension,
                residents = it.residents, url = it.url, created = it.created)
            }
            database.locationDao().insertLocations(locationResponse)

            val originResponse = api.requestLocations(origin, type = "", dimension = "", page = 0).results.map {
                LocationData(id = it.id, name = it.name, type = it.type, dimension = it.dimension,
                    residents = it.residents, url = it.url, created = it.created)
            }
            database.locationDao().insertLocations(originResponse)
        }
    }

    fun searchCharacters(query: String): Flow<PagingData<CharacterData>> {

        val dbQuery = if (query.isBlank()) {
            "empty"
        } else {
            "%${query}%"
        }

        fun pagingSourceFactory(): () -> PagingSource<Int, CharacterData> {
            return { database.characterDao().charactersBySearch(query = if (dbQuery == "empty") null else dbQuery)
            }
        }

        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = pagingSourceFactory(),
//            remoteMediator = CharacterRemoteMediator(queries, api, database) // don`t know how implement this
        ).flow

    }

    companion object {
        const val PAGE_SIZE = 20
    }
}