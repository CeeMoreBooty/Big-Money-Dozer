package com.ceemoreboty.bigmoneydozer.vpn

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Simple VPN Quick Toggle Widget
 * This can be embedded in any screen in the app
 */
@Composable
fun VpnQuickToggle(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vpnStatus by VpnManager.isVpnEnabled.collectAsState()
    val bytesReceived by VpnManager.bytesReceived.collectAsState()
    val bytesSent by VpnManager.bytesSent.collectAsState()

    // VPN permission launcher
    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            VpnManager.startVpn(context)
        }
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (vpnStatus) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "VPN & Proxy",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                if (vpnStatus) {
                    Text(
                        text = "↓${formatBytes(bytesReceived)} ↑${formatBytes(bytesSent)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                } else {
                    Text(
                        text = "Disconnected",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            Switch(
                checked = vpnStatus,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        val intent = VpnService.prepare(context)
                        if (intent != null) {
                            vpnPermissionLauncher.launch(intent)
                        } else {
                            VpnManager.startVpn(context)
                        }
                    } else {
                        VpnManager.stopVpn(context)
                    }
                }
            )
        }
    }
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
