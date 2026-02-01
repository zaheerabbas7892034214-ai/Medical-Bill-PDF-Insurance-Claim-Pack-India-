package com.medicalbill.claimpack.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.io.File

class PdfProcessor(private val context: Context) {
    
    private val TAG = "PdfProcessor"
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    suspend fun extractTextFromPdf(uri: Uri): Result<String> {
        return try {
            val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                ?: return Result.failure(Exception("Failed to open PDF"))
            
            val renderer = PdfRenderer(pfd)
            val totalPages = renderer.pageCount
            val extractedText = StringBuilder()
            
            Log.d(TAG, "Processing PDF with $totalPages pages")
            
            for (i in 0 until totalPages) {
                val page = renderer.openPage(i)
                
                // Render page to bitmap
                val bitmap = Bitmap.createBitmap(
                    page.width * 2,  // 2x resolution for better OCR
                    page.height * 2,
                    Bitmap.Config.ARGB_8888
                )
                
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                
                // Use ML Kit for OCR
                val text = performOCR(bitmap)
                extractedText.append(text).append("\n\n")
                
                bitmap.recycle()
                page.close()
            }
            
            renderer.close()
            pfd.close()
            
            Result.success(extractedText.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting text from PDF", e)
            Result.failure(e)
        }
    }
    
    private suspend fun performOCR(bitmap: Bitmap): String {
        return try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val result = textRecognizer.process(inputImage).await()
            result.text
        } catch (e: Exception) {
            Log.e(TAG, "OCR failed", e)
            ""
        }
    }
    
    fun cleanup() {
        textRecognizer.close()
    }
}
