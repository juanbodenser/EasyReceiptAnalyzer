package com.easyreceiptanalyzer.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easyreceiptanalyzer.data.ProductCategory
import com.easyreceiptanalyzer.data.ProductCategoryDao
import com.easyreceiptanalyzer.data.ProductCategoryOverride
import com.easyreceiptanalyzer.data.ReceiptRepository
import com.easyreceiptanalyzer.data.local.ItemEntity
import com.easyreceiptanalyzer.data.local.ReceiptEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReceiptDetailViewModel @Inject constructor(
    private val receiptRepository: ReceiptRepository,
    private val productCategoryDao: ProductCategoryDao
) : ViewModel() {

    private val _receipt = MutableStateFlow<ReceiptEntity?>(null)
    val receipt: StateFlow<ReceiptEntity?> = _receipt.asStateFlow()

    private val _items = MutableStateFlow<List<ItemEntity>>(emptyList())
    val items: StateFlow<List<ItemEntity>> = _items.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    // Evento de un solo uso: no retiene el valor tras consumirse
    private val _deletedEvents = Channel<Unit>(Channel.BUFFERED)
    val deletedEvents: Flow<Unit> = _deletedEvents.receiveAsFlow()

    fun load(receiptId: Long) {
        _receipt.value = null
        _items.value = emptyList()
        _isLoading.value = true

        viewModelScope.launch {
            _receipt.value = receiptRepository.getReceipt(receiptId)
            _items.value = receiptRepository.getItems(receiptId)
            _isLoading.value = false
        }
    }

    fun updateItemCategory(itemId: Long, newCategory: ProductCategory, productName: String) {
        viewModelScope.launch {
            receiptRepository.updateItemCategory(itemId, newCategory.name)
            // Guardar el override para futuros tickets
            productCategoryDao.saveOverride(
                ProductCategoryOverride(
                    normalizedName = productName.uppercase()
                        .replace("Á", "A").replace("É", "E").replace("Í", "I")
                        .replace("Ó", "O").replace("Ú", "U").replace("Ñ", "N")
                        .trim(),
                    category = newCategory.name
                )
            )
            // Refrescar los items del ticket
            _items.value = receiptRepository.getItems(_receipt.value?.id ?: return@launch)
        }
    }

    fun deleteReceipt(receiptId: Long) {
        viewModelScope.launch {
            _isDeleting.value = true
            try {
                receiptRepository.deleteReceipt(receiptId)
                _deletedEvents.send(Unit)
            } finally {
                _isDeleting.value = false
            }
        }
    }
}
