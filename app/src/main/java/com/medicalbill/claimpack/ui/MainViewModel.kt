package com.medicalbill.claimpack.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.medicalbill.claimpack.billing.BillingManager
import com.medicalbill.claimpack.billing.PurchaseState
import com.medicalbill.claimpack.data.database.AppDatabase
import com.medicalbill.claimpack.data.database.ProcessedFile
import com.medicalbill.claimpack.data.repository.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val repository = FileRepository(database.processedFileDao())
    val billingManager = BillingManager(application)
    
    val allFiles: StateFlow<List<ProcessedFile>> = MutableStateFlow(emptyList())
    val isProUnlocked: StateFlow<Boolean> = billingManager.isProUnlocked
    val purchaseState: StateFlow<PurchaseState> = billingManager.purchaseState
    
    init {
        viewModelScope.launch {
            repository.allFiles.collect { files ->
                (allFiles as MutableStateFlow).value = files
            }
        }
    }
    
    fun insertFile(file: ProcessedFile) {
        viewModelScope.launch {
            repository.insertFile(file)
        }
    }
    
    fun deleteFile(file: ProcessedFile) {
        viewModelScope.launch {
            repository.deleteFile(file)
        }
    }
    
    fun clearAllHistory() {
        viewModelScope.launch {
            repository.deleteAllFiles()
        }
    }
    
    fun restorePurchases() {
        billingManager.restorePurchases()
    }
    
    fun resetPurchaseState() {
        billingManager.resetPurchaseState()
    }
    
    override fun onCleared() {
        super.onCleared()
        billingManager.endConnection()
    }
}
