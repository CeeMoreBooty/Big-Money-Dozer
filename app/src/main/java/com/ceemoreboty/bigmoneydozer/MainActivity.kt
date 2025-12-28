package com.ceemoreboty.bigmoneydozer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ceemoreboty.bigmoneydozer.ui.screens.GameScreen
import com.ceemoreboty.bigmoneydozer.ui.screens.ShopScreen
import com.ceemoreboty.bigmoneydozer.utils.BillingManager
import timber.log.Timber

class MainActivity : ComponentActivity() {
    private lateinit var billingManager: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        billingManager = BillingManager.getInstance(this)

        setContent {
            BigMoneyDozerApp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        billingManager.endConnection()
    }
}

@Composable
fun BigMoneyDozerApp() {
    var currentScreen by remember { mutableStateOf(AppScreen.GAME) }
    var playerBalance by remember { mutableStateOf(100) }
    var totalWinnings by remember { mutableStateOf(0) }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF1a1a2e)
        ) {
            when (currentScreen) {
                AppScreen.GAME -> {
                    GameScreen(
                        playerBalance = playerBalance,
                        onBalanceChange = { playerBalance = it },
                        onWinnings = { amount ->
                            totalWinnings += amount
                            playerBalance += amount
                        },
                        onNavigateToShop = { currentScreen = AppScreen.SHOP },
                        totalWinnings = totalWinnings
                    )
                }
                AppScreen.SHOP -> {
                    ShopScreen(
                        playerBalance = playerBalance,
                        onBalanceChange = { playerBalance = it },
                        onNavigateToGame = { currentScreen = AppScreen.GAME }
                    )
                }
                AppScreen.STATS -> {
                    StatsScreen(
                        totalWinnings = totalWinnings,
                        playerBalance = playerBalance,
                        onNavigateBack = { currentScreen = AppScreen.GAME }
                    )
                }
            }
        }
    }
}

@Composable
fun StatsScreen(
    totalWinnings: Int,
    playerBalance: Int,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your Statistics",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        StatCard(
            title = "Current Balance",
            value = "$playerBalance",
            backgroundColor = Color(0xFF16213e)
        )

        Spacer(modifier = Modifier.height(16.dp))

        StatCard(
            title = "Total Winnings",
            value = "$totalWinnings",
            backgroundColor = Color(0xFF0f3460)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFe94560)
            )
        ) {
            Text("Back to Game", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    backgroundColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00d4ff)
            )
        }
    }
}

enum class AppScreen {
    GAME,
    SHOP,
    STATS
}