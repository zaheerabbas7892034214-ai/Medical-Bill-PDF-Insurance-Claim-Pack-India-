package com.medicalbill.claimpack.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "processed_files")
data class ProcessedFile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val filePath: String,
    val patientName: String,
    val hospitalName: String,
    val invoiceDate: String,
    val billNumber: String,
    val totalAmount: Double,
    val processedDate: Long,
    val lineItemCount: Int,
    val confidence: Float
)
