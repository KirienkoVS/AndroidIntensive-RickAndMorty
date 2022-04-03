package com.example.rickandmorty.api

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
    ): List<CharacterInfo>


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
}