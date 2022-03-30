package com.example.rickandmorty

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModelProvider
import com.example.rickandmorty.api.RickAndMortyApi
import com.example.rickandmorty.data.characters.CharacterRepository
import com.example.rickandmorty.data.episodes.EpisodeRepository
import com.example.rickandmorty.data.locations.LocationRepository
import com.example.rickandmorty.db.AppDatabase
import com.example.rickandmorty.ui.characters.CharacterViewModelFactory
import com.example.rickandmorty.ui.episodes.EpisodeViewModelFactory
import com.example.rickandmorty.ui.locations.LocationViewModelFactory

object Injection {

    fun isOnline(context: Context): Boolean {

        // register activity with the connectivity manager service
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // returns a Network object corresponding to the currently active default data network
        val network = connectivityManager.activeNetwork ?: return false

        // representation of the capabilities of an active network.
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            // indicates this network uses a Wi-Fi transport, or WiFi has network connectivity
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

            // indicates this network uses a Cellular transport or cellular has network connectivity
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

            // else return false
            else -> false
        }
    }

    private fun provideCharacterRepository(context: Context): CharacterRepository {
        return CharacterRepository(RickAndMortyApi.create(), AppDatabase.getInstance(context))
    }

    fun provideCharacterViewModelFactory(context: Context): ViewModelProvider.Factory {
        return CharacterViewModelFactory(provideCharacterRepository(context))
    }


    private fun provideEpisodeRepository(context: Context): EpisodeRepository {
        return EpisodeRepository(RickAndMortyApi.create(), AppDatabase.getInstance(context))
    }

    fun provideEpisodeViewModelFactory(context: Context): ViewModelProvider.Factory {
        return EpisodeViewModelFactory(provideEpisodeRepository(context))
    }


    private fun provideLocationRepository(context: Context): LocationRepository {
        return LocationRepository(RickAndMortyApi.create(), AppDatabase.getInstance(context))
    }

    fun provideLocationViewModelFactory(context: Context): ViewModelProvider.Factory {
        return LocationViewModelFactory(provideLocationRepository(context))
    }
}