package com.medicalbill.claimpack.billing

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BillingManager(private val context: Context) : PurchasesUpdatedListener {
    
    private val TAG = "BillingManager"
    private val PREFS_NAME = "billing_prefs"
    private val KEY_PRO_UNLOCKED = "pro_unlocked"
    
    companion object {
        const val PRODUCT_ID_PRO = "claim_pro_unlock"
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private var billingClient: BillingClient? = null
    private val _isProUnlocked = MutableStateFlow(prefs.getBoolean(KEY_PRO_UNLOCKED, false))
    val isProUnlocked: StateFlow<Boolean> = _isProUnlocked.asStateFlow()
    
    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()
    
    init {
        startBillingConnection()
    }
    
    private fun startBillingConnection() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing setup successful")
                    queryPurchases()
                } else {
                    Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                }
            }
            
            override fun onBillingServiceDisconnected() {
                Log.d(TAG, "Billing service disconnected")
                // Try to restart connection
                startBillingConnection()
            }
        })
    }
    
    private fun queryPurchases() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                processPurchases(purchases)
            } else {
                Log.e(TAG, "Query purchases failed: ${billingResult.debugMessage}")
            }
        }
    }
    
    private fun processPurchases(purchases: List<Purchase>) {
        var hasProPurchase = false
        
        purchases.forEach { purchase ->
            if (purchase.products.contains(PRODUCT_ID_PRO) && 
                purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                
                hasProPurchase = true
                
                // Acknowledge the purchase if not already acknowledged
                if (!purchase.isAcknowledged) {
                    acknowledgePurchase(purchase)
                }
            }
        }
        
        updateProStatus(hasProPurchase)
    }
    
    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        
        billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Purchase acknowledged")
            } else {
                Log.e(TAG, "Purchase acknowledgment failed: ${billingResult.debugMessage}")
            }
        }
    }
    
    private fun updateProStatus(isUnlocked: Boolean) {
        _isProUnlocked.value = isUnlocked
        prefs.edit().putBoolean(KEY_PRO_UNLOCKED, isUnlocked).apply()
        Log.d(TAG, "Pro status updated: $isUnlocked")
    }
    
    fun launchPurchaseFlow(activity: Activity) {
        _purchaseState.value = PurchaseState.Loading
        
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_ID_PRO)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()
        
        billingClient?.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && 
                productDetailsList.isNotEmpty()) {
                
                val productDetails = productDetailsList[0]
                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
                
                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()
                
                val launchResult = billingClient?.launchBillingFlow(activity, billingFlowParams)
                if (launchResult?.responseCode != BillingClient.BillingResponseCode.OK) {
                    _purchaseState.value = PurchaseState.Error("Failed to launch purchase flow")
                    Log.e(TAG, "Launch billing flow failed: ${launchResult?.debugMessage}")
                }
            } else {
                _purchaseState.value = PurchaseState.Error("Product not found")
                Log.e(TAG, "Query product details failed: ${billingResult.debugMessage}")
            }
        }
    }
    
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases != null) {
                    processPurchases(purchases)
                    _purchaseState.value = PurchaseState.Success
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseState.value = PurchaseState.Cancelled
                Log.d(TAG, "User cancelled the purchase")
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _purchaseState.value = PurchaseState.AlreadyOwned
                queryPurchases() // Refresh purchase status
                Log.d(TAG, "Item already owned")
            }
            else -> {
                _purchaseState.value = PurchaseState.Error(billingResult.debugMessage)
                Log.e(TAG, "Purchase update failed: ${billingResult.debugMessage}")
            }
        }
    }
    
    fun restorePurchases() {
        _purchaseState.value = PurchaseState.Loading
        queryPurchases()
        
        // After a short delay, set state based on result
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (_isProUnlocked.value) {
                _purchaseState.value = PurchaseState.Restored
            } else {
                _purchaseState.value = PurchaseState.RestoreFailed
            }
        }, 1000)
    }
    
    fun resetPurchaseState() {
        _purchaseState.value = PurchaseState.Idle
    }
    
    fun endConnection() {
        billingClient?.endConnection()
    }
}

sealed class PurchaseState {
    object Idle : PurchaseState()
    object Loading : PurchaseState()
    object Success : PurchaseState()
    object Cancelled : PurchaseState()
    object AlreadyOwned : PurchaseState()
    object Restored : PurchaseState()
    object RestoreFailed : PurchaseState()
    data class Error(val message: String) : PurchaseState()
}
