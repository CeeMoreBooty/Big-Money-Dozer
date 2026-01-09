# VPN & Proxy Server Integration Guide

This guide shows how to integrate the VPN and Proxy Server feature into the Big Money Dozer app.

## Quick Start

### Step 1: Add VPN Toggle to Your Screen

The easiest way to add VPN functionality to any screen is using the `VpnQuickToggle` composable:

```kotlin
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ceemoreboty.bigmoneydozer.vpn.VpnQuickToggle

@Composable
fun YourGameScreen() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Your game content here
        
        // Add VPN toggle at the bottom
        VpnQuickToggle(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}
```

### Step 2: Add Full VPN Configuration Screen (Optional)

For complete VPN settings, add the configuration screen to your navigation:

```kotlin
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import com.ceemoreboty.bigmoneydozer.vpn.VpnConfigScreen

NavHost(...) {
    composable("vpn_settings") {
        VpnConfigScreen(
            onNavigateBack = { navController.navigateUp() }
        )
    }
    
    // Your other screens...
}
```

### Step 3: Monitor VPN State (Optional)

If you need to react to VPN state changes in your code:

```kotlin
import androidx.compose.runtime.*
import com.ceemoreboty.bigmoneydozer.vpn.VpnManager

@Composable
fun YourComponent() {
    val isVpnEnabled by VpnManager.isVpnEnabled.collectAsState()
    val bytesReceived by VpnManager.bytesReceived.collectAsState()
    val bytesSent by VpnManager.bytesSent.collectAsState()
    
    // Use these states in your UI
    if (isVpnEnabled) {
        Text("VPN is active - Traffic: ↓$bytesReceived ↑$bytesSent")
    }
}
```

## Programmatic Control

### Start VPN from Code

```kotlin
import android.content.Context
import android.net.VpnService
import androidx.activity.result.ActivityResultLauncher
import com.ceemoreboty.bigmoneydozer.vpn.VpnManager

fun startVpnFromCode(
    context: Context,
    permissionLauncher: ActivityResultLauncher<Intent>
) {
    // Check if permission is needed
    val intent = VpnService.prepare(context)
    if (intent != null) {
        // Request permission
        permissionLauncher.launch(intent)
    } else {
        // Permission already granted
        VpnManager.startVpn(context)
    }
}

// Handle permission result
val vpnPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        VpnManager.startVpn(context)
    }
}
```

### Stop VPN from Code

```kotlin
import android.content.Context
import com.ceemoreboty.bigmoneydozer.vpn.VpnManager

fun stopVpnFromCode(context: Context) {
    VpnManager.stopVpn(context)
}
```

### Toggle VPN

```kotlin
import android.content.Context
import com.ceemoreboty.bigmoneydozer.vpn.VpnManager

fun toggleVpn(context: Context) {
    VpnManager.toggleVpn(context)
}
```

## Advanced Usage

### Custom Traffic Statistics Display

```kotlin
import androidx.compose.runtime.*
import com.ceemoreboty.bigmoneydozer.vpn.VpnManager
import com.ceemoreboty.bigmoneydozer.vpn.VpnUtils

@Composable
fun CustomTrafficDisplay() {
    val bytesReceived by VpnManager.bytesReceived.collectAsState()
    val bytesSent by VpnManager.bytesSent.collectAsState()
    
    Column {
        Text("Downloaded: ${VpnUtils.formatBytes(bytesReceived)}")
        Text("Uploaded: ${VpnUtils.formatBytes(bytesSent)}")
        Text("Total: ${VpnUtils.formatBytes(bytesReceived + bytesSent)}")
    }
}
```

### Check VPN Permission Status

```kotlin
import android.content.Context
import com.ceemoreboty.bigmoneydozer.vpn.VpnManager

fun checkVpnPermission(context: Context): Boolean {
    return VpnManager.isVpnPermissionGranted(context)
}
```

### Get VPN Status

```kotlin
import com.ceemoreboty.bigmoneydozer.vpn.VpnManager
import com.ceemoreboty.bigmoneydozer.vpn.VpnStatus

fun getVpnInfo(): VpnStatus {
    val status = VpnManager.getVpnStatus()
    println("VPN Enabled: ${status.isEnabled}")
    println("Bytes Received: ${status.getFormattedBytesReceived()}")
    println("Bytes Sent: ${status.getFormattedBytesSent()}")
    println("Total Traffic: ${status.getFormattedTotalBytes()}")
    return status
}
```

## UI Components

### Available Components

1. **VpnQuickToggle** - Compact widget with toggle and stats
2. **VpnConfigScreen** - Full configuration screen
3. **VpnManager** - Programmatic control and state management
4. **VpnUtils** - Utility functions

### Styling VpnQuickToggle

```kotlin
VpnQuickToggle(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
)
```

## Best Practices

### 1. Request Permission Properly

Always check for VPN permission before starting the VPN:

```kotlin
val intent = VpnService.prepare(context)
if (intent != null) {
    // Show explanation to user
    // Then launch permission request
    permissionLauncher.launch(intent)
} else {
    // Start VPN
    VpnManager.startVpn(context)
}
```

### 2. Handle VPN Revocation

The VPN service will automatically stop if permission is revoked. Update your UI accordingly:

```kotlin
LaunchedEffect(Unit) {
    VpnManager.isVpnEnabled.collect { isEnabled ->
        if (!isEnabled) {
            // Update UI to show VPN is disconnected
        }
    }
}
```

### 3. Respect User Choice

Don't start VPN automatically without user consent. Always provide clear UI controls.

### 4. Explain VPN Usage

Before requesting VPN permission, explain to users why your app needs VPN access and what it will be used for.

## Testing

### Manual Testing Checklist

- [ ] VPN toggle switches on/off correctly
- [ ] VPN permission request appears on first use
- [ ] Notification shows when VPN is active
- [ ] Traffic statistics update in real-time
- [ ] VPN survives app minimize/restore
- [ ] VPN stops when toggle is disabled
- [ ] Network connectivity works through VPN
- [ ] No memory leaks after multiple on/off cycles

### Testing Proxy Server

Configure another app or device to use the proxy:
- **Proxy Host**: 127.0.0.1 or 10.0.0.2
- **Proxy Port**: 8080

Test HTTP and HTTPS traffic to verify proxy functionality.

## Troubleshooting

### VPN Won't Start

1. Check if another VPN is active
2. Verify VPN permission is granted
3. Check system logs: `adb logcat | grep ProxyVpnService`

### No Network Connectivity

1. Verify DNS configuration (8.8.8.8)
2. Check routing table
3. Ensure proxy server is running

### High Battery Usage

1. Check traffic statistics for unusual activity
2. Verify no infinite loops in packet processing
3. Consider reducing VPN usage or optimizing packet processing

## Security Notes

1. The VPN service runs as a foreground service for reliability
2. All traffic goes through the local proxy server
3. Network security config allows localhost connections
4. No traffic is logged or stored by default
5. Users must explicitly grant VPN permission

## More Information

For detailed technical documentation, see [VPN_PROXY_DOCUMENTATION.md](VPN_PROXY_DOCUMENTATION.md)

## Support

If you encounter issues:
1. Check the logs: `adb logcat | grep -E "ProxyVpnService|ProxyServer|VpnManager"`
2. Verify all permissions are granted in AndroidManifest.xml
3. Ensure network security configuration is properly set
4. Review the technical documentation

## Example: Complete Integration

Here's a complete example showing VPN integration in a game screen:

```kotlin
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ceemoreboty.bigmoneydozer.vpn.VpnQuickToggle
import com.ceemoreboty.bigmoneydozer.vpn.VpnManager

@Composable
fun GameScreen() {
    val isVpnEnabled by VpnManager.isVpnEnabled.collectAsState()
    
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Your game content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Game rendering here
                Text("Game Content")
            }
            
            // VPN controls at bottom
            VpnQuickToggle(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            
            // Status indicator
            if (isVpnEnabled) {
                Text(
                    text = "🔒 Secure Connection Active",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
```

This provides a complete, production-ready integration of VPN functionality into your app!
