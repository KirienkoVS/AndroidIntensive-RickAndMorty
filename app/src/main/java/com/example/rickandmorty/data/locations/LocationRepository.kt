package com.example.rickandmorty.data.locations

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.*
import com.example.rickandmorty.api.RickAndMortyApi
import com.example.rickandmorty.db.AppDatabase
import com.example.rickandmorty.model.CharacterData
import com.example.rickandmorty.model.LocationData
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalPagingApi::class)
class LocationRepository(
    private val api: RickAndMortyApi,
    private val database: AppDatabase
) {

    fun getLocations(queries: Map<String, String>): Flow<PagingData<LocationData>> {

        val name = if (queries.get("name").isNullOrBlank()) {
            "empty"
        } else "%${queries.get("name")}%"

        val type = if (queries.get("type").isNullOrBlank()) {
            "empty"
        } else "%${queries.get("type")}%"

        val dimension = if (queries.get("dimension").isNullOrBlank()) {
            "empty"
        } else "%${queries.get("dimension")}%"

        fun pagingSourceFactory(): () -> PagingSource<Int, LocationData> {
            return { database.locationDao().locationsByFilter(
                name = if (name == "empty") null else name,
                type = if (type == "empty") null else type,
                dimension = if (dimension == "empty") null else dimension)
            }
        }

        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = pagingSourceFactory(),
            remoteMediator = LocationRemoteMediator(queries, api, database)
        ).flow

    }

    fun getLocationDetails(id: Int): LiveData<LocationData> {
        return database.locationDao().getLocationDetails(id)
    }

    suspend fun getLocationResidents(residentUrlList: List<String>, isOnline: Boolean): LiveData<List<CharacterData>> {

        var apiQuery = ""
        val dbQuery = mutableListOf<Int>()

        residentUrlList.forEach { residentUrl ->
            val residentID = residentUrl.substringAfterLast("/").toInt()
            apiQuery += "$residentID,"
            dbQuery.add(residentID)
        }

        val residentData = if (isOnline) {
            liveData {
                val apiResponse = api.requestSingleCharacter(apiQuery).map {
                    CharacterData(
                        id = it.id, name = it.name, species = it.species, status = it.status, gender = it.gender,
                        image = it.image, type = it.type, url = it.url, created = it.created, originName = "",
                        originUrl = "", locationName = "", locationUrl = "", episode = it.episode
                    )
                }
                emit(apiResponse)
            }
        } else {
            database.characterDao().getLocationOrEpisodeCharacters(dbQuery)
        }

        return residentData
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}