package com.easyreceiptanalyzer.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ReceiptEntityTest {

    @Test
    fun receiptEntity_with_all_fields_creates_correctly() {
        // Given
        val id = 1L
        val storeName = "Mercadona"
        val purchaseDate = 123456789L
        val totalCents = 2515L
        val rawText = "Ticket text"
        val createdAt = 987654321L

        // When
        val receipt = ReceiptEntity(
            id = id,
            storeName = storeName,
            purchaseDate = purchaseDate,
            totalCents = totalCents,
            rawText = rawText,
            createdAt = createdAt
        )

        // Then
        assertEquals(id, receipt.id)
        assertEquals(storeName, receipt.storeName)
        assertEquals(purchaseDate, receipt.purchaseDate)
        assertEquals(totalCents, receipt.totalCents)
        assertEquals(rawText, receipt.rawText)
        assertEquals(createdAt, receipt.createdAt)
    }

    @Test
    fun receiptEntity_with_optional_null_fields_creates_correctly() {
        // Given
        val storeName = "Carrefour"

        // When
        val receipt = ReceiptEntity(
            id = 0,
            storeName = storeName,
            purchaseDate = null,
            totalCents = null,
            rawText = null
        )

        // Then
        assertEquals(0L, receipt.id)
        assertEquals(storeName, receipt.storeName)
        assertEquals(null, receipt.purchaseDate)
        assertEquals(null, receipt.totalCents)
        assertEquals(null, receipt.rawText)
        assertNotNull(receipt.createdAt)
    }

    @Test
    fun receiptEntity_defaults_createdAt_to_current_time() {
        // Given
        val beforeCreation = System.currentTimeMillis()

        // When
        val receipt = ReceiptEntity(
            id = 0,
            storeName = "Lidl"
        )

        val afterCreation = System.currentTimeMillis()

        // Then
        assertNotNull(receipt.createdAt)
        assertTrue(receipt.createdAt >= beforeCreation)
        assertTrue(receipt.createdAt <= afterCreation)
    }

    @Test
    fun receiptEntity_defaults_id_to_zero() {
        // When
        val receipt = ReceiptEntity(
            storeName = "Test Store"
        )

        // Then
        assertEquals(0L, receipt.id)
    }

    private fun assertTrue(condition: Boolean) {
        if (!condition) {
            throw AssertionError("Expected true but was false")
        }
    }
}