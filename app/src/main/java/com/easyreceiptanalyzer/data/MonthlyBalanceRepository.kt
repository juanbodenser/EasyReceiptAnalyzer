package com.easyreceiptanalyzer.data

import com.easyreceiptanalyzer.data.local.MonthlyBalanceDao
import com.easyreceiptanalyzer.data.local.MonthlyBalanceEntity
import javax.inject.Inject

class MonthlyBalanceRepository @Inject constructor(
    private val monthlyBalanceDao: MonthlyBalanceDao,
    private val analysisRepository: AnalysisRepository
) {

    /**
     * Calcula el balance del mes sin guardarlo.
     */
    suspend fun calculateBalance(year: Int, month: Int): Long {
        val totals = analysisRepository.getMonthTotals(year, month)
        return totals.totalIncomeCents - totals.totalExpenseCents
    }

    /**
     * Cierra el mes: calcula el balance y lo guarda en la BD.
     * Si ya estaba cerrado, lo sobreescribe (equivale a reabrir y volver a cerrar).
     */
    suspend fun closeMonth(year: Int, month: Int) {
        val totals = analysisRepository.getMonthTotals(year, month)
        val balance = totals.totalIncomeCents - totals.totalExpenseCents
        val yearMonth = String.format("%04d-%02d", year, month + 1)

        monthlyBalanceDao.save(
            MonthlyBalanceEntity(
                yearMonth = yearMonth,
                year = year,
                month = month,
                totalIncomeCents = totals.totalIncomeCents,
                totalExpenseCents = totals.totalExpenseCents,
                balanceCents = balance,
                closedAt = System.currentTimeMillis()
            )
        )
    }

    /**
     * Reabre el mes: elimina el registro de la BD.
     * El balance se recalculará en tiempo real hasta que se vuelva a cerrar.
     */
    suspend fun reopenMonth(year: Int, month: Int) {
        val yearMonth = String.format("%04d-%02d", year, month + 1)
        monthlyBalanceDao.deleteByMonth(yearMonth)
    }

    suspend fun getClosedBalance(year: Int, month: Int): MonthlyBalanceEntity? {
        val yearMonth = String.format("%04d-%02d", year, month + 1)
        return monthlyBalanceDao.getByMonth(yearMonth)
    }

    suspend fun getAllClosedBalances(): List<MonthlyBalanceEntity> {
        return monthlyBalanceDao.getAll()
    }

    suspend fun getTotalSavings(): Long {
        return monthlyBalanceDao.getTotalSavings() ?: 0L
    }
}
