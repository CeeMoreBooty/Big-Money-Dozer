package com.ceemoreboty.bigmoneydozer.models

/**
 * Data class representing a prize in the Big Money Dozer game
 */
data class Prize(
    val id: String,
    val name: String,
    val description: String,
    val value: Double,
    val imageUrl: String?,
    val rarity: PrizeRarity = PrizeRarity.COMMON,
    val isWon: Boolean = false
)

/**
 * Enum representing the rarity level of a prize
 */
enum class PrizeRarity {
    COMMON,
    UNCOMMON,
    RARE,
    EPIC,
    LEGENDARY
}

/**
 * Game state model representing the current state of a game session
 */
data class GameState(
    val sessionId: String,
    val playerId: String,
    val currentBalance: Double = 0.0,
    val totalEarnings: Double = 0.0,
    val isGameActive: Boolean = false,
    val pusherForce: Float = 0f,
    val pusherPosition: Float = 0f,
    val gameBoard: GameBoard = GameBoard(),
    val prizeCollected: List<Prize> = emptyList()
)

/**
 * Data class representing the game board
 */
data class GameBoard(
    val width: Float = 400f,
    val height: Float = 600f,
    val coins: List<Coin> = emptyList(),
    val obstacles: List<Obstacle> = emptyList()
)

/**
 * Data class representing a coin on the game board
 */
data class Coin(
    val id: String,
    val value: Double,
    val x: Float,
    val y: Float,
    val isCollected: Boolean = false
)

/**
 * Data class representing an obstacle on the game board
 */
data class Obstacle(
    val id: String,
    val type: ObstacleType,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val isActive: Boolean = true
)

/**
 * Enum representing different types of obstacles
 */
enum class ObstacleType {
    WALL,
    BARRIER,
    HOLE,
    RAMP
}

/**
 * Data class for tracking game statistics
 */
data class GameStatistics(
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val totalMoneyEarned: Double = 0.0,
    val averageEarningsPerGame: Double = 0.0,
    val bestGame: Double = 0.0,
    val worstGame: Double = 0.0,
    val favoriteAchievement: String? = null
)

/**
 * Data class representing a leaderboard entry
 */
data class LeaderboardEntry(
    val rank: Int,
    val playerId: String,
    val playerName: String,
    val highScore: Double,
    val gamesPlayed: Int,
    val totalEarnings: Double
)
