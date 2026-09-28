package com.easyreceiptanalyzer.data

import com.easyreceiptanalyzer.data.local.AnalysisDao
import com.easyreceiptanalyzer.data.local.CategoryTotal
import com.easyreceiptanalyzer.data.local.MonthlySummary
import com.easyreceiptanalyzer.data.local.ProductTotal
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