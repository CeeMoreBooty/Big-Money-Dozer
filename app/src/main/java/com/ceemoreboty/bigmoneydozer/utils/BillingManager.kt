package com.ceemoreboty.bigmoneydozer.utils

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import kotlinx.coroutines.*

/**
 * Manages Google Play Billing for in-app purchases, product loading, and purchase acknowledgement.
 * Provides a singleton interface for handling product queries, purchase flows, and acknowledgement.
 */
class BillingManager(private val context: Context) {

    private lateinit var billingClient: BillingClient
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    // LiveData for observing products
    private val _availableProducts = MutableLiveData<List<ProductDetails>>()
    val availableProducts: LiveData<List<ProductDetails>> = _availableProducts

    // LiveData for observing purchases
    private val _purchaseHistory = MutableLiveData<List<Purchase>>()
    val purchaseHistory: LiveData<List<Purchase>> = _purchaseHistory

    // LiveData for billing connection state
    private val _billingConnectionState = MutableLiveData<BillingConnectionState>()
    val billingConnectionState: LiveData<BillingConnectionState> = _billingConnectionState

    // LiveData for purchase flow events
    private val _purchaseFlowState = MutableLiveData<PurchaseFlowState>()
    val purchaseFlowState: LiveData<PurchaseFlowState> = _purchaseFlowState

    init {
        initializeBillingClient()
    }

    /**
     * Initialize the BillingClient and establish connection to Google Play Billing.
     */
    private fun initializeBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        establishBillingConnection()
    }

    /**
     * Establish connection to Google Play Billing service.
     */
    private fun establishBillingConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _billingConnectionState.value = BillingConnectionState.CONNECTED
                    loadProducts()
                    queryPurchaseHistory()
                } else {
                    _billingConnectionState.value = BillingConnectionState.DISCONNECTED
                }
            }

            override fun onBillingServiceDisconnected() {
                _billingConnectionState.value = BillingConnectionState.DISCONNECTED
                // Attempt to reconnect after a delay
                scope.launch {
                    delay(5000)
                    if (!billingClient.isReady) {
                        establishBillingConnection()
                    }
                }
            }
        })
    }

    /**
     * Load available products from Google Play.
     * Supports both IN_APP and SUBSCRIPTION product types.
     */
    private fun loadProducts() {
        scope.launch {
            try {
                val productList = listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("in_app_purchase_example")
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("subscription_example")
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )

                val params = QueryProductDetailsParams.newBuilder()
                    .setProductList(productList)
                    .build()

                billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        _availableProducts.postValue(productDetailsList)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Query purchase history for both in-app and subscription purchases.
     */
    private fun queryPurchaseHistory() {
        scope.launch {
            try {
                // Query in-app purchases
                billingClient.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                ) { billingResult, purchases ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        _purchaseHistory.postValue(purchases)
                    }
                }

                // Query subscriptions
                billingClient.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                ) { billingResult, purchases ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        val currentPurchases = _purchaseHistory.value?.toMutableList() ?: mutableListOf()
                        currentPurchases.addAll(purchases)
                        _purchaseHistory.postValue(currentPurchases)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Launch the purchase flow for a specific product.
     *
     * @param activity The Activity context required for launching the purchase flow
     * @param productId The product ID to purchase
     * @param productType The type of product (INAPP or SUBS)
     */
    fun launchPurchaseFlow(
        activity: Activity,
        productId: String,
        @BillingClient.ProductType productType: String = BillingClient.ProductType.INAPP
    ) {
        scope.launch {
            try {
                val productDetails = _availableProducts.value?.find { it.productId == productId }
                    ?: return@launch

                val offerToken = when (productType) {
                    BillingClient.ProductType.SUBS -> {
                        productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return@launch
                    }
                    else -> ""
                }

                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .apply {
                                    if (productType == BillingClient.ProductType.SUBS && offerToken.isNotEmpty()) {
                                        setOfferToken(offerToken)
                                    }
                                }
                                .build()
                        )
                    )
                    .build()

                val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
                _purchaseFlowState.postValue(
                    PurchaseFlowState.FlowStarted(billingResult.responseCode)
                )
            } catch (e: Exception) {
                _purchaseFlowState.postValue(PurchaseFlowState.Error(e.message ?: "Unknown error"))
                e.printStackTrace()
            }
        }
    }

    /**
     * Acknowledge a purchase to complete the purchase flow.
     * Required for in-app purchases within a certain time frame.
     *
     * @param purchase The Purchase object to acknowledge
     */
    fun acknowledgePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) {
            return
        }

        scope.launch {
            try {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        _purchaseFlowState.postValue(
                            PurchaseFlowState.PurchaseAcknowledged(purchase.productId)
                        )
                    } else {
                        _purchaseFlowState.postValue(
                            PurchaseFlowState.Error("Failed to acknowledge purchase: ${billingResult.debugMessage}")
                        )
                    }
                }
            } catch (e: Exception) {
                _purchaseFlowState.postValue(PurchaseFlowState.Error(e.message ?: "Unknown error"))
                e.printStackTrace()
            }
        }
    }

    /**
     * Consume a purchase token (primarily for consumable in-app purchases).
     * Allows the user to purchase the same product again.
     *
     * @param purchase The Purchase object to consume
     */
    fun consumePurchase(purchase: Purchase) {
        scope.launch {
            try {
                val consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.consumeAsync(consumeParams) { billingResult, purchaseToken ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        _purchaseFlowState.postValue(
                            PurchaseFlowState.PurchaseConsumed(purchaseToken)
                        )
                    } else {
                        _purchaseFlowState.postValue(
                            PurchaseFlowState.Error("Failed to consume purchase: ${billingResult.debugMessage}")
                        )
                    }
                }
            } catch (e: Exception) {
                _purchaseFlowState.postValue(PurchaseFlowState.Error(e.message ?: "Unknown error"))
                e.printStackTrace()
            }
        }
    }

    /**
     * Get a product by its product ID.
     *
     * @param productId The product ID to search for
     * @return The ProductDetails if found, null otherwise
     */
    fun getProductById(productId: String): ProductDetails? {
        return _availableProducts.value?.find { it.productId == productId }
    }

    /**
     * Check if a product has been purchased.
     *
     * @param productId The product ID to check
     * @return True if the product has been purchased and acknowledged, false otherwise
     */
    fun isPurchased(productId: String): Boolean {
        return _purchaseHistory.value?.any { purchase ->
            purchase.productId == productId && purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        } ?: false
    }

    /**
     * Reconnect to the billing service if disconnected.
     */
    fun reconnectBillingService() {
        if (!billingClient.isReady) {
            establishBillingConnection()
        }
    }

    /**
     * Clean up resources and disconnect from the billing service.
     */
    fun endConnection() {
        scope.cancel()
        if (::billingClient.isInitialized) {
            billingClient.endConnection()
        }
    }

    /**
     * Listener for purchase updates from Google Play.
     */
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    // Acknowledge the purchase if not already acknowledged
                    if (!purchase.isAcknowledged) {
                        acknowledgePurchase(purchase)
                    }
                }
            }
            // Update purchase history
            queryPurchaseHistory()
            _purchaseFlowState.postValue(PurchaseFlowState.PurchasesUpdated(purchases))
        } else {
            _purchaseFlowState.postValue(
                PurchaseFlowState.Error("Purchase failed: ${billingResult.debugMessage}")
            )
        }
    }

    /**
     * Sealed class representing different states of the purchase flow.
     */
    sealed class PurchaseFlowState {
        data class FlowStarted(val responseCode: Int) : PurchaseFlowState()
        data class PurchaseAcknowledged(val productId: String) : PurchaseFlowState()
        data class PurchaseConsumed(val purchaseToken: String) : PurchaseFlowState()
        data class PurchasesUpdated(val purchases: List<Purchase>) : PurchaseFlowState()
        data class Error(val message: String) : PurchaseFlowState()
    }

    /**
     * Enum representing the billing connection state.
     */
    enum class BillingConnectionState {
        CONNECTED,
        DISCONNECTED
    }

    companion object {
        @Volatile
        private var instance: BillingManager? = null

        fun getInstance(context: Context): BillingManager =
            instance ?: synchronized(this) {
                instance ?: BillingManager(context).also { instance = it }
            }
    }
}
