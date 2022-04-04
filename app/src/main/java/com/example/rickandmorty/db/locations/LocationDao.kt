package com.example.rickandmorty.db.locations

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rickandmorty.model.LocationData

@Dao
interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locationData: List<LocationData>)

    @Query("SELECT * FROM locations WHERE " +
            "(name LIKE :name OR :name IS NULL) AND " +
            "(type LIKE :type OR :type IS NULL) AND " +
            "(dimension LIKE :dimension OR :dimension IS NULL)")
    fun locationsByFilter(name: String?, type: String?, dimension: String?): PagingSource<Int, LocationData>

    @Query("SELECT * FROM locations WHERE id = :id OR name = :name")
    fun getLocationDetails(id: Int, name: String): LiveData<LocationData>

    @Query("DELETE FROM locations")
    suspend fun clearLocations()

    @Query("SELECT * FROM locations WHERE " +
            "(name LIKE :query OR :query IS NULL) OR " +
            "(type LIKE :query OR :query IS NULL) OR " +
            "(dimension LIKE :query OR :query IS NULL)")
    fun locationsBySearch(query: String?): PagingSource<Int, LocationData>

}