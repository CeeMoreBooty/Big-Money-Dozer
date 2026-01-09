# VPN & Proxy Server Implementation

This document describes the VPN and Proxy Server implementation in the Big Money Dozer app.

## Overview

The implementation provides a combined VPN and Proxy Server solution for Android that allows secure routing of network traffic through a VPN tunnel with local proxy server capabilities.

## Architecture

### Components

1. **ProxyVpnService** - Main VPN service that establishes VPN tunnel
2. **ProxyServer** - Local HTTP/HTTPS proxy server
3. **VpnConfigScreen** - UI for configuring and controlling VPN
4. **VpnManager** - Utility for managing VPN state and statistics

### How It Works

```
User Device Traffic
        ↓
    VPN Tunnel (10.0.0.2)
        ↓
  Local Proxy Server (Port 8080)
        ↓
    Internet
```

## Features

### VPN Service
- Establishes VPN tunnel using Android's VpnService API
- Routes all traffic through local VPN interface
- Supports IPv4 and IPv6 protocols
- Processes TCP and UDP packets
- Persistent foreground service with notification

### Proxy Server
- HTTP and HTTPS (CONNECT) proxy support
- Concurrent connection handling using coroutines
- Traffic statistics tracking
- Configurable port (default: 8080)
- Error handling and logging

### User Interface
- Toggle VPN on/off
- Real-time traffic statistics
- Feature overview
- Technical details display

## Implementation Details

### VPN Configuration

- **VPN Address**: 10.0.0.2/24
- **Route**: 0.0.0.0/0 (all traffic)
- **DNS Server**: 8.8.8.8 (Google DNS)
- **MTU**: 1500 bytes

### Proxy Server

- **Port**: 8080
- **Protocols**: HTTP, HTTPS
- **Connection Timeout**: 30 seconds
- **Buffer Size**: 8192 bytes

## Permissions Required

The following permissions are required in AndroidManifest.xml:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.BIND_VPN_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## Usage

### Quick Toggle Widget

The easiest way to integrate VPN functionality into your app is using the VpnQuickToggle composable:

```kotlin
import com.ceemoreboty.bigmoneydozer.vpn.VpnQuickToggle

@Composable
fun YourScreen() {
    Column {
        // Your screen content
        
        // Add VPN toggle widget
        VpnQuickToggle(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}
```

### Full Configuration Screen

For complete control, use the VpnConfigScreen:

```kotlin
import com.ceemoreboty.bigmoneydozer.vpn.VpnConfigScreen

// In your navigation setup
composable("vpn_settings") {
    VpnConfigScreen(
        onNavigateBack = { /* handle navigation */ }
    )
}
```

### Starting the VPN

```kotlin
// Check if permission is granted
val intent = VpnService.prepare(context)
if (intent != null) {
    // Request permission
    startActivityForResult(intent, VPN_REQUEST_CODE)
} else {
    // Permission already granted, start VPN
    VpnManager.startVpn(context)
}
```

### Stopping the VPN

```kotlin
VpnManager.stopVpn(context)
```

### Monitoring Traffic

```kotlin
VpnManager.isVpnEnabled.collect { isEnabled ->
    // Update UI based on VPN state
}

VpnManager.bytesReceived.collect { bytes ->
    // Display bytes received
}

VpnManager.bytesSent.collect { bytes ->
    // Display bytes sent
}
```

## Security Considerations

1. **Network Security Config**: The app includes a network security configuration that allows cleartext traffic for localhost and VPN addresses
2. **Trust Anchors**: System and user-added certificates are trusted
3. **Foreground Service**: VPN runs as a foreground service for reliability
4. **Permission Control**: User must explicitly grant VPN permission

## Testing

### Manual Testing

1. Build and install the app
2. Navigate to VPN settings screen
3. Toggle VPN on
4. Grant VPN permission when prompted
5. Verify VPN connection in notification
6. Test network connectivity
7. Monitor traffic statistics
8. Toggle VPN off

### Testing Proxy Server

You can test the proxy server by configuring your device or another app to use:
- Proxy Host: 127.0.0.1 or 10.0.0.2
- Proxy Port: 8080

## Limitations

1. Basic packet processing - advanced protocol handling may need enhancement
2. IPv6 support is basic forwarding only
3. No traffic filtering or content inspection
4. Limited to HTTP/HTTPS proxy protocols
5. No persistent VPN across app restarts (intentional)

## Future Enhancements

- [ ] Advanced packet inspection and filtering
- [ ] Custom DNS resolution
- [ ] Traffic encryption options
- [ ] Whitelist/blacklist for domains
- [ ] Enhanced IPv6 support
- [ ] VPN profile persistence
- [ ] Split tunneling support
- [ ] Bandwidth throttling controls

## Troubleshooting

### VPN Won't Start
- Check VPN permission is granted
- Verify no other VPN is active
- Check system logs for errors

### No Internet Connectivity
- Verify DNS configuration
- Check routing table
- Ensure proxy server is running

### Poor Performance
- Check traffic statistics
- Monitor system resources
- Verify network conditions

## Code Structure

```
app/src/main/java/com/ceemoreboty/bigmoneydozer/vpn/
├── ProxyVpnService.kt      # Main VPN service
├── ProxyServer.kt          # Proxy server implementation
├── VpnConfigScreen.kt      # Full configuration UI screen
├── VpnQuickToggle.kt       # Quick toggle widget component
└── VpnManager.kt           # VPN state manager
```

## Dependencies

No additional dependencies are required beyond what's already in the project:
- Kotlin Coroutines (for async operations)
- Jetpack Compose (for UI)
- Android VpnService API

## License

Same as the main project license.
