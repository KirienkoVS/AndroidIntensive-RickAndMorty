package com.example.rickandmorty.api

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RickAndMortyApi {

    @GET("character/")
    suspend fun requestCharacters(
        @Query("name") name: String?,
        @Query("species") species: String?,
        @Query("status") status: String?,
        @Query("gender") gender: String?,
        @Query("page") page: Int?
    ): CharacterRequestResponse

    @GET("character/{id}")
    suspend fun requestSingleCharacter(
        @Path("id") id: String
    ): List<CharacterInfo>//List<CharacterData>


    @GET("episode/")
    suspend fun requestEpisodes(
        @Query("name") name: String?,
        @Query("episode") episode: String?,
        @Query("page") page: Int?
    ): EpisodeRequestResponse

    @GET("episode/{id}")
    suspend fun requestSingleEpisode(
        @Path("id") id: String
    ): List<EpisodeResponse>


    @GET("location/")
    suspend fun requestLocations(
        @Query("name") name: String?,
        @Query("type") type: String?,
        @Query("dimension") dimension: String?,
        @Query("page") page: Int?
    ): LocationRequestResponse


    companion object {
        private const val BASE_URL = "https://rickandmortyapi.com/api/"

        fun create(): RickAndMortyApi {
            val logger = HttpLoggingInterceptor { Log.d("API", it) }
            logger.level = Level.BASIC

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .validateEagerly(true)
                .build()
                .create(RickAndMortyApi::class.java)
        }
    }
}