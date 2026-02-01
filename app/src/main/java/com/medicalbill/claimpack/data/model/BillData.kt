package com.medicalbill.claimpack.data.model

data class BillData(
    val patientName: String = "",
    val hospitalName: String = "",
    val invoiceDate: String = "",
    val billNumber: String = "",
    val lineItems: List<LineItem> = emptyList(),
    val taxes: Double = 0.0,
    val totalAmount: Double = 0.0,
    val doctorFees: Double = 0.0,
    val roomRent: Double = 0.0,
    val medicineExpenses: Double = 0.0,
    val diagnostics: Double = 0.0,
    val confidence: Float = 0.0f,
    val sourceFilePath: String = ""
)

data class LineItem(
    val description: String,
    val quantity: Int,
    val rate: Double,
    val amount: Double
)
