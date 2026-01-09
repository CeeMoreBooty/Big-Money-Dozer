package com.ceemoreboty.bigmoneydozer.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ceemoreboty.bigmoneydozer.MainActivity
import com.ceemoreboty.bigmoneydozer.R
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

/**
 * VPN Service that combines VPN functionality with a local proxy server
 * This service establishes a VPN tunnel and routes traffic through a local proxy
 */
class ProxyVpnService : VpnService() {
    
    companion object {
        private const val TAG = "ProxyVpnService"
        private const val CHANNEL_ID = "ProxyVpnChannel"
        private const val NOTIFICATION_ID = 1
        private const val VPN_ADDRESS = "10.0.0.2"
        private const val VPN_ROUTE = "0.0.0.0"
        private const val VPN_DNS = "8.8.8.8"
        const val ACTION_START = "com.ceemoreboty.bigmoneydozer.vpn.START"
        const val ACTION_STOP = "com.ceemoreboty.bigmoneydozer.vpn.STOP"
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    private var proxyServer: ProxyServer? = null
    private var isRunning = false
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var tunnelJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "ProxyVpnService created")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                Log.i(TAG, "Starting VPN service")
                startVpn()
            }
            ACTION_STOP -> {
                Log.i(TAG, "Stopping VPN service")
                stopVpn()
            }
        }
        return START_STICKY
    }

    /**
     * Start the VPN and proxy server
     */
    private fun startVpn() {
        if (isRunning) {
            Log.w(TAG, "VPN is already running")
            return
        }

        try {
            // Start as foreground service
            startForeground(NOTIFICATION_ID, createNotification("VPN Connected"))

            // Establish VPN interface
            vpnInterface = establishVpnInterface()
            if (vpnInterface == null) {
                Log.e(TAG, "Failed to establish VPN interface")
                stopVpn()
                return
            }

            // Start proxy server
            proxyServer = ProxyServer(8080).apply {
                onTrafficStats = { bytesIn, bytesOut ->
                    Log.d(TAG, "Traffic stats - In: $bytesIn bytes, Out: $bytesOut bytes")
                    updateNotification("VPN Active - ↓${formatBytes(bytesIn)} ↑${formatBytes(bytesOut)}")
                }
                start()
            }

            isRunning = true

            // Start VPN tunnel processing
            tunnelJob = scope.launch {
                processVpnTunnel()
            }

            Log.i(TAG, "VPN and proxy server started successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error starting VPN", e)
            stopVpn()
        }
    }

    /**
     * Stop the VPN and proxy server
     */
    private fun stopVpn() {
        if (!isRunning) {
            return
        }

        isRunning = false

        // Cancel tunnel processing
        tunnelJob?.cancel()
        tunnelJob = null

        // Stop proxy server
        proxyServer?.stop()
        proxyServer = null

        // Close VPN interface
        try {
            vpnInterface?.close()
            vpnInterface = null
        } catch (e: Exception) {
            Log.e(TAG, "Error closing VPN interface", e)
        }

        // Stop foreground service
        stopForeground(true)
        stopSelf()

        Log.i(TAG, "VPN and proxy server stopped")
    }

    /**
     * Establish the VPN interface
     */
    private fun establishVpnInterface(): ParcelFileDescriptor? {
        return try {
            Builder()
                .setSession("Big Money Dozer VPN")
                .addAddress(VPN_ADDRESS, 24)
                .addRoute(VPN_ROUTE, 0)
                .addDnsServer(VPN_DNS)
                .setMtu(1500)
                .setBlocking(false)
                .establish()
        } catch (e: Exception) {
            Log.e(TAG, "Error establishing VPN interface", e)
            null
        }
    }

    /**
     * Process packets through the VPN tunnel
     */
    private suspend fun processVpnTunnel() = withContext(Dispatchers.IO) {
        val fileDescriptor = vpnInterface?.fileDescriptor ?: return@withContext
        val inputStream = FileInputStream(fileDescriptor)
        val outputStream = FileOutputStream(fileDescriptor)
        
        val buffer = ByteBuffer.allocate(32767)
        
        try {
            while (isRunning) {
                // Read packet from VPN interface
                buffer.clear()
                val length = inputStream.channel.read(buffer)
                
                if (length > 0) {
                    buffer.flip()
                    
                    // Process the packet
                    processPacket(buffer, outputStream)
                } else if (length < 0) {
                    break
                }
                
                // Small delay to prevent busy-waiting
                delay(1)
            }
        } catch (e: Exception) {
            if (isRunning) {
                Log.e(TAG, "Error processing VPN tunnel", e)
            }
        }
    }

    /**
     * Process individual network packets
     */
    private fun processPacket(packet: ByteBuffer, outputStream: FileOutputStream) {
        try {
            // Get IP version
            val ipVersion = (packet.get(0).toInt() shr 4) and 0x0F
            
            if (ipVersion == 4) {
                processIPv4Packet(packet, outputStream)
            } else if (ipVersion == 6) {
                processIPv6Packet(packet, outputStream)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing packet", e)
        }
    }

    /**
     * Process IPv4 packets
     */
    private fun processIPv4Packet(packet: ByteBuffer, outputStream: FileOutputStream) {
        // IPv4 header processing
        val headerLength = (packet.get(0).toInt() and 0x0F) * 4
        val protocol = packet.get(9).toInt() and 0xFF
        
        // Get source and destination addresses
        val sourceAddr = ByteArray(4)
        val destAddr = ByteArray(4)
        packet.position(12)
        packet.get(sourceAddr)
        packet.get(destAddr)
        
        // Process based on protocol
        when (protocol) {
            6 -> processTcpPacket(packet, outputStream) // TCP
            17 -> processUdpPacket(packet, outputStream) // UDP
            else -> {
                // For other protocols, just forward the packet
                packet.position(0)
                outputStream.channel.write(packet)
            }
        }
    }

    /**
     * Process IPv6 packets
     */
    private fun processIPv6Packet(packet: ByteBuffer, outputStream: FileOutputStream) {
        // Basic IPv6 processing - just forward the packet
        packet.position(0)
        outputStream.channel.write(packet)
    }

    /**
     * Process TCP packets
     */
    private fun processTcpPacket(packet: ByteBuffer, outputStream: FileOutputStream) {
        // TCP packet processing
        // For now, forward the packet as-is
        // In a full implementation, you would intercept and route through proxy
        packet.position(0)
        outputStream.channel.write(packet)
    }

    /**
     * Process UDP packets
     */
    private fun processUdpPacket(packet: ByteBuffer, outputStream: FileOutputStream) {
        // UDP packet processing
        // For now, forward the packet as-is
        packet.position(0)
        outputStream.channel.write(packet)
    }

    /**
     * Create notification channel for Android O and above
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "VPN Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "VPN Service notifications"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Create notification for the foreground service
     */
    private fun createNotification(contentText: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, ProxyVpnService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Big Money Dozer VPN")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_delete, "Disconnect", stopPendingIntent)
            .setOngoing(true)
            .build()
    }

    /**
     * Update the notification with new content
     */
    private fun updateNotification(contentText: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, createNotification(contentText))
    }

    /**
     * Format bytes to human-readable format
     */
    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
        scope.cancel()
        Log.d(TAG, "ProxyVpnService destroyed")
    }

    override fun onRevoke() {
        super.onRevoke()
        Log.w(TAG, "VPN permission revoked")
        stopVpn()
    }
}
