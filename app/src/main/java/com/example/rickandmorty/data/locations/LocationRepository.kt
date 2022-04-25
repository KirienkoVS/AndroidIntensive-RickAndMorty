package com.example.rickandmorty.data.locations

import android.util.Log
import androidx.paging.*
import com.example.rickandmorty.api.RickAndMortyApi
import com.example.rickandmorty.data.ResponseResult
import com.example.rickandmorty.db.AppDatabase
import com.example.rickandmorty.model.CharacterData
import com.example.rickandmorty.model.LocationData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class LocationRepository @Inject constructor(
    private val api: RickAndMortyApi,
    private val database: AppDatabase,
) {

    fun getLocations(queries: Map<String, String>): Flow<PagingData<LocationData>> {

        val name = if (queries.get("name").isNullOrBlank()) "empty" else "%${queries.get("name")}%"
        val type = if (queries.get("type").isNullOrBlank()) "empty" else "%${queries.get("type")}%"
        val dimension = if (queries.get("dimension").isNullOrBlank()) "empty" else "%${queries.get("dimension")}%"

        fun pagingSourceFactory(): () -> PagingSource<Int, LocationData> {
            return {
                database.locationDao().locationsByFilter(
                    name = if (name == "empty") null else name,
                    type = if (type == "empty") null else type,
                    dimension = if (dimension == "empty") null else dimension
                )
            }
        }

        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = pagingSourceFactory(),
            remoteMediator = LocationRemoteMediator(queries, api, database)
        ).flow

    }

    suspend fun getLocationDetails(id: Int, name: String): ResponseResult<LocationData> {
        return try {
            val dbResponse = withContext(Dispatchers.IO) {
                database.locationDao().getLocationDetails(id, name)
            }
            if (dbResponse == null) {
                val apiResponse = api.requestLocations(name = name, type = "", dimension = "", page = 0)
                if (apiResponse.isSuccessful) {
                    apiResponse.body()?.let {
                        database.locationDao().insertLocations(it.results)
                        ResponseResult.Success(data = it.results.first())
                    } ?: ResponseResult.Error(apiResponse.message())
                } else ResponseResult.Error(apiResponse.message())
            } else {
                ResponseResult.Success(dbResponse)
            }
        } catch (exception: Exception) {
            ResponseResult.Error(exception.message)
        }
    }

    suspend fun getLocationResidents(residentUrlList: List<String>): ResponseResult<List<CharacterData>> {
        Log.d("LocationRepository", "residentUrlList: $residentUrlList")
        var apiQuery = ""
        val dbQuery = mutableListOf<Int>()

        residentUrlList.forEach { residentUrl ->
            val residentID = residentUrl.substringAfterLast("/").toInt()
            apiQuery += "$residentID,"
            dbQuery.add(residentID)
        }

        return try {
            val dbResponse = withContext(Dispatchers.IO) {
                database.characterDao().getLocationOrEpisodeCharacters(dbQuery)
            }
            Log.d("LocationRepository", "dbResponse: $dbResponse")
            if (dbResponse.isEmpty()) {
                val apiResponse = api.requestSingleCharacter(apiQuery)
                if (apiResponse.isSuccessful) {
                    apiResponse.body()?.let { response ->
                        val characterData = response.map {
                            CharacterData(
                                id = it.id, name = it.name, species = it.species, status = it.status,
                                gender = it.gender, image = it.image, type = it.type, created = it.created,
                                originName = it.origin.name, locationName = it.location.name, episode = it.episode
                            )
                        }
                        database.characterDao().insertCharacters(characterData)
                        Log.d("LocationRepository", "apiResponse: $apiResponse")
                        ResponseResult.Success(characterData)
                    } ?: ResponseResult.Error(apiResponse.message())
                } else ResponseResult.Error(apiResponse.message())
            } else {
                ResponseResult.Success(dbResponse)
            }
        } catch (exception: Exception) {
            ResponseResult.Error(exception.message)
        }
    }

    fun searchLocations(query: String): Flow<PagingData<LocationData>> {
        val dbQuery = if (query.isBlank()) "empty" else "%${query}%"
        fun pagingSourceFactory(): () -> PagingSource<Int, LocationData> {
            return {
                database.locationDao().locationsBySearch(query = if (dbQuery == "empty") null else dbQuery)
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
