package com.example.rickandmorty.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.rickandmorty.api.RickAndMortyApi
import com.example.rickandmorty.db.CharacterDatabase
import com.example.rickandmorty.db.RemoteKeys
import com.example.rickandmorty.model.CharacterData
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class CharacterRemoteMediator(
    private val filter: String,
    private val api: RickAndMortyApi,
    private val database: CharacterDatabase
    ) : RemoteMediator<Int, CharacterData>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, CharacterData>): MediatorResult {
        val page: Int = when(loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: FIRST_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                // If remoteKeys is null, that means the refresh result is not in the database yet.
                remoteKeys?.prevKey ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                // If remoteKeys is null, that means the refresh result is not in the database yet.
                // We can return Success with endOfPaginationReached = false because Paging
                // will call this method again if RemoteKeys becomes non-null.
                // If remoteKeys is NOT NULL but its nextKey is null, that means we've reached
                // the end of pagination for append.
                remoteKeys?.nextKey ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
            }
        }

        try {
            if (filter.isBlank()) {
                val response = api.requestCharacters("", page)
                val characters = response.results
                val endOfPaginationReached = characters.isEmpty()

                database.withTransaction {
                    // clear all tables in the database
                    if (loadType == LoadType.REFRESH) {
                        database.remoteKeysDao().clearRemoteKeys()
                        database.characterDao().clearCharacters()
                    }
                    val prevKey = if (page == FIRST_PAGE_INDEX) null else page - 1
                    val nextKey = if (endOfPaginationReached) null else page + 1
                    val keys = characters.map {
                        RemoteKeys(characterId = it.id, prevKey = prevKey, nextKey = nextKey)
                    }
                    database.remoteKeysDao().insertAll(keys)
                    database.characterDao().insertCharacters(characters)
                }
                return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
            } else {
                val response = api.requestCharacters(filter, page)
                val characters = response.results
                val endOfPaginationReached = characters.isEmpty()

                database.withTransaction {
                    // clear all tables in the database
                    if (loadType == LoadType.REFRESH) {
                        database.remoteKeysDao().clearRemoteKeys()
                        database.characterDao().clearCharacters()
                    }
                    val prevKey = if (page == FIRST_PAGE_INDEX) null else page - 1
                    val nextKey = if (endOfPaginationReached) null else page + 1
                    val keys = characters.map {
                        RemoteKeys(characterId = it.id, prevKey = prevKey, nextKey = nextKey)
                    }
                    database.remoteKeysDao().insertAll(keys)
                    database.characterDao().insertCharacters(characters)
                }
                return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
            }
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    // LoadType.REFRESH
    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, CharacterData>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { characterId ->
                database.remoteKeysDao().characterIdRemoteKeys(characterId)
            }
        }
    }

    // LoadType.PREPEND
    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, CharacterData>): RemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { character ->
                // Get the remote keys of the first items retrieved
                database.remoteKeysDao().characterIdRemoteKeys(character.id)
            }
    }

    // LoadType.APPEND
    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, CharacterData>): RemoteKeys? {
        // Get the last page that was retrieved, that contained items. From that last page, get the last item.
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { character ->
                database.remoteKeysDao().characterIdRemoteKeys(character.id)
            }
    }

    companion object {
        private const val FIRST_PAGE_INDEX = 1
    }
}
