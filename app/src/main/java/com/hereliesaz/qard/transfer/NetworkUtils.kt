package com.hereliesaz.qard.transfer

import android.util.Log
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections

/** Helpers for locating this device on the local network for same-Wi-Fi transfers. */
object NetworkUtils {

    /**
     * The device's site-local IPv4 address (e.g. 192.168.x.x / 10.x.x.x) on the active
     * Wi-Fi or hotspot interface, or null if none is found. This is the address a
     * receiver on the same network uses to reach our local file server.
     */
    fun localIpAddress(): String? {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return null
            Collections.list(interfaces)
                .asSequence()
                .filter { runCatching { it.isUp && !it.isLoopback }.getOrDefault(false) }
                .flatMap { Collections.list(it.inetAddresses).asSequence() }
                .filterIsInstance<Inet4Address>()
                .firstOrNull { !it.isLoopbackAddress && it.isSiteLocalAddress }
                ?.hostAddress
        } catch (e: Exception) {
            Log.e("NetworkUtils", "Failed to read local IP", e)
            null
        }
    }
}
