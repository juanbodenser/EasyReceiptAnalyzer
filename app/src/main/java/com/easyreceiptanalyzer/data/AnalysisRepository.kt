package com.easyreceiptanalyzer.data

import com.easyreceiptanalyzer.data.local.AnalysisDao
import com.easyreceiptanalyzer.data.local.CategoryTotal
import com.easyreceiptanalyzer.data.local.ExpenseCategoryTotal
import com.easyreceiptanalyzer.data.local.ExpenseMovementDetail
import com.easyreceiptanalyzer.data.local.GeneralExpenseSummary
import com.easyreceiptanalyzer.data.local.IncomeDetail
import com.easyreceiptanalyzer.data.local.MonthTotals
import com.easyreceiptanalyzer.data.local.MonthlySummary
import com.easyreceiptanalyzer.data.local.ProductTotal
import com.easyreceiptanalyzer.data.local.TopMovement
import java.util.Calendar
import javax.inject.Inject

class AnalysisRepository @Inject constructor(
    private val analysisDao: AnalysisDao
) {

    suspend fun getMonthlySummary(year: Int, month: Int): MonthlySummary {
        val (start, end) = monthRange(year, month)
        return analysisDao.getMonthlySummary(start, end)
    }

    suspend fun getCategoryTotals(year: Int, month: Int): List<CategoryTotal> {
        val (start, end) = monthRange(year, month)
        return analysisDao.getCategoryTotals(start, end)
    }

    suspend fun getTopProducts(year: Int, month: Int): List<ProductTotal> {
        val (start, end) = monthRange(year, month)
        return analysisDao.getTopProducts(start, end)
    }

    suspend fun getProductsByCategory(year: Int, month: Int, category: String): List<ProductTotal> {
        val (start, end) = monthRange(year, month)
        return analysisDao.getProductsByCategory(start, end, category)
    }

    suspend fun getLastPurchaseDate(): Long? = analysisDao.getLastPurchaseDate()

    suspend fun getGeneralExpenseSummary(year: Int, month: Int): GeneralExpenseSummary {
        val (start, end) = monthRange(year, month)
        return analysisDao.getGeneralExpenseSummary(start, end)
    }

    suspend fun getExpenseCategoryTotals(year: Int, month: Int): List<ExpenseCategoryTotal> {
        val (start, end) = monthRange(year, month)
        return analysisDao.getExpenseCategoryTotals(start, end)
    }

    suspend fun getTopMovements(year: Int, month: Int): List<TopMovement> {
        val (start, end) = monthRange(year, month)
        return analysisDao.getTopMovements(start, end)
    }

    suspend fun getMovementsByExpenseCategory(
        year: Int,
        month: Int,
        category: String
    ): List<ExpenseMovementDetail> {
        val (start, end) = monthRange(year, month)
        return analysisDao.getMovementsByExpenseCategory(start, end, category)
    }

    suspend fun getMonthTotals(year: Int, month: Int): MonthTotals {
        val (start, end) = monthRange(year, month)
        return analysisDao.getMonthTotals(start, end)
    }

    suspend fun getIncomeDetails(year: Int, month: Int): List<IncomeDetail> {
        val (start, end) = monthRange(year, month)
        return analysisDao.getIncomeDetails(start, end)
    }

    private fun monthRange(year: Int, month: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.clear()
        cal.set(year, month, 1, 0, 0, 0)
        val start = cal.timeInMillis

        cal.add(Calendar.MONTH, 1)
        cal.add(Calendar.MILLISECOND, -1)
        val end = cal.timeInMillis

        return start to end
    }
}
