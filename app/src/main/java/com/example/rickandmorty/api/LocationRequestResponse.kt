package com.example.rickandmorty.api

import com.example.rickandmorty.model.LocationData

data class LocationRequestResponse(
    val results: List<LocationData>,
    val info: LocationInfo
)

//data class LocationResponse(
//    val id: Int,
//    val name: String,
//    val air_date: String,
//    val episode: String,
//    val characters: List<String>,
//    val url: String,
//    val created: String
//)

data class LocationInfo(
    val next: String?,
    val prev: String?
)