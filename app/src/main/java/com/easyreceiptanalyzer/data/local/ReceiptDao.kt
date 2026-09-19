package com.easyreceiptanalyzer.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptDao {
    @Insert
    suspend fun insert(receipt: ReceiptEntity): Long

    @Query("SELECT * FROM receipts ORDER BY purchaseDate DESC, id DESC")
    fun observeAll(): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE id = :id")
    suspend fun findById(id: Long): ReceiptEntity?
}
