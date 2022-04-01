package com.example.rickandmorty.data.characters

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.rickandmorty.api.RickAndMortyApi
import com.example.rickandmorty.db.AppDatabase
import com.example.rickandmorty.db.characters.CharacterRemoteKeys
import com.example.rickandmorty.model.CharacterData
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class CharacterRemoteMediator(
    private var queries: Map<String, String>,
    private val api: RickAndMortyApi,
    private val database: AppDatabase
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
                /*// If remoteKeys is null, that means the refresh result is not in the database yet.
                // We can return Success with endOfPaginationReached = false because Paging
                // will call this method again if RemoteKeys becomes non-null.
                // If remoteKeys is NOT NULL but its nextKey is null, that means we've reached
                // the end of pagination for append.*/
                remoteKeys?.nextKey ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
            }
        }

        try {

            val response = api.requestCharacters(
                name = queries.get("name"),
                species = queries.get("species"),
                status = queries.get("status"),
                gender = queries.get("gender"),
                page
            )

            val charactersInfo = response.results
            val endOfPaginationReached = response.info.next == null

            database.withTransaction {

                if (loadType == LoadType.REFRESH) {
                    database.characterRemoteKeysDao().clearRemoteKeys()
                    database.characterDao().clearCharacters()
                }

                val prevKey = if (page == FIRST_PAGE_INDEX) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1

                val keys = charactersInfo.map {
                    CharacterRemoteKeys(characterId = it.id, prevKey = prevKey, nextKey = nextKey)
                }

                val characters = charactersInfo.map {
                    CharacterData(
                        id = it.id, name = it.name, species = it.species, status = it.status, gender = it.gender,
                        image = it.image, type = it.type, created = it.created, originName = it.origin.name,
                        locationName = it.location.name, episode = it.episode
                    )
                }

                database.characterRemoteKeysDao().insertKeys(keys)
                database.characterDao().insertCharacters(characters)
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    // LoadType.REFRESH
    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, CharacterData>): CharacterRemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { characterId ->
                database.characterRemoteKeysDao().characterIdRemoteKeys(characterId)
            }
        }
    }

    // LoadType.PREPEND
    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, CharacterData>): CharacterRemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { character ->
                // Get the remote keys of the first items retrieved
                database.characterRemoteKeysDao().characterIdRemoteKeys(character.id)
            }
    }

    // LoadType.APPEND
    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, CharacterData>): CharacterRemoteKeys? {
        // Get the last page that was retrieved, that contained items. From that last page, get the last item.
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { character ->
                database.characterRemoteKeysDao().characterIdRemoteKeys(character.id)
            }
    }

    companion object {
        private const val FIRST_PAGE_INDEX = 1
    }
}
