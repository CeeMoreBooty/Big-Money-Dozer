package com.ceemoreboty.bigmoneydozer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Main GameScreen composable that displays the complete game UI including
 * header with player info, game canvas container, and control buttons.
 *
 * @param playerBalance Current player balance in currency units
 * @param coinsCollected Total coins collected in current session
 * @param isGameRunning Whether the game is currently running
 * @param onPlayPauseClick Callback when play/pause button is clicked
 * @param onResetClick Callback when reset button is clicked
 * @param modifier Modifier for styling the screen
 * @param gameContent Composable lambda for the game canvas content
 */
@Composable
fun GameScreen(
    playerBalance: Long,
    coinsCollected: Int,
    isGameRunning: Boolean,
    onPlayPauseClick: () -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier,
    gameContent: @Composable () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Game Header with player info
        GameHeader(
            playerBalance = playerBalance,
            coinsCollected = coinsCollected,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )

        // Game Canvas Container
        GameCanvasContainer(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            gameContent = gameContent
        )

        // Game Controls
        GameControls(
            isGameRunning = isGameRunning,
            onPlayPauseClick = onPlayPauseClick,
            onResetClick = onResetClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        )
    }
}

/**
 * GameHeader composable that displays player balance and coins collected.
 * Shows the current financial status and session statistics.
 *
 * @param playerBalance Current player balance in currency units
 * @param coinsCollected Total coins collected in current session
 * @param modifier Modifier for styling the header
 */
@Composable
fun GameHeader(
    playerBalance: Long,
    coinsCollected: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player Balance Display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Balance",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$$playerBalance",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Coins Collected Display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Coins Collected",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = coinsCollected.toString(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

/**
 * GameCanvasContainer composable that provides a container for the game canvas.
 * Serves as the main play area where game content is rendered.
 *
 * @param modifier Modifier for styling the container
 * @param gameContent Composable lambda containing the actual game canvas/content
 */
@Composable
fun GameCanvasContainer(
    modifier: Modifier = Modifier,
    gameContent: @Composable () -> Unit = {}
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        gameContent()
    }
}

/**
 * GameControls composable that displays game control buttons.
 * Includes play/pause and reset functionality.
 *
 * @param isGameRunning Whether the game is currently running
 * @param onPlayPauseClick Callback when play/pause button is clicked
 * @param onResetClick Callback when reset button is clicked
 * @param modifier Modifier for styling the controls
 */
@Composable
fun GameControls(
    isGameRunning: Boolean,
    onPlayPauseClick: () -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = {
                // Play/Pause Button
                Button(
                    onClick = onPlayPauseClick,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = if (isGameRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isGameRunning) "Pause Game" else "Play Game",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = if (isGameRunning) "Pause" else "Play",
                        fontWeight = FontWeight.Bold
                    )
                }

                // Reset Button
                Button(
                    onClick = onResetClick,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(
                        text = "Reset",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}
