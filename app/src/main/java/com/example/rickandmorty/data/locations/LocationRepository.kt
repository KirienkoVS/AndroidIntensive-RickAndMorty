package com.example.rickandmorty.data.locations

import android.database.sqlite.SQLiteException
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.*
import com.example.rickandmorty.api.RickAndMortyApi
import com.example.rickandmorty.db.AppDatabase
import com.example.rickandmorty.model.CharacterData
import com.example.rickandmorty.model.LocationData
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class LocationRepository @Inject constructor(
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

    fun getLocationDetails(id: Int, name: String): LiveData<LocationData> {
        return database.locationDao().getLocationDetails(id, name)
    }

    suspend fun getLocationResidents(residentUrlList: List<String>, isOnline: Boolean): LiveData<List<CharacterData>> {
        var apiQuery = ""
        val dbQuery = mutableListOf<Int>()

        residentUrlList.forEach { residentUrl ->
            val residentID = residentUrl.substringAfterLast("/").toInt()
            apiQuery += "$residentID,"
            dbQuery.add(residentID)
        }

        return if (isOnline) {
            try {
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
            } catch (exception: IOException) {
                error(exception)
            } catch (exception: HttpException) {
                error(exception)
            }
        } else {
            try {
                database.characterDao().getLocationOrEpisodeCharacters(dbQuery)
            } catch (exception: SQLiteException) {
                error(exception)
            }
        }
    }

    fun searchLocations(query: String): Flow<PagingData<LocationData>> {

        val dbQuery = if (query.isBlank()) {
            "empty"
        } else {
            "%${query}%"
        }

        fun pagingSourceFactory(): () -> PagingSource<Int, LocationData> {
            return { database.locationDao().locationsBySearch(query = if (dbQuery == "empty") null else dbQuery)
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
