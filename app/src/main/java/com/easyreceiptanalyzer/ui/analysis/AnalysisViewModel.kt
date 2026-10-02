package com.easyreceiptanalyzer.ui.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easyreceiptanalyzer.data.AnalysisRepository
import com.easyreceiptanalyzer.data.local.CategoryTotal
import com.easyreceiptanalyzer.data.local.MonthlySummary
import com.easyreceiptanalyzer.data.local.ProductTotal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class AnalysisUiState(
    val year: Int,
    val month: Int,
    val isLoading: Boolean = false,
    val summary: MonthlySummary? = null,
    val categories: List<CategoryTotal> = emptyList(),
    val topProducts: List<ProductTotal> = emptyList()
)

data class CategoryDetail(
    val categoryName: String,           // "SNACKS", "LACTEOS", etc.
    val products: List<ProductTotal>
)

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val analysisRepository: AnalysisRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AnalysisUiState(
        year = Calendar.getInstance().get(Calendar.YEAR),
        month = Calendar.getInstance().get(Calendar.MONTH)
    ))
    val state: StateFlow<AnalysisUiState> = _state.asStateFlow()

    private val _categoryDetail = MutableStateFlow<CategoryDetail?>(null)
    val categoryDetail: StateFlow<CategoryDetail?> = _categoryDetail.asStateFlow()

    fun refresh() {
        load()
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

    private fun load() {
        viewModelScope.launch {
            val current = _state.value
            _state.value = current.copy(isLoading = true)
            try {
                val summary = analysisRepository.getMonthlySummary(current.year, current.month)
                val categories = analysisRepository.getCategoryTotals(current.year, current.month)
                val topProducts = analysisRepository.getTopProducts(current.year, current.month)
                _state.value = current.copy(
                    isLoading = false,
                    summary = summary,
                    categories = categories,
                    topProducts = topProducts
                )
            } catch (e: Exception) {
                _state.value = current.copy(isLoading = false)
            }
        }
    }
}
