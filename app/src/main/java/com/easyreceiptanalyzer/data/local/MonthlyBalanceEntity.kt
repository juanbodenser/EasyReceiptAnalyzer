package com.easyreceiptanalyzer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_balances")
data class MonthlyBalanceEntity(
    @PrimaryKey val yearMonth: String,
    val year: Int,
    val month: Int,
    val totalIncomeCents: Long,
    val totalExpenseCents: Long,
    val balanceCents: Long,
    val closedAt: Long
)
