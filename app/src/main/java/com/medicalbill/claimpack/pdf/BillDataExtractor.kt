package com.medicalbill.claimpack.pdf

import android.util.Log
import com.medicalbill.claimpack.data.model.BillData
import com.medicalbill.claimpack.data.model.LineItem
import java.util.regex.Pattern

class BillDataExtractor {
    
    private val TAG = "BillDataExtractor"
    
    fun extractBillData(text: String): BillData {
        Log.d(TAG, "Extracting bill data from text")
        
        val patientName = extractPatientName(text)
        val hospitalName = extractHospitalName(text)
        val invoiceDate = extractInvoiceDate(text)
        val billNumber = extractBillNumber(text)
        val lineItems = extractLineItems(text)
        val totalAmount = extractTotalAmount(text)
        
        // Calculate category totals
        val doctorFees = calculateDoctorFees(lineItems)
        val roomRent = calculateRoomRent(lineItems)
        val medicineExpenses = calculateMedicineExpenses(lineItems)
        val diagnostics = calculateDiagnostics(lineItems)
        
        // Calculate confidence based on how many fields were found
        val confidence = calculateConfidence(
            patientName, hospitalName, invoiceDate, billNumber, 
            lineItems, totalAmount
        )
        
        return BillData(
            patientName = patientName,
            hospitalName = hospitalName,
            invoiceDate = invoiceDate,
            billNumber = billNumber,
            lineItems = lineItems,
            totalAmount = totalAmount,
            doctorFees = doctorFees,
            roomRent = roomRent,
            medicineExpenses = medicineExpenses,
            diagnostics = diagnostics,
            confidence = confidence
        )
    }
    
    private fun extractPatientName(text: String): String {
        val patterns = listOf(
            "Patient[:\\s]+([A-Za-z\\s]+)",
            "Name[:\\s]+([A-Za-z\\s]+)",
            "Patient Name[:\\s]+([A-Za-z\\s]+)"
        )
        
        for (pattern in patterns) {
            val matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(text)
            if (matcher.find()) {
                return matcher.group(1)?.trim() ?: ""
            }
        }
        return ""
    }
    
    private fun extractHospitalName(text: String): String {
        val patterns = listOf(
            "Hospital[:\\s]+([A-Za-z\\s]+(?:Hospital|Clinic|Medical Center|Healthcare))",
            "([A-Za-z\\s]+(?:Hospital|Clinic|Medical Center|Healthcare))",
        )
        
        for (pattern in patterns) {
            val matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(text)
            if (matcher.find()) {
                return matcher.group(1)?.trim() ?: ""
            }
        }
        return ""
    }
    
    private fun extractInvoiceDate(text: String): String {
        val patterns = listOf(
            "Date[:\\s]+(\\d{1,2}[-/]\\d{1,2}[-/]\\d{2,4})",
            "Invoice Date[:\\s]+(\\d{1,2}[-/]\\d{1,2}[-/]\\d{2,4})",
            "(\\d{1,2}[-/]\\d{1,2}[-/]\\d{2,4})"
        )
        
        for (pattern in patterns) {
            val matcher = Pattern.compile(pattern).matcher(text)
            if (matcher.find()) {
                return matcher.group(1)?.trim() ?: ""
            }
        }
        return ""
    }
    
    private fun extractBillNumber(text: String): String {
        val patterns = listOf(
            "Bill No[:\\s#]+(\\w+)",
            "Invoice No[:\\s#]+(\\w+)",
            "Receipt No[:\\s#]+(\\w+)"
        )
        
        for (pattern in patterns) {
            val matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(text)
            if (matcher.find()) {
                return matcher.group(1)?.trim() ?: ""
            }
        }
        return ""
    }
    
    private fun extractLineItems(text: String): List<LineItem> {
        val items = mutableListOf<LineItem>()
        
        // Try to find table-like structures
        val lines = text.split("\n")
        var inTable = false
        
        for (line in lines) {
            // Look for header keywords
            if (line.contains("Description", ignoreCase = true) && 
                (line.contains("Amount", ignoreCase = true) || 
                 line.contains("Rate", ignoreCase = true))) {
                inTable = true
                continue
            }
            
            if (inTable) {
                // Try to extract line item data
                // Pattern: text followed by numbers
                val pattern = "(.+?)\\s+(\\d+)\\s+([\\d,.]+)\\s+([\\d,.]+)"
                val matcher = Pattern.compile(pattern).matcher(line)
                
                if (matcher.find()) {
                    try {
                        val description = matcher.group(1)?.trim() ?: ""
                        val quantity = matcher.group(2)?.toIntOrNull() ?: 1
                        val rate = matcher.group(3)?.replace(",", "")?.toDoubleOrNull() ?: 0.0
                        val amount = matcher.group(4)?.replace(",", "")?.toDoubleOrNull() ?: 0.0
                        
                        if (description.isNotEmpty()) {
                            items.add(LineItem(description, quantity, rate, amount))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing line item: $line", e)
                    }
                }
                
                // Stop if we hit total or similar keywords
                if (line.contains("Total", ignoreCase = true) || 
                    line.contains("Grand Total", ignoreCase = true)) {
                    break
                }
            }
        }
        
        Log.d(TAG, "Extracted ${items.size} line items")
        return items
    }
    
    private fun extractTotalAmount(text: String): Double {
        val patterns = listOf(
            "Total[:\\s]+₹?\\s*([\\d,.]+)",
            "Grand Total[:\\s]+₹?\\s*([\\d,.]+)",
            "Amount[:\\s]+₹?\\s*([\\d,.]+)"
        )
        
        for (pattern in patterns) {
            val matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(text)
            if (matcher.find()) {
                return matcher.group(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0
            }
        }
        return 0.0
    }
    
    private fun calculateDoctorFees(items: List<LineItem>): Double {
        return items.filter { 
            it.description.contains("doctor", ignoreCase = true) ||
            it.description.contains("consultation", ignoreCase = true) ||
            it.description.contains("physician", ignoreCase = true)
        }.sumOf { it.amount }
    }
    
    private fun calculateRoomRent(items: List<LineItem>): Double {
        return items.filter { 
            it.description.contains("room", ignoreCase = true) ||
            it.description.contains("bed", ignoreCase = true) ||
            it.description.contains("accommodation", ignoreCase = true)
        }.sumOf { it.amount }
    }
    
    private fun calculateMedicineExpenses(items: List<LineItem>): Double {
        return items.filter { 
            it.description.contains("medicine", ignoreCase = true) ||
            it.description.contains("drug", ignoreCase = true) ||
            it.description.contains("tablet", ignoreCase = true) ||
            it.description.contains("injection", ignoreCase = true) ||
            it.description.contains("pharmacy", ignoreCase = true)
        }.sumOf { it.amount }
    }
    
    private fun calculateDiagnostics(items: List<LineItem>): Double {
        return items.filter { 
            it.description.contains("test", ignoreCase = true) ||
            it.description.contains("lab", ignoreCase = true) ||
            it.description.contains("x-ray", ignoreCase = true) ||
            it.description.contains("scan", ignoreCase = true) ||
            it.description.contains("diagnostic", ignoreCase = true)
        }.sumOf { it.amount }
    }
    
    private fun calculateConfidence(
        patientName: String,
        hospitalName: String,
        invoiceDate: String,
        billNumber: String,
        lineItems: List<LineItem>,
        totalAmount: Double
    ): Float {
        var score = 0f
        var maxScore = 6f
        
        if (patientName.isNotEmpty()) score += 1f
        if (hospitalName.isNotEmpty()) score += 1f
        if (invoiceDate.isNotEmpty()) score += 1f
        if (billNumber.isNotEmpty()) score += 1f
        if (lineItems.isNotEmpty()) score += 1f
        if (totalAmount > 0) score += 1f
        
        return (score / maxScore) * 100f
    }
}
