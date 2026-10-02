package com.easyreceiptanalyzer.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easyreceiptanalyzer.data.AnalysisRepository
import com.easyreceiptanalyzer.data.local.MonthlySummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val analysisRepository: AnalysisRepository
) : ViewModel() {

    private val _summary = MutableStateFlow<MonthlySummary?>(null)
    val summary: StateFlow<MonthlySummary?> = _summary.asStateFlow()

    private val _lastPurchaseDate = MutableStateFlow<Long?>(null)
    val lastPurchaseDate: StateFlow<Long?> = _lastPurchaseDate.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH)
            _summary.value = analysisRepository.getMonthlySummary(year, month)
            _lastPurchaseDate.value = analysisRepository.getLastPurchaseDate()
        }
    }
}
