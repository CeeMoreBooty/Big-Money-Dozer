package com.ceemoreboty.bigmoneydozer.models

import kotlin.math.sqrt

/**
 * Coin data class representing a coin entity in the Big Money Dozer game.
 * Includes physics simulation and collision detection capabilities.
 */
data class Coin(
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,  // velocity x
    var vy: Float = 0f,  // velocity y
    var radius: Float = 10f,
    var mass: Float = 1f,
    var value: Int = 1,
    var isActive: Boolean = true,
    var rotation: Float = 0f
) {
    companion object {
        const val GRAVITY = 0.5f
        const val FRICTION = 0.98f
        const val ANGULAR_FRICTION = 0.96f
        const val MIN_VELOCITY = 0.1f
    }

    /**
     * Update coin physics simulation
     * @param deltaTime Time elapsed since last update in seconds
     */
    fun update(deltaTime: Float) {
        if (!isActive) return

        // Apply gravity
        vy += GRAVITY * deltaTime

        // Apply friction/damping
        vx *= FRICTION
        vy *= FRICTION

        // Update position
        x += vx * deltaTime
        y += vy * deltaTime

        // Update rotation
        rotation += vx * 2f
        rotation %= 360f

        // Stop movement if velocity is too small
        if (vx * vx + vy * vy < MIN_VELOCITY * MIN_VELOCITY) {
            vx = 0f
            vy = 0f
        }
    }

    /**
     * Check collision with another coin
     * @param other The other coin to check collision with
     * @return True if coins are colliding
     */
    fun isCollidingWith(other: Coin): Boolean {
        if (!isActive || !other.isActive) return false

        val dx = other.x - x
        val dy = other.y - y
        val distanceSquared = dx * dx + dy * dy
        val minDistance = radius + other.radius

        return distanceSquared < minDistance * minDistance
    }

    /**
     * Resolve collision with another coin
     * @param other The other coin to collide with
     */
    fun collideWith(other: Coin) {
        if (!isActive || !other.isActive) return

        // Calculate collision normal
        val dx = other.x - x
        val dy = other.y - y
        val distance = sqrt(dx * dx + dy * dy)

        if (distance == 0f) return

        val nx = dx / distance
        val ny = dy / distance

        // Relative velocity
        val dvx = other.vx - vx
        val dvy = other.vy - vy

        // Relative velocity along collision normal
        val dvn = dvx * nx + dvy * ny

        // Don't resolve if velocities are separating
        if (dvn >= 0) return

        // Impulse scalar (with equal mass assumption)
        val impulse = dvn / (1f / mass + 1f / other.mass)

        // Apply impulse
        vx += impulse * nx / mass
        vy += impulse * ny / mass
        other.vx -= impulse * nx / other.mass
        other.vy -= impulse * ny / other.mass

        // Separate overlapping coins
        val overlap = (radius + other.radius) - distance
        val separationX = (overlap / 2f) * nx
        val separationY = (overlap / 2f) * ny

        x -= separationX
        y -= separationY
        other.x += separationX
        other.y += separationY
    }

    /**
     * Check collision with a rectangular boundary
     * @param minX Left boundary
     * @param maxX Right boundary
     * @param minY Top boundary
     * @param maxY Bottom boundary
     */
    fun checkBoundaryCollision(minX: Float, maxX: Float, minY: Float, maxY: Float) {
        if (!isActive) return

        // Left boundary
        if (x - radius < minX) {
            x = minX + radius
            vx = -vx * 0.8f  // Bounce with energy loss
        }

        // Right boundary
        if (x + radius > maxX) {
            x = maxX - radius
            vx = -vx * 0.8f
        }

        // Top boundary
        if (y - radius < minY) {
            y = minY + radius
            vy = -vy * 0.8f
        }

        // Bottom boundary (deactivate coin if it falls off)
        if (y - radius > maxY) {
            isActive = false
        }
    }

    /**
     * Apply impulse to the coin
     * @param forceX Force in X direction
     * @param forceY Force in Y direction
     */
    fun applyImpulse(forceX: Float, forceY: Float) {
        vx += forceX / mass
        vy += forceY / mass
    }

    /**
     * Apply force to the coin (frame-independent)
     * @param forceX Force in X direction
     * @param forceY Force in Y direction
     * @param deltaTime Time elapsed in seconds
     */
    fun applyForce(forceX: Float, forceY: Float, deltaTime: Float) {
        vx += (forceX / mass) * deltaTime
        vy += (forceY / mass) * deltaTime
    }

    /**
     * Get the distance to another coin
     * @param other The other coin
     * @return Distance between coin centers
     */
    fun distanceTo(other: Coin): Float {
        val dx = other.x - x
        val dy = other.y - y
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * Get the current speed of the coin
     * @return Speed magnitude
     */
    fun getSpeed(): Float {
        return sqrt(vx * vx + vy * vy)
    }

    /**
     * Reset coin to initial state
     */
    fun reset() {
        vx = 0f
        vy = 0f
        rotation = 0f
        isActive = true
    }
}
