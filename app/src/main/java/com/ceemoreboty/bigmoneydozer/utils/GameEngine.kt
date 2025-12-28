package com.ceemoreboty.bigmoneydozer.utils

import com.ceemoreboty.bigmoneydozer.models.Coin
import kotlin.math.sin

/**
 * GameEngine manages all game physics, coin mechanics, and game logic
 */
class GameEngine(
    val screenWidth: Float = 400f,
    val screenHeight: Float = 600f
) {
    private var coins = mutableListOf<Coin>()
    private var coinIdCounter = 0
    private var gameTime = 0f
    private var isPusherMovingRight = true
    private var pusherX = screenWidth / 2f
    private var pusherWidth = 80f
    private var pusherY = screenHeight - 50f
    private var pusherSpeed = 200f
    private var coinsCollected = 0
    private var coinsOffScreen = 0
    private var gameActive = true

    companion object {
        const val PUSHER_AMPLITUDE = 100f
        const val PUSHER_FREQUENCY = 2f
        const val COIN_SPAWN_RATE = 0.1f
        const val GRAVITY = 500f
        const val FRICTION = 0.98f
        const val BOUNCE_DAMPING = 0.7f
    }

    fun update(deltaTime: Float) {
        if (!gameActive) return
        gameTime += deltaTime
        updatePusher(deltaTime)
        updateCoins(deltaTime)
        checkCollisions()
        removeOffScreenCoins()
    }

    private fun updatePusher(deltaTime: Float) {
        val centerX = screenWidth / 2f
        pusherX = centerX + sin(gameTime * PUSHER_FREQUENCY) * PUSHER_AMPLITUDE
    }

    private fun updateCoins(deltaTime: Float) {
        coins.forEach { coin ->
            if (coin.isActive) {
                coin.vy += GRAVITY * deltaTime
                coin.vx *= FRICTION
                coin.vy *= FRICTION
                coin.x += coin.vx * deltaTime
                coin.y += coin.vy * deltaTime
                coin.rotation += (coin.vx / coin.radius) * 50f
                coin.rotation %= 360f
                coin.checkBoundaryCollision(0f, screenWidth, 0f, screenHeight)
                checkPusherCollision(coin)
            }
        }
    }

    private fun checkPusherCollision(coin: Coin) {
        if (coin.y + coin.radius >= pusherY && 
            coin.y - coin.radius <= pusherY + 20f &&
            coin.x + coin.radius >= pusherX &&
            coin.x - coin.radius <= pusherX + pusherWidth) {
            val pushForce = 600f
            coin.vy = -pushForce
            val pusherCenter = pusherX + pusherWidth / 2f
            if (coin.x < pusherCenter) {
                coin.vx = -300f
            } else {
                coin.vx = 300f
            }
        }
    }

    private fun checkCollisions() {
        for (i in coins.indices) {
            for (j in i + 1 until coins.size) {
                if (coins[i].isActive && coins[j].isActive) {
                    if (coins[i].isCollidingWith(coins[j])) {
                        coins[i].collideWith(coins[j])
                    }
                }
            }
        }
    }

    private fun removeOffScreenCoins() {
        coins.forEach { coin ->
            if (!coin.isActive && coin.y > screenHeight) {
                coinsOffScreen++
                coinsCollected++
            }
        }
        coins.removeAll { !it.isActive && it.y > screenHeight }
    }

    fun dropCoin(value: Int = 1) {
        val coin = Coin(
            x = screenWidth / 2f,
            y = -20f,
            vx = (Math.random().toFloat() - 0.5f) * 50f,
            vy = 0f,
            radius = 10f,
            value = value
        )
        coins.add(coin)
    }

    fun dropCoins(count: Int, value: Int = 1) {
        repeat(count) {
            dropCoin(value)
        }
    }

    fun getPusherX(): Float = pusherX
    fun getPusherY(): Float = pusherY
    fun getPusherWidth(): Float = pusherWidth
    fun getCoins(): List<Coin> = coins.toList()
    fun getCoinsCollected(): Int = coinsCollected
    fun getActiveCoinCount(): Int = coins.count { it.isActive }
    fun getGameTime(): Float = gameTime

    fun reset() {
        coins.clear()
        coinIdCounter = 0
        gameTime = 0f
        coinsCollected = 0
        coinsOffScreen = 0
        gameActive = true
    }

    fun pause() {
        gameActive = false
    }

    fun resume() {
        gameActive = true
    }

    fun isGameActive(): Boolean = gameActive
    fun getScreenSize(): Pair<Float, Float> = Pair(screenWidth, screenHeight)
}