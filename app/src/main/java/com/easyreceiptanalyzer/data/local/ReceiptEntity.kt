package com.easyreceiptanalyzer.data.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val storeName: String,
    val purchaseDate: Long? = null,
    val totalCents: Long? = null,
    val rawText: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
