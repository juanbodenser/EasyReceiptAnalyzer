package com.easyreceiptanalyzer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val storeName: String,
    val purchaseDate: Long? = null,
    val totalCents: Long? = null,
    val rawText: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val source: String = "scan",
    val notes: String? = null,
    val isExpense: Boolean = true
)
