package com.ceemoreboty.bigmoneydozer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class CoinPackage(
    val id: String,
    val name: String,
    val baseCoins: Int,
    val bonusCoins: Int,
    val totalCoins: Int,
    val price: String,
    val emoji: String,
    val badge: String? = null
)

enum class PaymentMethod(val displayName: String, val emoji: String) {
    CREDIT_CARD("Credit Card", "💳"),
    PAYPAL("PayPal", "🅿️"),
    PHONE_BILL("Phone Bill", "📱")
}

@Composable
fun ShopScreen(
    playerBalance: Int = 0,
    onNavigateBack: () -> Unit = {},
    onPurchaseComplete: (coinAmount: Int) -> Unit = {}
) {
    var selectedPackage by remember { mutableStateOf<CoinPackage?>(null) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf<PaymentMethod?>(null) }

    val coinPackages = listOf(
        CoinPackage(
            id = "starter",
            name = "Starter",
            baseCoins = 500,
            bonusCoins = 100,
            totalCoins = 600,
            price = "$0.99",
            emoji = "⭐"
        ),
        CoinPackage(
            id = "popular",
            name = "Popular",
            baseCoins = 1500,
            bonusCoins = 300,
            totalCoins = 1800,
            price = "$2.99",
            emoji = "🔥",
            badge = "POPULAR"
        ),
        CoinPackage(
            id = "best_value",
            name = "Best Value",
            baseCoins = 3500,
            bonusCoins = 1000,
            totalCoins = 4500,
            price = "$4.99",
            emoji = "💎",
            badge = "BEST VALUE"
        ),
        CoinPackage(
            id = "premium",
            name = "Premium",
            baseCoins = 7500,
            bonusCoins = 2500,
            totalCoins = 10000,
            price = "$9.99",
            emoji = "👑",
            badge = null
        ),
        CoinPackage(
            id = "ultimate",
            name = "Ultimate",
            baseCoins = 15000,
            bonusCoins = 5000,
            totalCoins = 20000,
            price = "$19.99",
            emoji = "🌟",
            badge = null
        ),
        CoinPackage(
            id = "legendary",
            name = "Legendary",
            baseCoins = 35000,
            bonusCoins = 15000,
            totalCoins = 50000,
            price = "$49.99",
            emoji = "🏆",
            badge = "LEGENDARY"
        )
    )

    Scaffold(
        topBar = {
            ShopTopAppBar(
                playerBalance = playerBalance,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Choose Your Coin Package",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(coinPackages) { package ->
                CoinPackageCard(
                    package = package,
                    onClick = {
                        selectedPackage = package
                        showPaymentDialog = true
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showPaymentDialog && selectedPackage != null) {
        PaymentMethodDialog(
            coinPackage = selectedPackage!!,
            onDismiss = {
                showPaymentDialog = false
                selectedPackage = null
                selectedPaymentMethod = null
            },
            onConfirm = { paymentMethod ->
                onPurchaseComplete(selectedPackage!!.totalCoins)
                showPaymentDialog = false
                selectedPackage = null
                selectedPaymentMethod = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopTopAppBar(
    playerBalance: Int = 0,
    onNavigateBack: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Coin Shop",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                BalanceBadge(balance = playerBalance)
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Game"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun BalanceBadge(balance: Int) {
    Surface(
        modifier = Modifier
            .padding(end = 8.dp)
            .background(
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondary
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "💰",
                fontSize = 16.sp
            )
            Text(
                text = balance.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CoinPackageCard(
    package: CoinPackage,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with Name and Emoji
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = package.emoji,
                            fontSize = 28.sp
                        )
                        Column {
                            Text(
                                text = package.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = package.price,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Coins Breakdown
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Base Coins:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = package.baseCoins.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎁 Bonus Coins:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFF9800)
                        )
                        Text(
                            text = "+ ${package.bonusCoins}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFF9800)
                        )
                    }
                    Divider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        thickness = 1.dp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Coins:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = package.totalCoins.toString(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // CTA Button
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Get ${package.name}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Badge overlay if applicable
            if (package.badge != null) {
                Badge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    text = package.badge!!
                )
            }
        }
    }
}

@Composable
fun Badge(modifier: Modifier = Modifier, text: String) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = Color(0xFFFF6B6B)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 10.sp
        )
    }
}

@Composable
fun PaymentMethodDialog(
    coinPackage: CoinPackage,
    onDismiss: () -> Unit = {},
    onConfirm: (PaymentMethod) -> Unit = {}
) {
    var selectedPayment by remember { mutableStateOf<PaymentMethod?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Choose Payment Method",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                // Package Summary
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "You're about to purchase",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = coinPackage.emoji,
                                fontSize = 24.sp
                            )
                            Text(
                                text = coinPackage.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Total Coins",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = coinPackage.totalCoins.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Divider(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(40.dp)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Price",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = coinPackage.price,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Payment Methods
                Text(
                    text = "Select Payment Method",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 8.dp)
                )

                PaymentMethod.values().forEach { method ->
                    PaymentMethodCard(
                        method = method,
                        isSelected = selectedPayment == method,
                        onClick = { selectedPayment = method }
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (selectedPayment != null) {
                                onConfirm(selectedPayment!!)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        enabled = selectedPayment != null
                    ) {
                        Text("Complete Purchase")
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodCard(
    method: PaymentMethod,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected)
            androidx.compose.material.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else
            null,
        elevation = if (isSelected)
            CardDefaults.cardElevation(defaultElevation = 8.dp)
        else
            CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = method.emoji,
                    fontSize = 28.sp
                )
                Text(
                    text = method.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (isSelected) {
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close.copy(),
                        contentDescription = "Selected",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(4.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
