package com.medicalbill.claimpack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.medicalbill.claimpack.MainActivity
import com.medicalbill.claimpack.R
import com.medicalbill.claimpack.billing.PurchaseState
import com.medicalbill.claimpack.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val purchaseState by viewModel.purchaseState.collectAsState()
    val isProUnlocked by viewModel.isProUnlocked.collectAsState()
    
    LaunchedEffect(purchaseState) {
        when (purchaseState) {
            is PurchaseState.Success, is PurchaseState.AlreadyOwned -> {
                kotlinx.coroutines.delay(1500)
                onNavigateBack()
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.paywall_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.paywall_subtitle),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Free Tier Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.free_tier_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FeatureItem(stringResource(R.string.free_feature_1), false)
                    FeatureItem(stringResource(R.string.free_feature_2), true)
                    FeatureItem(stringResource(R.string.free_feature_3), true)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Pro Tier Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.pro_tier_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    FeatureItem(stringResource(R.string.pro_feature_1), false)
                    FeatureItem(stringResource(R.string.pro_feature_2), false)
                    FeatureItem(stringResource(R.string.pro_feature_3), false)
                    FeatureItem(stringResource(R.string.pro_feature_4), false)
                    FeatureItem(stringResource(R.string.pro_feature_5), false)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Purchase Button
            Button(
                onClick = {
                    viewModel.billingManager.launchPurchaseFlow(context as MainActivity)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = purchaseState !is PurchaseState.Loading && !isProUnlocked
            ) {
                if (purchaseState is PurchaseState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else if (isProUnlocked) {
                    Text("Already Unlocked ✓")
                } else {
                    Text(stringResource(R.string.purchase_button))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Restore Purchase Button
            TextButton(
                onClick = { viewModel.restorePurchases() },
                enabled = purchaseState !is PurchaseState.Loading
            ) {
                Text(stringResource(R.string.restore_purchase))
            }
            
            // Purchase State Messages
            when (purchaseState) {
                is PurchaseState.Success -> {
                    SuccessMessage(stringResource(R.string.purchase_success))
                }
                is PurchaseState.AlreadyOwned -> {
                    InfoMessage(stringResource(R.string.already_owned))
                }
                is PurchaseState.Restored -> {
                    SuccessMessage(stringResource(R.string.restore_success))
                }
                is PurchaseState.RestoreFailed -> {
                    ErrorMessage(stringResource(R.string.restore_failed))
                }
                is PurchaseState.Error -> {
                    ErrorMessage((purchaseState as PurchaseState.Error).message)
                }
                else -> {}
            }
        }
    }
}

@Composable
fun FeatureItem(text: String, isDisabled: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isDisabled) Icons.Default.Close else Icons.Default.Check,
            contentDescription = null,
            tint = if (isDisabled) 
                MaterialTheme.colorScheme.error 
            else 
                MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun SuccessMessage(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun InfoMessage(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun ErrorMessage(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}
