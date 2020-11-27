package ru.skillbranch.skillarticles.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.MutableLiveData

class NetworkMonitor(
    context: Context // Передаем как параметр для инициализации cm: ConnectivityManager
) {
    var isConnected: Boolean = false
    val isConnectedLive = MutableLiveData(false)
    val networkTypeLive = MutableLiveData(NetworkType.NONE)

    private val cm: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun registerNetworkMonitor() {

        obtainNetworkType(cm.activeNetwork?.let { cm.getNetworkCapabilities(it) })
            .also { networkTypeLive.postValue(it) }

        cm.registerNetworkCallback( // регистрируем Callback-и
            NetworkRequest.Builder().build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onCapabilitiesChanged( // Возможности сети поменялись
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    networkTypeLive.postValue(obtainNetworkType(networkCapabilities))
                }

                override fun onLost(network: Network) { // Сеть недоступна
                    isConnected = false
                    isConnectedLive.postValue(false)
                    networkTypeLive.postValue(NetworkType.NONE)
                }

                override fun onAvailable(network: Network) { // Сеть доступна
                    isConnected = true
                    isConnectedLive.postValue(true)
                }
            }
        )
    }

    // Определяем тип сети
    private fun obtainNetworkType(networkCapabilities: NetworkCapabilities?): NetworkType = when {
        networkCapabilities == null -> NetworkType.NONE
        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
        else -> NetworkType.UNKNOWN
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun setNetworkIsConnected(isConnected: Boolean = true) {
        this.isConnected = isConnected
    }
}

enum class NetworkType { NONE, UNKNOWN, WIFI, CELLULAR}