package com.easyreceiptanalyzer.data

import com.easyreceiptanalyzer.data.local.ReceiptDao
import com.easyreceiptanalyzer.data.local.ReceiptEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class ReceiptRepositoryTest {

    @Mock
    private lateinit var receiptDao: ReceiptDao

    private lateinit var receiptRepository: ReceiptRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        receiptRepository = ReceiptRepository(receiptDao)
    }

    @Test
    fun observeReceipts_returns_flow_from_dao() = runTest {
        // Given
        val expectedReceipts = listOf(
            ReceiptEntity(
                id = 1,
                storeName = "Mercadona",
                purchaseDate = 123456789L,
                totalCents = 2515L,
                rawText = "Ticket text",
                createdAt = 987654321L
            )
        )
        `when`(receiptDao.observeAll()).thenReturn(flowOf(expectedReceipts))

        // When
        val result = receiptRepository.observeReceipts().first()

        // Then
        assertEquals(expectedReceipts, result)
    }

    @Test
    fun saveReceipt_calls_dao_insert() = runTest {
        // Given
        val receipt = ReceiptEntity(
            id = 0,
            storeName = "Carrefour",
            purchaseDate = null,
            totalCents = null,
            rawText = null
        )
        val expectedId = 42L
        `when`(receiptDao.insert(receipt)).thenReturn(expectedId)

        // When
        val result = receiptRepository.saveReceipt(receipt)

        // Then
        assertEquals(expectedId, result)
        verify(receiptDao).insert(receipt)
    }

    @Test
    fun getReceipt_calls_dao_findById() = runTest {
        // Given
        val receiptId = 123L
        val expectedReceipt = ReceiptEntity(
            id = receiptId,
            storeName = "Lidl",
            purchaseDate = 111111111L,
            totalCents = 999L,
            rawText = "Another ticket",
            createdAt = 222222222L
        )
        `when`(receiptDao.findById(receiptId)).thenReturn(expectedReceipt)

        // When
        val result = receiptRepository.getReceipt(receiptId)

        // Then
        assertEquals(expectedReceipt, result)
        verify(receiptDao).findById(receiptId)
    }
}