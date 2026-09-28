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

    @Transaction
    suspend fun insertReceiptWithItems(receipt: ReceiptEntity, items: List<ItemEntity>): Long {
        val receiptId = insert(receipt)
        insertItems(items.map { it.copy(receiptId = receiptId) })
        return receiptId
    }
}