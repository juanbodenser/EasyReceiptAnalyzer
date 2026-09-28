package com.easyreceiptanalyzer.ui.addreceipt

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easyreceiptanalyzer.data.EditableReceiptItem
import com.easyreceiptanalyzer.data.GeminiReceiptAnalyzer
import com.easyreceiptanalyzer.data.ProductCategory
import com.easyreceiptanalyzer.data.ProductCategoryDao
import com.easyreceiptanalyzer.data.ProductCategorizer
import com.easyreceiptanalyzer.data.ReceiptRepository
import com.easyreceiptanalyzer.data.local.ReceiptEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

data class ReceiptMeta(val storeName: String, val date: String, val total: Double)

@HiltViewModel
class AddReceiptViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val receiptRepository: ReceiptRepository,
    private val productCategoryDao: ProductCategoryDao
) : ViewModel() {

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val _isProcessingOcr = MutableStateFlow(false)
    val isProcessingOcr: StateFlow<Boolean> = _isProcessingOcr.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _receiptMeta = MutableStateFlow<ReceiptMeta?>(null)
    val receiptMeta: StateFlow<ReceiptMeta?> = _receiptMeta.asStateFlow()

    private val _items = MutableStateFlow<List<EditableReceiptItem>>(emptyList())
    val items: StateFlow<List<EditableReceiptItem>> = _items.asStateFlow()

    private var lastRawText: String? = null

    fun selectImage(uri: Uri?) {
        _selectedImageUri.value = uri
        _receiptMeta.value = null
        _items.value = emptyList()
        _errorMessage.value = null
    }

    fun analyzeReceipt() {
        val uri = _selectedImageUri.value ?: return
        viewModelScope.launch {
            _isProcessingOcr.value = true
            _errorMessage.value = null
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(appContext.contentResolver, uri)
                val parsed = GeminiReceiptAnalyzer.analyze(bitmap)
                lastRawText = null // ya no generamos texto OCR crudo con este método

                _receiptMeta.value = ReceiptMeta(parsed.storeName, parsed.date, parsed.total)

                val editable = parsed.items.map { item ->
                    val category = ProductCategorizer.categorize(item.name, productCategoryDao)
                    EditableReceiptItem(item.name, item.price, category)
                }
                _items.value = editable
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Error al analizar el ticket con IA."
            } finally {
                _isProcessingOcr.value = false
            }
        }
    }

    fun onPriceChanged(item: EditableReceiptItem, newPrice: Double) {
        item.price = newPrice
    }

    fun onCategoryChanged(item: EditableReceiptItem, newCategory: ProductCategory) {
        item.category = newCategory
        viewModelScope.launch {
            ProductCategorizer.saveCorrection(item.name, newCategory, productCategoryDao)
        }
    }

    fun saveReceipt() {
        val meta = _receiptMeta.value ?: return
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dateMillis = runCatching { dateFormat.parse(meta.date)?.time }.getOrNull()
                val correctedTotal = _items.value.sumOf { it.price }
                val totalCents = Math.round(correctedTotal * 100)

                val entity = ReceiptEntity(
                    storeName = meta.storeName,
                    purchaseDate = dateMillis,
                    totalCents = totalCents,
                    rawText = lastRawText
                )
                receiptRepository.saveReceipt(entity)

                _selectedImageUri.value = null
                _receiptMeta.value = null
                _items.value = emptyList()
            } finally {
                _isSaving.value = false
            }
        }
    }
}
