package com.medicalbill.claimpack.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.medicalbill.claimpack.R
import com.medicalbill.claimpack.data.database.ProcessedFile
import com.medicalbill.claimpack.pdf.BillDataExtractor
import com.medicalbill.claimpack.pdf.PdfProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvertProgressScreen(
    fileUri: String,
    onConversionComplete: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var progress by remember { mutableStateOf(0f) }
    var statusText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    LaunchedEffect(fileUri) {
        scope.launch {
            try {
                statusText = context.getString(R.string.progress_rendering)
                progress = 0.2f
                
                val uri = Uri.parse(Uri.decode(fileUri))
                val pdfProcessor = PdfProcessor(context)
                
                statusText = context.getString(R.string.progress_ocr)
                progress = 0.4f
                
                val textResult = withContext(Dispatchers.IO) {
                    pdfProcessor.extractTextFromPdf(uri)
                }
                
                if (textResult.isFailure) {
                    throw textResult.exceptionOrNull() ?: Exception("Unknown error")
                }
                
                statusText = context.getString(R.string.progress_extracting)
                progress = 0.7f
                
                val extractor = BillDataExtractor()
                val billData = withContext(Dispatchers.Default) {
                    extractor.extractBillData(textResult.getOrThrow())
                }
                
                progress = 0.9f
                
                // Save to database (simplified for now)
                val fileId = 1L // Would normally insert and get the ID
                
                statusText = context.getString(R.string.progress_complete)
                progress = 1f
                
                kotlinx.coroutines.delay(500)
                onConversionComplete(fileId)
                
            } catch (e: Exception) {
                isError = true
                errorMessage = e.message ?: "Unknown error occurred"
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.convert_progress_title)) },
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
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isError) {
                Text(
                    text = stringResource(R.string.error_extraction),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onNavigateBack) {
                    Text(stringResource(R.string.retry_button))
                }
            } else {
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.size(100.dp),
                    strokeWidth = 8.dp
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
