package com.ceemoreboty.bigmoneydozer.vpn

import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * VPN Manager to control and monitor VPN state
 */
object VpnManager {
    private const val TAG = "VpnManager"
    
    private val _isVpnEnabled = MutableStateFlow(false)
    val isVpnEnabled: StateFlow<Boolean> = _isVpnEnabled.asStateFlow()
    
    private val _bytesReceived = MutableStateFlow(0L)
    val bytesReceived: StateFlow<Long> = _bytesReceived.asStateFlow()
    
    private val _bytesSent = MutableStateFlow(0L)
    val bytesSent: StateFlow<Long> = _bytesSent.asStateFlow()

    /**
     * Check if VPN permission is granted
     */
    fun isVpnPermissionGranted(context: Context): Boolean {
        val intent = VpnService.prepare(context)
        return intent == null
    }

    /**
     * Start the VPN service
     */
    fun startVpn(context: Context): Boolean {
        return try {
            val intent = Intent(context, ProxyVpnService::class.java).apply {
                action = ProxyVpnService.ACTION_START
            }
            context.startService(intent)
            _isVpnEnabled.value = true
            Log.i(TAG, "VPN service started")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start VPN service", e)
            false
        }
    }

    /**
     * Stop the VPN service
     */
    fun stopVpn(context: Context): Boolean {
        return try {
            val intent = Intent(context, ProxyVpnService::class.java).apply {
                action = ProxyVpnService.ACTION_STOP
            }
            context.startService(intent)
            _isVpnEnabled.value = false
            Log.i(TAG, "VPN service stopped")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop VPN service", e)
            false
        }
    }

    /**
     * Toggle VPN state
     */
    fun toggleVpn(context: Context): Boolean {
        return if (_isVpnEnabled.value) {
            stopVpn(context)
        } else {
            startVpn(context)
        }
    }

    /**
     * Update traffic statistics
     */
    fun updateTrafficStats(bytesIn: Long, bytesOut: Long) {
        _bytesReceived.value = bytesIn
        _bytesSent.value = bytesOut
    }

    /**
     * Get current VPN status
     */
    fun getVpnStatus(): VpnStatus {
        return VpnStatus(
            isEnabled = _isVpnEnabled.value,
            bytesReceived = _bytesReceived.value,
            bytesSent = _bytesSent.value
        )
    }

    /**
     * Reset statistics
     */
    fun resetStats() {
        _bytesReceived.value = 0L
        _bytesSent.value = 0L
    }
}

/**
 * Data class representing VPN status
 */
data class VpnStatus(
    val isEnabled: Boolean,
    val bytesReceived: Long,
    val bytesSent: Long
) {
    fun getTotalBytes(): Long = bytesReceived + bytesSent
    
    fun getFormattedBytesReceived(): String = VpnUtils.formatBytes(bytesReceived)
    fun getFormattedBytesSent(): String = VpnUtils.formatBytes(bytesSent)
    fun getFormattedTotalBytes(): String = VpnUtils.formatBytes(getTotalBytes())
}
