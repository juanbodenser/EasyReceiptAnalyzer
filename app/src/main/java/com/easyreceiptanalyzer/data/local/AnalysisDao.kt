package com.easyreceiptanalyzer.data.local

import androidx.room.Dao
import androidx.room.Query

data class MonthlySummary(
    val numTickets: Int,
    val totalCents: Long,
    val avgTicketCents: Long
)

data class CategoryTotal(
    val category: String,
    val totalCents: Long,
    val numItems: Int
)

data class ProductTotal(
    val name: String,
    val veces: Int,
    val totalCents: Long
)

@Dao
interface AnalysisDao {

    @Query("""
        SELECT 
            COUNT(*) as numTickets,
            COALESCE(SUM(totalCents), 0) as totalCents,
            COALESCE(AVG(totalCents), 0) as avgTicketCents
        FROM receipts
        WHERE purchaseDate BETWEEN :startMillis AND :endMillis
    """)
    suspend fun getMonthlySummary(startMillis: Long, endMillis: Long): MonthlySummary

    @Query("""
        SELECT 
            i.category as category,
            SUM(i.priceCents) as totalCents,
            COUNT(*) as numItems
        FROM items i
        INNER JOIN receipts r ON i.receiptId = r.id
        WHERE r.purchaseDate BETWEEN :startMillis AND :endMillis
        GROUP BY i.category
        ORDER BY totalCents DESC
    """)
    suspend fun getCategoryTotals(startMillis: Long, endMillis: Long): List<CategoryTotal>

    @Query("""
        SELECT 
            i.name as name,
            COUNT(*) as veces,
            SUM(i.priceCents) as totalCents
        FROM items i
        INNER JOIN receipts r ON i.receiptId = r.id
        WHERE r.purchaseDate BETWEEN :startMillis AND :endMillis
        GROUP BY i.name
        ORDER BY veces DESC
        LIMIT 5
    """)
    suspend fun getTopProducts(startMillis: Long, endMillis: Long): List<ProductTotal>

    @Query("""
        SELECT 
            i.name as name,
            SUM(i.priceCents) as totalCents,
            COUNT(*) as veces
        FROM items i
        INNER JOIN receipts r ON i.receiptId = r.id
        WHERE r.purchaseDate BETWEEN :startMillis AND :endMillis
          AND i.category = :category
        GROUP BY i.name
        ORDER BY totalCents DESC
    """)
    suspend fun getProductsByCategory(
        startMillis: Long,
        endMillis: Long,
        category: String
    ): List<ProductTotal>
}