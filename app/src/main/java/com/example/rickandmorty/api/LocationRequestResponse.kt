package com.example.rickandmorty.api

import com.example.rickandmorty.model.LocationData

data class LocationRequestResponse(
    val results: List<LocationData>,
    val info: LocationInfo
)

data class LocationInfo(
    val next: String?,
    val prev: String?
)