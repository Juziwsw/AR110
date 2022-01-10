package com.hiar.ar110.network.http

import android.content.Context
import android.net.*
import android.os.Build
import com.blankj.utilcode.util.Utils
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*

/**
 * Author:wilson.chen
 * date：5/20/21
 * desc：
 */
class NetWorkManager {
    companion object {
        private var instance: NetWorkManager? = null

        fun getInstance(): NetWorkManager {
            return instance ?: synchronized(this) {
                instance ?: NetWorkManager().also { instance = it }
            }
        }
    }

    private val listeners: MutableSet<INetworkStateListener> = mutableSetOf()
    private var networkState: NetworkState = NetworkState.NONE
    private val cm: ConnectivityManager = Utils.getApp().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    init {
        checkNetworkState()
    }


    fun initMonitor() {
        stopMonitor()

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            cm.registerDefaultNetworkCallback(networkCallback)
        } else {
            cm.registerNetworkCallback(NetworkRequest.Builder().build(), networkCallback)
        }
    }

    fun registerListener(listener: INetworkStateListener) {
        listeners.add(listener)
    }

    fun unRegisterListener(listener: INetworkStateListener) {
        listeners.remove(listener)
    }

    fun getNetworkState(): NetworkState {
        return networkState
    }

    fun isNetworkConnected(): Boolean {
        return when (networkState) {
            NetworkState.MOBILE, NetworkState.WIFI -> true
            NetworkState.NONE -> false
            else -> false
        }
    }

    fun getIpAddress(): String {
        try {
            val enNetI: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
            while (enNetI
                    .hasMoreElements()
            ) {
                val netI: NetworkInterface = enNetI.nextElement()
                val enumIpAddr: Enumeration<InetAddress> = netI.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress: InetAddress = enumIpAddr.nextElement()
                    if (inetAddress is Inet4Address && !inetAddress.isLoopbackAddress) {
                        return inetAddress.getHostAddress()
                    }
                }
            }
        } catch (e: SocketException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun stopMonitor() {
        try {
            cm.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {

        }
    }

    private val networkCallback: ConnectivityManager.NetworkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            checkNetworkState()
            postNetworkState()
        }

        override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
            super.onBlockedStatusChanged(network, blocked)
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties)
        }

        override fun onLosing(network: Network, maxMsToLive: Int) {
            super.onLosing(network, maxMsToLive)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            networkState = NetworkState.NONE
            postNetworkState()
        }

        override fun onUnavailable() {
            super.onUnavailable()
        }
    }

    private fun postNetworkState() {
        listeners.forEach {
            it.onNetworkStateChanged(networkState)
        }
    }


    private fun checkNetworkState() {
        val networkInfo = cm.activeNetworkInfo
        networkState = if (networkInfo == null) {
            NetworkState.NONE
        } else {
            if (networkInfo.isAvailable) {
                when (networkInfo.type) {
                    ConnectivityManager.TYPE_WIFI -> NetworkState.WIFI
                    ConnectivityManager.TYPE_MOBILE -> NetworkState.MOBILE
                    else -> NetworkState.UNKNOWN
                }
            } else {
                NetworkState.NONE
            }
        }
    }

}

enum class NetworkState {
    NONE,
    WIFI,
    MOBILE,
    UNKNOWN
}

interface INetworkStateListener {
    fun onNetworkStateChanged(state: NetworkState)
}