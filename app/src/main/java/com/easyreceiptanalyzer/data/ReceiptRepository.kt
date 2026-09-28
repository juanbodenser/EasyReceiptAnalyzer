package com.easyreceiptanalyzer.data

import com.easyreceiptanalyzer.data.local.ItemEntity
import com.easyreceiptanalyzer.data.local.ReceiptDao
import com.easyreceiptanalyzer.data.local.ReceiptEntity
import kotlinx.coroutines.flow.Flow

class ReceiptRepository(
    private val receiptDao: ReceiptDao
) {
    fun observeReceipts(): Flow<List<ReceiptEntity>> = receiptDao.observeAll()

    suspend fun getReceipt(id: Long): ReceiptEntity? = receiptDao.findById(id)

    suspend fun getItems(receiptId: Long): List<ItemEntity> =
        receiptDao.getItemsForReceipt(receiptId)

    suspend fun saveReceipt(receipt: ReceiptEntity, items: List<ItemEntity>): Long =
        receiptDao.insertReceiptWithItems(receipt, items)

    suspend fun deleteReceipt(id: Long) = receiptDao.deleteById(id)
}