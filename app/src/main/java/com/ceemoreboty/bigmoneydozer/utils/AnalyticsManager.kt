package com.ceemoreboty.bigmoneydozer.utils

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import android.os.Bundle

/**
 * AnalyticsManager handles all Firebase Analytics and Crashlytics tracking
 * for game events, purchases, errors, and user interactions.
 */
object AnalyticsManager {
    
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private val crashlytics = Firebase.crashlytics
    
    /**
     * Initialize the AnalyticsManager with a context
     * @param context Application context
     */
    fun initialize(context: Context) {
        firebaseAnalytics = Firebase.analytics
    }
    
    // ==================== GAME EVENTS ====================
    
    /**
     * Track when a game session starts
     * @param levelId The level/game ID
     * @param difficulty Game difficulty level
     */
    fun trackGameSessionStart(levelId: String, difficulty: String = "normal") {
        val bundle = Bundle().apply {
            putString("level_id", levelId)
            putString("difficulty", difficulty)
            putLong("timestamp", System.currentTimeMillis())
        }
        firebaseAnalytics.logEvent("game_session_start", bundle)
    }
    
    /**
     * Track when a game session ends
     * @param levelId The level/game ID
     * @param score Final score achieved
     * @param duration Duration of the game session in seconds
     * @param result Result of the game (won, lost, quit)
     */
    fun trackGameSessionEnd(
        levelId: String,
        score: Int,
        duration: Long,
        result: String = "completed"
    ) {
        val bundle = Bundle().apply {
            putString("level_id", levelId)
            putInt("final_score", score)
            putLong("session_duration_seconds", duration)
            putString("result", result)
            putLong("timestamp", System.currentTimeMillis())
        }
        firebaseAnalytics.logEvent("game_session_end", bundle)
    }
    
    /**
     * Track score changes during gameplay
     * @param levelId The level/game ID
     * @param score Current score
     * @param scoreChangeAmount Amount the score changed by
     */
    fun trackScoreChange(levelId: String, score: Int, scoreChangeAmount: Int) {
        val bundle = Bundle().apply {
            putString("level_id", levelId)
            putInt("current_score", score)
            putInt("score_change", scoreChangeAmount)
            putLong("timestamp", System.currentTimeMillis())
        }
        firebaseAnalytics.logEvent("score_changed", bundle)
    }
    
    /**
     * Track when a level is completed
     * @param levelId The level ID
     * @param stars Number of stars earned (1-3)
     * @param score Final score on the level
     */
    fun trackLevelCompleted(levelId: String, stars: Int, score: Int) {
        val bundle = Bundle().apply {
            putString("level_id", levelId)
            putInt("stars_earned", stars)
            putInt("level_score", score)
            putLong("timestamp", System.currentTimeMillis())
        }
        firebaseAnalytics.logEvent("level_completed", bundle)
    }
    
    /**
     * Track when a level is failed
     * @param levelId The level ID
     * @param attemptsCount Number of attempts made
     * @param maxScore Maximum score achieved on this level
     */
    fun trackLevelFailed(levelId: String, attemptsCount: Int, maxScore: Int) {
        val bundle = Bundle().apply {
            putString("level_id", levelId)
            putInt("attempts", attemptsCount)
            putInt("max_score_achieved", maxScore)
            putLong("timestamp", System.currentTimeMillis())
        }
        firebaseAnalytics.logEvent("level_failed", bundle)
    }
    
    /**
     * Track power-up usage
     * @param powerUpType Type of power-up used
     * @param levelId The level ID where power-up was used
     * @param costCoins Cost in coins, if any
     */
    fun trackPowerUpUsed(powerUpType: String, levelId: String, costCoins: Int = 0) {
        val bundle = Bundle().apply {
            putString("power_up_type", powerUpType)
            putString("level_id", levelId)
            putInt("cost_coins", costCoins)
            putLong("timestamp", System.currentTimeMillis())
        }
        firebaseAnalytics.logEvent("power_up_used", bundle)
    }
    
    /**
     * Track when a player loses lives
     * @param livesRemaining Lives remaining after loss
     * @param reason Reason for loss (game_over, used_for_continue, etc.)
     */
    fun trackLivesLost(livesRemaining: Int, reason: String = "game_over") {
        val bundle = Bundle().apply {
            putInt("lives_remaining", livesRemaining)
            putString("loss_reason", reason)
            putLong("timestamp", System.currentTimeMillis())
        }
        firebaseAnalytics.logEvent("lives_lost", bundle)
    }
    
    /**
     * Track when lives are restored
     * @param livesRestored Number of lives restored
     * @param method How lives were restored (ad, purchase, daily_reward, etc.)
     */
    fun trackLivesRestored(livesRestored: Int, method: String) {
        val bundle = Bundle().apply {
            putInt("lives_restored", livesRestored)
            putString("restoration_method", method)
            putLong("timestamp", System.currentTimeMillis())
        }
        firebaseAnalytics.logEvent("lives_restored", bundle)
    }
    
    // ==================== PURCHASE EVENTS ====================
    
    /**
     * Track in-app purchase
     * @param itemId ID of the item purchased
     * @param itemName Name of the item
     * @param price Price of the item
     * @param currency Currency code (e.g., "USD")
     * @param quantity Quantity purchased
     */
    fun trackPurchase(
        itemId: String,
        itemName: String,
        price: Double,
        currency: String = "USD",
        quantity: Long = 1
    ) {
        val bundle = Bundle().apply {
            putString("item_id", itemId)
            putString("item_name", itemName)
            putDouble("value", price)
            putString("currency", currency)
            putLong("quantity", quantity)
            putLong("timestamp", System.currentTimeMillis())
        }
        firebaseAnalytics.logEvent("purchase", bundle)
    }
    
    /**
     * Track coin purchase
     * @param coinAmount Number of coins purchased
     * @param price Price paid
     * @param currency Currency code
     */
    fun trackCoinPurchase(coinAmount: Int, price: Double, currency: String = "USD") {
        val bundle = Bundle().apply {
            putInt("coins_purchased", coinAmount)
            putDouble("price", price)
            putString("currency", currency)
            putLong("timestamp", System.currentTimeMillis())
        }
        firebaseAnalytics.logEvent("coin_purchase", bundle)
    }
    
    /**
     * Track coins spent in-game
     * @param coinAmount Number of coins spent
     * @param reason Reason for spending (power_up, continue, etc.)
     */
    fun trackCoinsSpent(coinAmount: Int, reason: String) {
        val bundle = Bundle().apply {
            putInt("coins_spent", coinAmount)
            putString("spend_reason", reason)
            putLong("timestamp", System.currentTimeMillis())
        }
        firebaseAnalytics.logEvent("coins_spent", bundle)
    }
    
    /**
     * Track purchase initiation
     * @param itemId ID of the item
     * @param itemName Name of the item
     * @param price Price of the item
     */
    fun trackPurchaseInitiated(itemId: String, itemName: String, price: Double) {
        val bundle = Bundle().apply {
            putString("item_id", itemId)
            putString("item_name", itemName)
            putDouble("value", price)
            putLong("timestamp", System.currentTimeMillis())
        }
        firebaseAnalytics.logEvent("purchase_initiated", bundle)
    }
    
    /**
     * Track purchase failure
     * @param itemId ID of the item
     * @param error Error message
     * @param errorCode Error code
     */
    fun trackPurchaseFailed(itemId: String, error: String, errorCode: Int? = null) {
        val bundle = Bundle().apply {
            putString("item_id", itemId)
            putString("error_message", error)
            if (errorCode != null) {
                putInt("error_code", errorCode)
            }
            putLong("timestamp", System.currentTimeMillis())
        }
        firebaseAnalytics.logEvent("purchase_failed", bundle)
        recordError("Purchase Failed", "Item: $itemId, Error: $error")
    }
    
    /**
     * Track ad revenue
     * @param value Revenue amount
     * @param currency Currency code
     * @param adType Type of ad (banner, interstitial, rewarded, etc.)
     * @param adNetwork Ad network name
     */
    fun trackAdRevenue(
        value: Double,
        currency: String = "USD",
        adType: String = "unknown",
        adNetwork: String = "unknown"
    ) {
        val bundle = Bundle().apply {
            putDouble("value", value)
            putString("currency", currency)
            putString("ad_type", adType)
            putString("ad_network", adNetwork)
            putLong("timestamp", System.currentTimeMillis())
        }
        firebaseAnalytics.logEvent("ad_revenue", bundle)
    }
    
    // ==================== ERROR TRACKING ====================
    
    /**
     * Record a non-fatal error/exception
     * @param errorName Name/title of the error
     * @param errorMessage Detailed error message
     * @param exception The exception object (optional)
     */
    fun recordError(
        errorName: String,
        errorMessage: String,
        exception: Exception? = null
    ) {
        val fullMessage = "[$errorName] $errorMessage"
        
        // Log to Crashlytics
        if (exception != null) {
            crashlytics.recordException(exception)
        } else {
            crashlytics.recordException(Exception(fullMessage))
        }
        
        // Also log to Analytics for tracking
        val bundle = Bundle().apply {
            putString("error_name", errorName)
            putString("error_message", errorMessage)
            putLong("timestamp", System.currentTimeMillis())
        }
        firebaseAnalytics.logEvent("app_error", bundle)
    }
    
    /**
     * Record a game-specific error
     * @param errorType Type of game error (logic_error, data_corruption, etc.)
     * @param description Error description
     */
    fun recordGameError(errorType: String, description: String) {
        val message = "Game Error [$errorType]: $description"
        recordError("GameError_$errorType", description)
    }
    
    /**
     * Record a network error
     * @param endpoint API endpoint that failed
     * @param statusCode HTTP status code if available
     * @param errorMessage Error message
     */
    fun recordNetworkError(
        endpoint: String,
        statusCode: Int? = null,
        errorMessage: String? = null
    ) {
        val message = "Network Error - Endpoint: $endpoint" +
                (statusCode?.let {", Status: $it"} ?: "") +
                (errorMessage?.let {", Message: $it"} ?: "")
        recordError("NetworkError", message)
    }
    
    /**
     * Log a custom event
     * @param eventName Name of the event
     * @param parameters Map of event parameters
     */
    fun logCustomEvent(eventName: String, parameters: Map<String, Any> = emptyMap()) {
        val bundle = Bundle().apply {
            parameters.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                    is Float -> putFloat(key, value)
                }
            }
            putLong("timestamp", System.currentTimeMillis())
        }
        firebaseAnalytics.logEvent(eventName, bundle)
    }
    
    // ==================== USER PROPERTIES ====================
    
    /**
     * Set user property for segmentation
     * @param propertyName Name of the property
     * @param value Value of the property
     */
    fun setUserProperty(propertyName: String, value: String) {
        firebaseAnalytics.setUserProperty(propertyName, value)
    }
    
    /**
     * Set user ID for tracking
     * @param userId Unique user identifier
     */
    fun setUserId(userId: String) {
        firebaseAnalytics.setUserId(userId)
    }
    
    /**
     * Track user level/progression
     * @param level Current level
     */
    fun setUserLevel(level: Int) {
        firebaseAnalytics.setUserProperty("level", level.toString())
    }
    
    /**
     * Track total coins user has
     * @param coins Total coins
     */
    fun setUserCoins(coins: Int) {
        firebaseAnalytics.setUserProperty("total_coins", coins.toString())
    }
    
    /**
     * Track player type/subscription status
     * @param playerType Type of player (free, premium, vip, etc.)
     */
    fun setPlayerType(playerType: String) {
        firebaseAnalytics.setUserProperty("player_type", playerType)
    }
    
    // ==================== SCREEN TRACKING ====================
    
    /**
     * Track screen view
     * @param screenName Name of the screen
     * @param screenClass Class name of the screen/activity
     */
    fun trackScreenView(screenName: String, screenClass: String? = null) {
        val bundle = Bundle().apply {
            putString("screen_name", screenName)
            if (screenClass != null) {
                putString("screen_class", screenClass)
            }
        }
        firebaseAnalytics.logEvent("screen_view", bundle)
    }
    
    /**
     * Track button/UI element click
     * @param elementName Name of the element clicked
     * @param screenName Screen where the click occurred
     */
    fun trackUIClick(elementName: String, screenName: String? = null) {
        val bundle = Bundle().apply {
            putString("element_name", elementName)
            if (screenName != null) {
                putString("screen_name", screenName)
            }
            putLong("timestamp", System.currentTimeMillis())
        }
        firebaseAnalytics.logEvent("ui_click", bundle)
    }
    
    // ==================== FEATURE USAGE ====================
    
    /**
     * Track feature/tutorial completion
     * @param featureName Name of the feature
     * @param completed Whether the feature was completed
     */
    fun trackFeatureUsage(featureName: String, completed: Boolean = true) {
        val bundle = Bundle().apply {
            putString("feature_name", featureName)
            putBoolean("completed", completed)
            putLong("timestamp", System.currentTimeMillis())
        }
        firebaseAnalytics.logEvent("feature_usage", bundle)
    }
    
    /**
     * Track tutorial completion
     * @param tutorialName Name of the tutorial
     * @param stepNumber Step number completed
     * @param totalSteps Total steps in the tutorial
     */
    fun trackTutorialProgress(tutorialName: String, stepNumber: Int, totalSteps: Int) {
        val bundle = Bundle().apply {
            putString("tutorial_name", tutorialName)
            putInt("step_number", stepNumber)
            putInt("total_steps", totalSteps)
            putLong("timestamp", System.currentTimeMillis())
        }
        firebaseAnalytics.logEvent("tutorial_progress", bundle)
    }
    
    /**
     * Track daily login
     * @param isFirstTime Whether this is the player's first time
     * @param consecutiveDays Number of consecutive days logged in
     */
    fun trackDailyLogin(isFirstTime: Boolean = false, consecutiveDays: Int = 1) {
        val bundle = Bundle().apply {
            putBoolean("is_first_time", isFirstTime)
            putInt("consecutive_days", consecutiveDays)
            putLong("timestamp", System.currentTimeMillis())
        }
        firebaseAnalytics.logEvent("daily_login", bundle)
    }
    
    /**
     * Track when a player watches a rewarded ad
     * @param rewardType Type of reward given
     * @param rewardValue Value of the reward
     * @param levelId Level ID where ad was watched (optional)
     */
    fun trackRewardedAdWatched(rewardType: String, rewardValue: Int, levelId: String? = null) {
        val bundle = Bundle().apply {
            putString("reward_type", rewardType)
            putInt("reward_value", rewardValue)
            if (levelId != null) {
                putString("level_id", levelId)
            }
            putLong("timestamp", System.currentTimeMillis())
        }
        firebaseAnalytics.logEvent("rewarded_ad_watched", bundle)
    }
    
    /**
     * Track ad-related errors
     * @param adType Type of ad that failed
     * @param error Error message
     */
    fun trackAdError(adType: String, error: String) {
        recordError("AdError_$adType", error)
    }
    
    /**
     * Enable/disable analytics collection
     * @param enabled True to enable, false to disable
     */
    fun setAnalyticsCollectionEnabled(enabled: Boolean) {
        firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
    }
    
    /**
     * Set Crashlytics data collection enabled/disabled
     * @param enabled True to enable, false to disable
     */
    fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        crashlytics.setCrashlyticsCollectionEnabled(enabled)
    }
}
