package com.easyreceiptanalyzer.ui.manual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easyreceiptanalyzer.data.ProductCategory
import com.easyreceiptanalyzer.data.ReceiptRepository
import com.easyreceiptanalyzer.data.local.ItemEntity
import com.easyreceiptanalyzer.data.local.ReceiptEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ManualReceiptViewModel @Inject constructor(
    private val receiptRepository: ReceiptRepository
) : ViewModel() {

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun saveManualReceipt(
        storeName: String,
        totalEuros: Double,
        category: ProductCategory,
        notes: String
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null
            try {
                val totalCents = Math.round(totalEuros * 100)

                val entity = ReceiptEntity(
                    storeName = storeName.ifBlank { "Sin tienda" },
                    purchaseDate = Calendar.getInstance().timeInMillis,
                    totalCents = totalCents,
                    rawText = notes.ifBlank { null }
                )

                // Creamos un único ItemEntity genérico para que aparezca
                // en el análisis por categorías
                val genericItem = ItemEntity(
                    receiptId = 0,
                    name = "Gasto manual",
                    priceCents = totalCents,
                    category = category.name
                )

                receiptRepository.saveReceipt(entity, listOf(genericItem))
                _saved.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Error al guardar el ticket manual."
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun reset() {
        _saved.value = false
        _errorMessage.value = null
    }
}
