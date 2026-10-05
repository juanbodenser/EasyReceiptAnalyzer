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

    suspend fun saveReceipt(receipt: ReceiptEntity, items: List<ItemEntity> = emptyList()): Long =
        receiptDao.insertReceiptWithItems(receipt, items)

    suspend fun deleteReceipt(id: Long) = receiptDao.deleteById(id)

    suspend fun findSimilarReceipt(purchaseDate: Long?, totalCents: Long?): ReceiptEntity? =
        receiptDao.findSimilar(purchaseDate, totalCents)

    suspend fun updateItemCategory(itemId: Long, newCategory: String) {
        receiptDao.updateItemCategory(itemId, newCategory)
    }

    /**
     * Busca un ticket escaneado que coincida con un movimiento bancario.
     * Usa tolerancia de ±2 días en la fecha para tener en cuenta el desfase
     * entre la fecha de compra y la fecha de cargo en el banco.
     *
     * @return El ticket duplicado, o null si no hay ninguno.
     */
    suspend fun findScanDuplicate(
        date: Long,
        totalCents: Long
    ): ReceiptEntity? {
        val twoDaysMillis = 2L * 24 * 60 * 60 * 1000
        return receiptDao.findScanDuplicate(
            minDate = date - twoDaysMillis,
            maxDate = date + twoDaysMillis,
            totalCents = totalCents
        )
    }

    /**
     * Guarda un movimiento bancario como ReceiptEntity con source = "bank".
     * No crea items (los movimientos bancarios no tienen desglose de productos).
     * La categoría del movimiento se guarda en el campo rawText.
     */
    suspend fun saveBankMovement(
        concept: String,
        date: Long,
        amountCents: Long,
        category: String,
        isExpense: Boolean = true
    ): Long {
        val entity = ReceiptEntity(
            storeName = concept,
            purchaseDate = date,
            totalCents = amountCents,
            rawText = category,
            source = "bank",
            isExpense = isExpense
        )
        return receiptDao.insert(entity)
    }

    suspend fun updateReceiptCategory(receiptId: Long, category: String) {
        receiptDao.updateReceiptCategory(receiptId, category)
    }

    suspend fun updateReceiptNotes(receiptId: Long, notes: String?) {
        receiptDao.updateReceiptNotes(receiptId, notes)
    }
}