package com.easyreceiptanalyzer.ui.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easyreceiptanalyzer.data.AnalysisRepository
import com.easyreceiptanalyzer.data.MonthlyBalanceRepository
import com.easyreceiptanalyzer.data.local.CategoryTotal
import com.easyreceiptanalyzer.data.local.ExpenseCategoryTotal
import com.easyreceiptanalyzer.data.local.ExpenseMovementDetail
import com.easyreceiptanalyzer.data.local.GeneralExpenseSummary
import com.easyreceiptanalyzer.data.local.IncomeDetail
import com.easyreceiptanalyzer.data.local.MonthlyBalanceEntity
import com.easyreceiptanalyzer.data.local.MonthlySummary
import com.easyreceiptanalyzer.data.local.ProductTotal
import com.easyreceiptanalyzer.data.local.TopMovement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

enum class AnalysisMode(val label: String) {
    EXPENSES("GASTOS"),
    SUMMARY("RESUMEN")
}

enum class HistorySortMode(val label: String) {
    DATE_DESC("FECHA ↓"),
    DATE_ASC("FECHA ↑"),
    AMOUNT_DESC("MONTO ↓"),
    AMOUNT_ASC("MONTO ↑")
}

data class AnalysisUiState(
    val year: Int,
    val month: Int,
    val mode: AnalysisMode = AnalysisMode.EXPENSES,
    val isLoading: Boolean = false,
    // Supermercado
    val summary: MonthlySummary? = null,
    val categories: List<CategoryTotal> = emptyList(),
    val topProducts: List<ProductTotal> = emptyList(),
    // Gastos generales
    val generalSummary: GeneralExpenseSummary? = null,
    val expenseCategories: List<ExpenseCategoryTotal> = emptyList(),
    val topMovements: List<TopMovement> = emptyList(),
    // Balance
    val balanceIncomeCents: Long = 0L,
    val balanceExpenseCents: Long = 0L,
    val balanceCents: Long = 0L,
    val balanceClosed: Boolean = false,
    val closedBalances: List<MonthlyBalanceEntity> = emptyList(),
    val totalSavings: Long = 0L,
    val incomeDetails: List<IncomeDetail> = emptyList(),
    val historySortMode: HistorySortMode = HistorySortMode.DATE_DESC
)

data class CategoryDetail(
    val categoryName: String,           // "SNACKS", "LACTEOS", etc.
    val products: List<ProductTotal>
)

data class ExpenseCategoryDetail(
    val categoryName: String,
    val movements: List<ExpenseMovementDetail>
)

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val analysisRepository: AnalysisRepository,
    private val monthlyBalanceRepository: MonthlyBalanceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AnalysisUiState(
        year = Calendar.getInstance().get(Calendar.YEAR),
        month = Calendar.getInstance().get(Calendar.MONTH)
    ))
    val state: StateFlow<AnalysisUiState> = _state.asStateFlow()

    private val _categoryDetail = MutableStateFlow<CategoryDetail?>(null)
    val categoryDetail: StateFlow<CategoryDetail?> = _categoryDetail.asStateFlow()

    private val _expenseCategoryDetail = MutableStateFlow<ExpenseCategoryDetail?>(null)
    val expenseCategoryDetail: StateFlow<ExpenseCategoryDetail?> = _expenseCategoryDetail.asStateFlow()

    fun refresh() {
        load()
    }

    fun setMode(mode: AnalysisMode) {
        _state.value = _state.value.copy(mode = mode)
    }

    fun closeCurrentMonth() {
        viewModelScope.launch {
            val current = _state.value
            monthlyBalanceRepository.closeMonth(current.year, current.month)
            load()
        }
    }

    fun reopenCurrentMonth() {
        viewModelScope.launch {
            val current = _state.value
            monthlyBalanceRepository.reopenMonth(current.year, current.month)
            load()
        }
    }

    fun previousMonth() {
        val current = _state.value
        val newMonth = if (current.month == 0) 11 else current.month - 1
        val newYear = if (current.month == 0) current.year - 1 else current.year
        _state.value = current.copy(year = newYear, month = newMonth)
        load()
    }

    fun nextMonth() {
        val current = _state.value
        val newMonth = if (current.month == 11) 0 else current.month + 1
        val newYear = if (current.month == 11) current.year + 1 else current.year
        _state.value = current.copy(year = newYear, month = newMonth)
        load()
    }

    fun openCategory(categoryName: String) {
        viewModelScope.launch {
            val current = _state.value
            val products = analysisRepository.getProductsByCategory(
                current.year, current.month, categoryName
            )
            _categoryDetail.value = CategoryDetail(categoryName, products)
        }
    }

    fun closeCategory() {
        _categoryDetail.value = null
    }

    fun openExpenseCategory(categoryName: String) {
        viewModelScope.launch {
            val current = _state.value
            val movements = analysisRepository.getMovementsByExpenseCategory(
                current.year, current.month, categoryName
            )
            _expenseCategoryDetail.value = ExpenseCategoryDetail(categoryName, movements)
        }
    }

    fun closeExpenseCategory() {
        _expenseCategoryDetail.value = null
    }

    fun setHistorySortMode(mode: HistorySortMode) {
        _state.value = _state.value.copy(historySortMode = mode)
    }

    private fun load() {
        viewModelScope.launch {
            val current = _state.value
            _state.value = current.copy(isLoading = true)
            try {
                val summary = analysisRepository.getMonthlySummary(current.year, current.month)
                val categories = analysisRepository.getCategoryTotals(current.year, current.month)
                val topProducts = analysisRepository.getTopProducts(current.year, current.month)
                val generalSummary = analysisRepository.getGeneralExpenseSummary(current.year, current.month)
                val expenseCategories = analysisRepository.getExpenseCategoryTotals(current.year, current.month)
                val topMovements = analysisRepository.getTopMovements(current.year, current.month)
                val monthTotals = analysisRepository.getMonthTotals(current.year, current.month)
                val incomeDetails = analysisRepository.getIncomeDetails(current.year, current.month)
                val closedBalance = monthlyBalanceRepository.getClosedBalance(current.year, current.month)
                val allClosed = monthlyBalanceRepository.getAllClosedBalances()
                val totalSavings = monthlyBalanceRepository.getTotalSavings()

                _state.value = current.copy(
                    isLoading = false,
                    summary = summary,
                    categories = categories,
                    topProducts = topProducts,
                    generalSummary = generalSummary,
                    expenseCategories = expenseCategories,
                    topMovements = topMovements,
                    balanceIncomeCents = monthTotals.totalIncomeCents,
                    balanceExpenseCents = monthTotals.totalExpenseCents,
                    balanceCents = monthTotals.totalIncomeCents - monthTotals.totalExpenseCents,
                    balanceClosed = closedBalance != null,
                    closedBalances = allClosed,
                    totalSavings = totalSavings,
                    incomeDetails = incomeDetails
                )
            } catch (e: Exception) {
                _state.value = current.copy(isLoading = false)
            }
        }
    }
}
