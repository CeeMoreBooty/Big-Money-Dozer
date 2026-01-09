package com.ceemoreboty.bigmoneydozer.vpn

/**
 * Utility functions for VPN and network operations
 */
object VpnUtils {
    
    /**
     * Format bytes to human-readable format
     */
    fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }
    
    /**
     * Format traffic rate (bytes per second)
     */
    fun formatBytesPerSecond(bytesPerSecond: Long): String {
        return "${formatBytes(bytesPerSecond)}/s"
    }
    
    /**
     * Parse host and port from a string
     */
    fun parseHostPort(hostPort: String, defaultPort: Int): Pair<String, Int> {
        val parts = hostPort.split(":")
        val host = parts[0]
        val port = if (parts.size > 1) parts[1].toIntOrNull() ?: defaultPort else defaultPort
        return Pair(host, port)
    }
}
