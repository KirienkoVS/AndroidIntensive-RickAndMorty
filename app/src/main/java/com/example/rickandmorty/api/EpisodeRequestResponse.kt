package com.example.rickandmorty.api

data class EpisodeRequestResponse(
    val results: List<EpisodeResponse>,
    val info: EpisodeInfo
)

data class EpisodeResponse(
    val id: Int,
    val name: String,
    val air_date: String,
    val episode: String,
    val characters: List<String>,
    val url: String,
    val created: String
)

data class EpisodeInfo(
    val next: String?,
    val prev: String?
)