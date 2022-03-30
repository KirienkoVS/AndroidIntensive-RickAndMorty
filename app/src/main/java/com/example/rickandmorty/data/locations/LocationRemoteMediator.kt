package com.example.rickandmorty.data.locations

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.rickandmorty.api.RickAndMortyApi
import com.example.rickandmorty.db.AppDatabase
import com.example.rickandmorty.db.locations.LocationRemoteKeys
import com.example.rickandmorty.model.LocationData
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class LocationRemoteMediator(
    private var queries: Map<String, String>,
    private val api: RickAndMortyApi,
    private val database: AppDatabase
) : RemoteMediator<Int, LocationData>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, LocationData>): MediatorResult {
        val page: Int = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: FIRST_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                remoteKeys?.prevKey ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                remoteKeys?.nextKey ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
            }
        }

        try {

            val response = api.requestLocations(
                name = queries.get("name"),
                type = queries.get("type"),
                dimension = queries.get("dimension"),
                page
            )

            val locationsData = response.results
            Log.d("api", "locationData - ${locationsData.size}")
            val endOfPaginationReached = response.info.next == null

            database.withTransaction {

                if (loadType == LoadType.REFRESH) {
                    database.locationDao().clearLocations()
                    database.locationRemoteKeysDao().clearRemoteKeys()
                }

                val prevKey = if (page == FIRST_PAGE_INDEX) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1

                val keys = locationsData.map {
                    LocationRemoteKeys(locationId = it.id, prevKey = prevKey, nextKey = nextKey)
                }

                database.locationDao().insertLocations(locationsData)
                database.locationRemoteKeysDao().insertKeys(keys)
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    // LoadType.REFRESH
    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, LocationData>): LocationRemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { locationId ->
                database.locationRemoteKeysDao().locationIdIdRemoteKeys(locationId)
            }
        }
    }

    // LoadType.PREPEND
    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, LocationData>): LocationRemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { location ->
                database.locationRemoteKeysDao().locationIdIdRemoteKeys(location.id)
            }
    }

    // LoadType.APPEND
    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, LocationData>): LocationRemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { location ->
                database.locationRemoteKeysDao().locationIdIdRemoteKeys(location.id)
            }
    }

    companion object {
        private const val FIRST_PAGE_INDEX = 1
    }

}