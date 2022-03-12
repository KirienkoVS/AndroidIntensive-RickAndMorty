package com.example.rickandmorty.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.rickandmorty.api.RickAndMortyApi
import com.example.rickandmorty.model.CharacterData
import com.example.rickandmorty.ui.characters.CharacterViewModel.Companion.PAGE_SIZE
import retrofit2.HttpException
import java.io.IOException

class CharacterPagingSource(private val api: RickAndMortyApi): PagingSource<Int, CharacterData>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CharacterData> {
        val page = params.key ?: FIRST_PAGE_INDEX

        return try {
            val response = api.requestCharacters(page)
            val charactersList = response.results

            val nextKey = if (charactersList.isEmpty()) {
                null
            } else {
                page + (params.loadSize / PAGE_SIZE)
            }
            LoadResult.Page(
                data = charactersList,
                prevKey = if (page == FIRST_PAGE_INDEX) null else page - 1,
                nextKey = nextKey
            )
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, CharacterData>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    companion object {
        private const val FIRST_PAGE_INDEX = 1
    }
}
