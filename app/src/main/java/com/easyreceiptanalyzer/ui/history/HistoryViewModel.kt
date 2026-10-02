package com.easyreceiptanalyzer.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easyreceiptanalyzer.data.ReceiptRepository
import com.easyreceiptanalyzer.data.local.ReceiptEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOption(val label: String) {
    DATE_DESC("FECHA ↓"),
    DATE_ASC("FECHA ↑"),
    PRICE_DESC("IMPORTE ↓"),
    PRICE_ASC("IMPORTE ↑"),
    STORE_AZ("TIENDA A-Z")
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val receiptRepository: ReceiptRepository
) : ViewModel() {

    private val _sortOption = MutableStateFlow(SortOption.DATE_DESC)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    val receipts: StateFlow<List<ReceiptEntity>> =
        combine(
            receiptRepository.observeReceipts(),
            _sortOption
        ) { list, sort ->
            when (sort) {
                SortOption.DATE_DESC -> list.sortedByDescending { it.purchaseDate ?: 0L }
                SortOption.DATE_ASC -> list.sortedBy { it.purchaseDate ?: 0L }
                SortOption.PRICE_DESC -> list.sortedByDescending { it.totalCents ?: 0L }
                SortOption.PRICE_ASC -> list.sortedBy { it.totalCents ?: 0L }
                SortOption.STORE_AZ -> list.sortedBy { it.storeName.lowercase() }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun deleteReceipt(receiptId: Long) {
        viewModelScope.launch {
            receiptRepository.deleteReceipt(receiptId)
        }
    }
}
