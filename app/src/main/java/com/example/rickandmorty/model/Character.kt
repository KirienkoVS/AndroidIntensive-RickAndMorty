package com.example.rickandmorty.model

data class CharacterRequestResponse(
    val results: List<CharacterData>,
    val info: CharacterRequestInfo
)
data class CharacterData(
    val name: String,
    val species: String,
    val status: String,
    val gender: String,
    val image: String
)
data class CharacterRequestInfo(
    val count: Int,
    val pages: Int,
    val next: String?,
    val prev: String?,
)
