package com.medicalbill.claimpack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.medicalbill.claimpack.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    fileId: Long,
    isProUnlocked: Boolean,
    onNavigateToPaywall: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var showExportSuccess by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.export_title)) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Choose Export Format",
                style = MaterialTheme.typography.titleLarge
            )
            
            ExportOptionCard(
                title = stringResource(R.string.export_pdf),
                description = "Generate complete claim pack PDF",
                icon = Icons.Default.PictureAsPdf,
                enabled = isProUnlocked,
                onClick = {
                    if (isProUnlocked) {
                        showExportSuccess = true
                    } else {
                        onNavigateToPaywall()
                    }
                }
            )
            
            ExportOptionCard(
                title = stringResource(R.string.export_csv),
                description = "Export line items as CSV",
                icon = Icons.Default.TableChart,
                enabled = isProUnlocked,
                onClick = {
                    if (isProUnlocked) {
                        showExportSuccess = true
                    } else {
                        onNavigateToPaywall()
                    }
                }
            )
            
            ExportOptionCard(
                title = stringResource(R.string.export_xlsx),
                description = "Export line items as Excel",
                icon = Icons.Default.Description,
                enabled = isProUnlocked,
                onClick = {
                    if (isProUnlocked) {
                        showExportSuccess = true
                    } else {
                        onNavigateToPaywall()
                    }
                }
            )
            
            if (!isProUnlocked) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.export_locked),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onNavigateToPaywall) {
                            Text(stringResource(R.string.upgrade_to_pro))
                        }
                    }
                }
            }
        }
    }
    
    if (showExportSuccess) {
        AlertDialog(
            onDismissRequest = { showExportSuccess = false },
            title = { Text("Export Successful") },
            text = { Text("Your file has been exported successfully!") },
            confirmButton = {
                TextButton(onClick = { showExportSuccess = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportOptionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) 
                MaterialTheme.colorScheme.secondaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = if (enabled) 
                    MaterialTheme.colorScheme.secondary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!enabled) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
