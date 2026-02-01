package com.medicalbill.claimpack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.medicalbill.claimpack.R
import com.medicalbill.claimpack.data.model.LineItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    fileId: Long,
    isProUnlocked: Boolean,
    onNavigateToPaywall: () -> Unit,
    onNavigateToClaimPack: () -> Unit,
    onNavigateBack: () -> Unit
) {
    // Mock data for preview - in production would load from database
    val lineItems = remember {
        List(25) { index ->
            LineItem(
                description = "Medical Item ${index + 1}",
                quantity = (1..5).random(),
                rate = (100..1000).random().toDouble(),
                amount = (100..5000).random().toDouble()
            )
        }
    }
    
    val visibleItemsCount = if (isProUnlocked) lineItems.size else minOf(10, lineItems.size)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.preview_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            if (!isProUnlocked) {
                BottomAppBar {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.limited_preview),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        Button(onClick = onNavigateToPaywall) {
                            Text(stringResource(R.string.unlock_all))
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Header Information
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "${stringResource(R.string.patient_name)} John Doe",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${stringResource(R.string.hospital_name)} Apollo Hospital",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${stringResource(R.string.invoice_date)} 15/01/2024",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${stringResource(R.string.bill_number)} INV-2024-001",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            text = "${stringResource(R.string.total_amount)} ₹12,500.00",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Line Items Header
            item {
                Text(
                    text = stringResource(R.string.line_items),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.description_column),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(2f)
                    )
                    Text(
                        text = stringResource(R.string.quantity_column),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(0.5f)
                    )
                    Text(
                        text = stringResource(R.string.rate_column),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = stringResource(R.string.amount_column),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
                Divider(modifier = Modifier.padding(vertical = 4.dp))
            }
            
            // Line Items
            itemsIndexed(lineItems) { index, item ->
                val isLocked = index >= visibleItemsCount
                
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isLocked) "••••••••" else item.description,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(2f)
                        )
                        Text(
                            text = if (isLocked) "•" else item.quantity.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(0.5f)
                        )
                        Text(
                            text = if (isLocked) "•••" else "₹${String.format("%.2f", item.rate)}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (isLocked) "•••" else "₹${String.format("%.2f", item.amount)}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    if (isLocked) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
                
                Divider()
            }
            
            // Action Buttons
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onNavigateToClaimPack,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isProUnlocked
                ) {
                    Text(stringResource(R.string.export_claim_pack))
                }
            }
        }
    }
}
