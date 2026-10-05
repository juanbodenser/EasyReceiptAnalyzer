package com.easyreceiptanalyzer.data.bank

/**
 * Representa un movimiento bancario parseado del extracto TXT del banco.
 */
data class BankMovement(
    val date: Long,             // Timestamp en milisegundos (fecha de operación)
    val concept: String,        // Nombre del comercio limpio
    val rawConcept: String,     // Concepto original completo (por si hace falta)
    val amountCents: Long,      // Importe en céntimos (siempre positivo)
    val isExpense: Boolean,     // true = gasto, false = ingreso
    val isSuspicious: Boolean = false
)
