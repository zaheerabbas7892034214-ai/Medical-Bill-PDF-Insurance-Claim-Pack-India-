package com.medicalbill.claimpack.pdf

import android.content.Context
import android.net.Uri
import android.util.Log
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.medicalbill.claimpack.data.model.BillData
import java.io.File
import java.io.FileOutputStream

class ClaimPackGenerator(private val context: Context) {
    
    private val TAG = "ClaimPackGenerator"
    
    fun generateClaimPackPdf(billData: BillData, outputFile: File): Result<File> {
        return try {
            val pdfWriter = PdfWriter(FileOutputStream(outputFile))
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)
            
            // Cover Page
            document.add(
                Paragraph("MEDICAL INSURANCE CLAIM PACK")
                    .setFontSize(20f)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
            )
            document.add(Paragraph("\n"))
            
            // Patient Information
            document.add(Paragraph("Patient Information").setFontSize(16f).setBold())
            document.add(Paragraph("Patient Name: ${billData.patientName}"))
            document.add(Paragraph("Hospital Name: ${billData.hospitalName}"))
            document.add(Paragraph("Invoice Date: ${billData.invoiceDate}"))
            document.add(Paragraph("Bill Number: ${billData.billNumber}"))
            document.add(Paragraph("\n"))
            
            // Summary Totals
            document.add(Paragraph("Summary of Expenses").setFontSize(16f).setBold())
            val summaryTable = Table(2)
            summaryTable.addCell("Category")
            summaryTable.addCell("Amount (₹)")
            
            if (billData.roomRent > 0) {
                summaryTable.addCell("Room Rent")
                summaryTable.addCell(String.format("%.2f", billData.roomRent))
            }
            if (billData.doctorFees > 0) {
                summaryTable.addCell("Doctor Fees")
                summaryTable.addCell(String.format("%.2f", billData.doctorFees))
            }
            if (billData.medicineExpenses > 0) {
                summaryTable.addCell("Medicine Expenses")
                summaryTable.addCell(String.format("%.2f", billData.medicineExpenses))
            }
            if (billData.diagnostics > 0) {
                summaryTable.addCell("Diagnostics")
                summaryTable.addCell(String.format("%.2f", billData.diagnostics))
            }
            
            summaryTable.addCell("Total Amount").setBold()
            summaryTable.addCell(String.format("%.2f", billData.totalAmount)).setBold()
            
            document.add(summaryTable)
            document.add(Paragraph("\n"))
            
            // Line Items Table
            document.add(Paragraph("Detailed Line Items").setFontSize(16f).setBold())
            val itemsTable = Table(4)
            itemsTable.addCell("Description")
            itemsTable.addCell("Quantity")
            itemsTable.addCell("Rate (₹)")
            itemsTable.addCell("Amount (₹)")
            
            billData.lineItems.forEach { item ->
                itemsTable.addCell(item.description)
                itemsTable.addCell(item.quantity.toString())
                itemsTable.addCell(String.format("%.2f", item.rate))
                itemsTable.addCell(String.format("%.2f", item.amount))
            }
            
            document.add(itemsTable)
            
            // Close the document
            document.close()
            
            Log.d(TAG, "Claim pack PDF generated successfully")
            Result.success(outputFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating claim pack PDF", e)
            Result.failure(e)
        }
    }
    
    fun generateCsv(billData: BillData, outputFile: File): Result<File> {
        return try {
            val csvContent = StringBuilder()
            
            // Header
            csvContent.append("Medical Bill Claim Pack - CSV Export\n\n")
            
            // Patient Info
            csvContent.append("Patient Information\n")
            csvContent.append("Patient Name,${billData.patientName}\n")
            csvContent.append("Hospital Name,${billData.hospitalName}\n")
            csvContent.append("Invoice Date,${billData.invoiceDate}\n")
            csvContent.append("Bill Number,${billData.billNumber}\n\n")
            
            // Line Items
            csvContent.append("Description,Quantity,Rate,Amount\n")
            billData.lineItems.forEach { item ->
                csvContent.append("\"${item.description}\",${item.quantity},${item.rate},${item.amount}\n")
            }
            
            csvContent.append("\nTotal Amount,,,${billData.totalAmount}\n")
            
            outputFile.writeText(csvContent.toString())
            
            Log.d(TAG, "CSV generated successfully")
            Result.success(outputFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating CSV", e)
            Result.failure(e)
        }
    }
    
    fun generateXlsx(billData: BillData, outputFile: File): Result<File> {
        return try {
            // Note: Apache POI for XLSX is complex, simplified implementation
            // For production, full Apache POI implementation would be needed
            val csvResult = generateCsv(billData, outputFile)
            
            Log.d(TAG, "XLSX generation delegated to CSV for now")
            csvResult
        } catch (e: Exception) {
            Log.e(TAG, "Error generating XLSX", e)
            Result.failure(e)
        }
    }
}
