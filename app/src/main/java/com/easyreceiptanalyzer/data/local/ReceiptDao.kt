package com.easyreceiptanalyzer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptDao {

    @Insert
    suspend fun insert(receipt: ReceiptEntity): Long

    @Insert
    suspend fun insertItems(items: List<ItemEntity>)

    @Query("SELECT * FROM receipts ORDER BY purchaseDate DESC, id DESC")
    fun observeAll(): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE id = :id")
    suspend fun findById(id: Long): ReceiptEntity?

    @Query("SELECT * FROM items WHERE receiptId = :receiptId")
    suspend fun getItemsForReceipt(receiptId: Long): List<ItemEntity>

    @Query("DELETE FROM receipts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("""
        SELECT * FROM receipts
        WHERE purchaseDate = :purchaseDate
          AND totalCents = :totalCents
        LIMIT 1
    """)
    suspend fun findSimilar(purchaseDate: Long?, totalCents: Long?): ReceiptEntity?

    @Transaction
    suspend fun insertReceiptWithItems(receipt: ReceiptEntity, items: List<ItemEntity>): Long {
        val receiptId = insert(receipt)
        insertItems(items.map { it.copy(receiptId = receiptId) })
        return receiptId
    }

    @Query("UPDATE items SET category = :newCategory WHERE id = :itemId")
    suspend fun updateItemCategory(itemId: Long, newCategory: String)

    @Query("""
        SELECT * FROM receipts
        WHERE source = 'scan'
          AND purchaseDate BETWEEN :minDate AND :maxDate
          AND totalCents = :totalCents
        LIMIT 1
    """)
    suspend fun findScanDuplicate(
        minDate: Long,
        maxDate: Long,
        totalCents: Long
    ): ReceiptEntity?

    @Query("UPDATE receipts SET rawText = :category WHERE id = :receiptId")
    suspend fun updateReceiptCategory(receiptId: Long, category: String)

    @Query("UPDATE receipts SET notes = :notes WHERE id = :receiptId")
    suspend fun updateReceiptNotes(receiptId: Long, notes: String?)
}