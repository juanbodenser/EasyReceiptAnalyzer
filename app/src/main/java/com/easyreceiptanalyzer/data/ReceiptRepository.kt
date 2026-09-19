package com.easyreceiptanalyzer.data

import com.easyreceiptanalyzer.data.local.ReceiptDao
import com.easyreceiptanalyzer.data.local.ReceiptEntity
import kotlinx.coroutines.flow.Flow

class ReceiptRepository(
    private val receiptDao: ReceiptDao
) {
    fun observeReceipts(): Flow<List<ReceiptEntity>> = receiptDao.observeAll()

    suspend fun saveReceipt(receipt: ReceiptEntity): Long = receiptDao.insert(receipt)

    suspend fun getReceipt(id: Long): ReceiptEntity? = receiptDao.findById(id)
}
