package com.easyreceiptanalyzer.data

data class ParsedReceiptItem(
    val name: String,
    val price: Double
)

data class ParsedReceipt(
    val storeName: String,
    val date: String,
    val total: Double,
    val items: List<ParsedReceiptItem>
)
