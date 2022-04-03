package com.example.rickandmorty

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities


internal fun isOnline(context: Context): Boolean {

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