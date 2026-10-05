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

data class GeneralExpenseSummary(
    val numMovements: Int,
    val totalCents: Long,
    val avgMovementCents: Long
)

data class ExpenseCategoryTotal(
    val category: String,       // nombre del ExpenseCategory (lo que hay en rawText)
    val totalCents: Long,
    val numMovements: Int
)

data class TopMovement(
    val concept: String,
    val date: Long,
    val amountCents: Long
)

data class ExpenseMovementDetail(
    val id: Long,
    val concept: String,
    val date: Long,
    val amountCents: Long,
    val notes: String?
)

data class MonthTotals(
    val totalIncomeCents: Long,
    val totalExpenseCents: Long
)

data class IncomeDetail(
    val concept: String,
    val date: Long,
    val amountCents: Long,
    val notes: String?
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
          AND source = 'scan'
          AND isExpense = 1
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
          AND r.source = 'scan'
          AND r.isExpense = 1
        GROUP BY i.category
        ORDER BY totalCents DESC
    """)
    suspend fun getCategoryTotals(startMillis: Long, endMillis: Long): List<CategoryTotal>

    @Query("""
        SELECT 
            UPPER(TRIM(i.name)) as name,
            COUNT(*) as veces,
            SUM(i.priceCents) as totalCents
        FROM items i
        INNER JOIN receipts r ON i.receiptId = r.id
        WHERE r.purchaseDate BETWEEN :startMillis AND :endMillis
          AND r.source = 'scan'
          AND r.isExpense = 1
        GROUP BY UPPER(TRIM(i.name))
        ORDER BY veces DESC
        LIMIT 5
    """)
    suspend fun getTopProducts(startMillis: Long, endMillis: Long): List<ProductTotal>

    @Query("""
        SELECT 
            UPPER(TRIM(i.name)) as name,
            SUM(i.priceCents) as totalCents,
            COUNT(*) as veces
        FROM items i
        INNER JOIN receipts r ON i.receiptId = r.id
        WHERE r.purchaseDate BETWEEN :startMillis AND :endMillis
          AND i.category = :category
          AND r.source = 'scan'
          AND r.isExpense = 1
        GROUP BY UPPER(TRIM(i.name))
        ORDER BY totalCents DESC
    """)
    suspend fun getProductsByCategory(
        startMillis: Long,
        endMillis: Long,
        category: String
    ): List<ProductTotal>

    @Query("SELECT MAX(purchaseDate) FROM receipts")
    suspend fun getLastPurchaseDate(): Long?

    @Query("""
        SELECT 
            COUNT(*) as numMovements,
            COALESCE(SUM(totalCents), 0) as totalCents,
            COALESCE(AVG(totalCents), 0) as avgMovementCents
        FROM receipts
        WHERE purchaseDate BETWEEN :startMillis AND :endMillis
          AND source = 'bank'
          AND isExpense = 1
    """)
    suspend fun getGeneralExpenseSummary(startMillis: Long, endMillis: Long): GeneralExpenseSummary

    @Query("""
        SELECT 
            COALESCE(rawText, 'OTROS') as category,
            SUM(totalCents) as totalCents,
            COUNT(*) as numMovements
        FROM receipts
        WHERE purchaseDate BETWEEN :startMillis AND :endMillis
          AND source = 'bank'
          AND isExpense = 1
        GROUP BY COALESCE(rawText, 'OTROS')
        ORDER BY totalCents DESC
    """)
    suspend fun getExpenseCategoryTotals(startMillis: Long, endMillis: Long): List<ExpenseCategoryTotal>

    @Query("""
        SELECT 
            storeName as concept,
            purchaseDate as date,
            totalCents as amountCents
        FROM receipts
        WHERE purchaseDate BETWEEN :startMillis AND :endMillis
          AND source = 'bank'
          AND isExpense = 1
        ORDER BY totalCents DESC
        LIMIT 5
    """)
    suspend fun getTopMovements(startMillis: Long, endMillis: Long): List<TopMovement>

    @Query("""
        SELECT 
            id,
            storeName as concept,
            purchaseDate as date,
            totalCents as amountCents,
            notes
        FROM receipts
        WHERE purchaseDate BETWEEN :startMillis AND :endMillis
          AND source = 'bank'
          AND COALESCE(rawText, 'OTROS') = :category
        ORDER BY purchaseDate DESC, id DESC
    """)
    suspend fun getMovementsByExpenseCategory(
        startMillis: Long,
        endMillis: Long,
        category: String
    ): List<ExpenseMovementDetail>

    @Query("""
        SELECT 
            COALESCE(SUM(CASE WHEN isExpense = 0 THEN totalCents ELSE 0 END), 0) as totalIncomeCents,
            COALESCE(SUM(CASE WHEN isExpense = 1 THEN totalCents ELSE 0 END), 0) as totalExpenseCents
        FROM receipts
        WHERE purchaseDate BETWEEN :startMillis AND :endMillis
    """)
    suspend fun getMonthTotals(startMillis: Long, endMillis: Long): MonthTotals

    @Query("""
        SELECT 
            storeName as concept,
            purchaseDate as date,
            totalCents as amountCents,
            notes
        FROM receipts
        WHERE purchaseDate BETWEEN :startMillis AND :endMillis
          AND isExpense = 0
        ORDER BY purchaseDate DESC, id DESC
    """)
    suspend fun getIncomeDetails(startMillis: Long, endMillis: Long): List<IncomeDetail>
}
