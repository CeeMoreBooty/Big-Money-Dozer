package com.ceemoreboty.bigmoneydozer.vpn

import android.util.Log
import kotlinx.coroutines.*
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

/**
 * A simple HTTP/HTTPS proxy server that can route traffic through the VPN tunnel
 */
class ProxyServer(private val port: Int = 8080) {
    private val TAG = "ProxyServer"
    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val activeConnections = ConcurrentHashMap<String, Socket>()
    
    var onTrafficStats: ((bytesIn: Long, bytesOut: Long) -> Unit)? = null
    private var totalBytesIn = 0L
    private var totalBytesOut = 0L

    /**
     * Start the proxy server
     */
    fun start() {
        if (isRunning) {
            Log.w(TAG, "Proxy server is already running")
            return
        }

        scope.launch {
            try {
                serverSocket = ServerSocket(port)
                isRunning = true
                Log.i(TAG, "Proxy server started on port $port")

                while (isRunning) {
                    try {
                        val clientSocket = serverSocket?.accept()
                        if (clientSocket != null) {
                            Log.d(TAG, "New client connection from ${clientSocket.inetAddress}")
                            launch { handleClient(clientSocket) }
                        }
                    } catch (e: Exception) {
                        if (isRunning) {
                            Log.e(TAG, "Error accepting client connection", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting proxy server", e)
                isRunning = false
            }
        }
    }

    /**
     * Stop the proxy server
     */
    fun stop() {
        if (!isRunning) {
            Log.w(TAG, "Proxy server is not running")
            return
        }

        isRunning = false
        
        // Close all active connections
        activeConnections.values.forEach { socket ->
            try {
                socket.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing client socket", e)
            }
        }
        activeConnections.clear()

        // Close server socket
        try {
            serverSocket?.close()
            serverSocket = null
        } catch (e: Exception) {
            Log.e(TAG, "Error closing server socket", e)
        }

        scope.cancel()
        Log.i(TAG, "Proxy server stopped")
    }

    /**
     * Handle individual client connections
     */
    private suspend fun handleClient(clientSocket: Socket) = withContext(Dispatchers.IO) {
        val connectionId = "${clientSocket.inetAddress}:${clientSocket.port}"
        activeConnections[connectionId] = clientSocket

        try {
            clientSocket.soTimeout = 30000 // 30 second timeout
            
            val clientInput = BufferedInputStream(clientSocket.getInputStream())
            val clientOutput = BufferedOutputStream(clientSocket.getOutputStream())

            // Read the HTTP request
            val requestLine = readLine(clientInput)
            if (requestLine.isEmpty()) {
                Log.w(TAG, "Empty request from client")
                return@withContext
            }

            Log.d(TAG, "Request: $requestLine")

            // Parse the request
            val parts = requestLine.split(" ")
            if (parts.size < 3) {
                sendErrorResponse(clientOutput, 400, "Bad Request")
                return@withContext
            }

            val method = parts[0]
            val url = parts[1]
            val version = parts[2]

            // Parse headers
            val headers = mutableMapOf<String, String>()
            var line = readLine(clientInput)
            while (line.isNotEmpty()) {
                val colonIndex = line.indexOf(':')
                if (colonIndex > 0) {
                    val key = line.substring(0, colonIndex).trim()
                    val value = line.substring(colonIndex + 1).trim()
                    headers[key.lowercase()] = value
                }
                line = readLine(clientInput)
            }

            // Handle CONNECT method for HTTPS tunneling
            if (method.equals("CONNECT", ignoreCase = true)) {
                handleConnect(clientSocket, clientOutput, url)
            } else {
                // Handle regular HTTP proxy request
                handleHttpRequest(clientSocket, clientOutput, method, url, version, headers, clientInput)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error handling client connection", e)
        } finally {
            activeConnections.remove(connectionId)
            try {
                clientSocket.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing client socket", e)
            }
        }
    }

    /**
     * Handle HTTPS CONNECT tunneling
     */
    private fun handleConnect(clientSocket: Socket, clientOutput: OutputStream, hostPort: String) {
        try {
            val parts = hostPort.split(":")
            val host = parts[0]
            val port = if (parts.size > 1) parts[1].toInt() else 443

            Log.d(TAG, "Connecting to $host:$port")

            // Connect to the target server
            val serverSocket = Socket(host, port)
            serverSocket.soTimeout = 30000

            // Send connection established response
            val response = "HTTP/1.1 200 Connection Established\r\n\r\n"
            clientOutput.write(response.toByteArray())
            clientOutput.flush()

            // Start bidirectional data transfer
            coroutineScope {
                val clientToServer = launch(Dispatchers.IO) {
                    try {
                        val buffer = ByteArray(8192)
                        val clientInput = clientSocket.getInputStream()
                        val serverOutput = serverSocket.getOutputStream()
                        
                        var bytesRead: Int
                        while (clientSocket.isConnected && serverSocket.isConnected) {
                            bytesRead = clientInput.read(buffer)
                            if (bytesRead == -1) break
                            
                            serverOutput.write(buffer, 0, bytesRead)
                            serverOutput.flush()
                            totalBytesOut += bytesRead
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "Client to server transfer ended: ${e.message}")
                    }
                }

                val serverToClient = launch(Dispatchers.IO) {
                    try {
                        val buffer = ByteArray(8192)
                        val serverInput = serverSocket.getInputStream()
                        val clientOut = clientSocket.getOutputStream()
                        
                        var bytesRead: Int
                        while (clientSocket.isConnected && serverSocket.isConnected) {
                            bytesRead = serverInput.read(buffer)
                            if (bytesRead == -1) break
                            
                            clientOut.write(buffer, 0, bytesRead)
                            clientOut.flush()
                            totalBytesIn += bytesRead
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "Server to client transfer ended: ${e.message}")
                    }
                }

                // Wait for both transfers to complete
                clientToServer.join()
                serverToClient.join()
            }

            serverSocket.close()
            onTrafficStats?.invoke(totalBytesIn, totalBytesOut)

        } catch (e: Exception) {
            Log.e(TAG, "Error in CONNECT tunnel", e)
        }
    }

    /**
     * Handle regular HTTP proxy request
     */
    private fun handleHttpRequest(
        clientSocket: Socket,
        clientOutput: OutputStream,
        method: String,
        url: String,
        version: String,
        headers: Map<String, String>,
        clientInput: InputStream
    ) {
        try {
            // Parse URL
            val urlParts = if (url.startsWith("http://")) {
                url.substring(7)
            } else {
                url
            }
            
            val pathStart = urlParts.indexOf('/')
            val hostPort = if (pathStart > 0) {
                urlParts.substring(0, pathStart)
            } else {
                urlParts
            }
            
            val path = if (pathStart > 0) {
                urlParts.substring(pathStart)
            } else {
                "/"
            }

            val hostParts = hostPort.split(":")
            val host = hostParts[0]
            val port = if (hostParts.size > 1) hostParts[1].toInt() else 80

            Log.d(TAG, "Proxying $method request to $host:$port$path")

            // Connect to target server
            val serverSocket = Socket(host, port)
            serverSocket.soTimeout = 30000

            val serverOutput = serverSocket.getOutputStream()
            val serverInput = BufferedInputStream(serverSocket.getInputStream())

            // Forward the request
            val requestBuilder = StringBuilder()
            requestBuilder.append("$method $path $version\r\n")
            
            // Forward headers (excluding proxy-specific ones)
            headers.forEach { (key, value) ->
                if (!key.equals("proxy-connection", ignoreCase = true)) {
                    requestBuilder.append("$key: $value\r\n")
                }
            }
            requestBuilder.append("\r\n")

            serverOutput.write(requestBuilder.toString().toByteArray())
            serverOutput.flush()

            // If there's a request body, forward it
            if (headers.containsKey("content-length")) {
                val contentLength = headers["content-length"]?.toIntOrNull() ?: 0
                if (contentLength > 0) {
                    val buffer = ByteArray(contentLength)
                    clientInput.read(buffer)
                    serverOutput.write(buffer)
                    serverOutput.flush()
                    totalBytesOut += contentLength
                }
            }

            // Forward the response
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (serverInput.read(buffer).also { bytesRead = it } != -1) {
                clientOutput.write(buffer, 0, bytesRead)
                totalBytesIn += bytesRead
            }
            clientOutput.flush()

            serverSocket.close()
            onTrafficStats?.invoke(totalBytesIn, totalBytesOut)

        } catch (e: Exception) {
            Log.e(TAG, "Error proxying HTTP request", e)
            try {
                sendErrorResponse(clientOutput, 502, "Bad Gateway")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending error response", e)
            }
        }
    }

    /**
     * Read a line from input stream
     */
    private fun readLine(input: InputStream): String {
        val builder = StringBuilder()
        var char: Int
        while (input.read().also { char = it } != -1) {
            if (char == '\n'.code) {
                break
            }
            if (char != '\r'.code) {
                builder.append(char.toChar())
            }
        }
        return builder.toString()
    }

    /**
     * Send HTTP error response
     */
    private fun sendErrorResponse(output: OutputStream, code: Int, message: String) {
        val response = "HTTP/1.1 $code $message\r\n" +
                "Content-Type: text/html\r\n" +
                "Connection: close\r\n\r\n" +
                "<html><body><h1>$code $message</h1></body></html>"
        output.write(response.toByteArray())
        output.flush()
    }

    /**
     * Check if the proxy server is running
     */
    fun isRunning(): Boolean = isRunning

    /**
     * Get the proxy port
     */
    fun getPort(): Int = port

    /**
     * Get traffic statistics
     */
    fun getTrafficStats(): Pair<Long, Long> = Pair(totalBytesIn, totalBytesOut)
}
