package com.example.autodatatoggle

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * Runs as a foreground service so Android doesn't kill it in the background.
 * Uses ConnectivityManager.NetworkCallback (the modern, reliable way to detect
 * WiFi connections — plain broadcast receivers are unreliable on Android 8+).
 *
 * When WiFi becomes the active network, it asks DataToggleAccessibilityService
 * to open Settings and tap the Mobile Data switch off.
 */
class WifiMonitorService : Service() {

    private lateinit var connectivityManager: ConnectivityManager
    private var lastWifiState = false

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            val isWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

            if (isWifi && isValidated && !lastWifiState) {
                lastWifiState = true
                // WiFi just became the active, validated connection -> turn mobile data off
                DataToggleAccessibilityService.requestTurnOffMobileData(applicationContext)
            } else if (!isWifi) {
                lastWifiState = false
            }
        }

        override fun onLost(network: Network) {
            lastWifiState = false
        }
    }

    override fun onCreate() {
        super.onCreate()
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        startForeground(NOTIFICATION_ID, buildNotification())

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) {
            // callback may already be unregistered
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val channelId = "auto_data_toggle_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "WiFi Monitoring",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Auto Data Toggle")
            .setContentText("Watching for WiFi connections")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
    }
}
