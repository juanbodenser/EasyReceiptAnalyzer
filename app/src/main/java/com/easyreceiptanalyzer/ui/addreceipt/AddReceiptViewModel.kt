package com.easyreceiptanalyzer.ui.addreceipt

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easyreceiptanalyzer.data.DeepSeekReceiptAnalyzer
import com.easyreceiptanalyzer.data.EditableReceiptItem
import com.easyreceiptanalyzer.data.ProductCategory
import com.easyreceiptanalyzer.data.ProductCategoryDao
import com.easyreceiptanalyzer.data.ProductCategorizer
import com.easyreceiptanalyzer.data.ReceiptRepository
import com.easyreceiptanalyzer.data.local.ItemEntity
import com.easyreceiptanalyzer.data.local.ReceiptEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class ReceiptMeta(val storeName: String, val date: String, val total: Double)

data class DuplicateWarning(val existingReceipt: ReceiptEntity)

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

    private val _duplicateWarning = MutableStateFlow<DuplicateWarning?>(null)
    val duplicateWarning: StateFlow<DuplicateWarning?> = _duplicateWarning.asStateFlow()

    private var lastRawText: String? = null

    fun selectImage(uri: Uri?) {
        _selectedImageUri.value = uri
        _receiptMeta.value = null
        _items.value = emptyList()
        _errorMessage.value = null
        _duplicateWarning.value = null
    }

    fun analyzeReceipt() {
        val uri = _selectedImageUri.value ?: return
        viewModelScope.launch {
            _isProcessingOcr.value = true
            _errorMessage.value = null
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(appContext.contentResolver, uri)
                val parsed = DeepSeekReceiptAnalyzer.analyze(bitmap)
                lastRawText = null

                // Si la IA no detecta fecha, usamos la de hoy
                val displayDate = if (parsed.date.isBlank()) {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                } else {
                    parsed.date
                }

                _receiptMeta.value = ReceiptMeta(parsed.storeName, displayDate, parsed.total)

                val editable = parsed.items.map { item ->
                    val category = ProductCategorizer.categorize(item.name, productCategoryDao)
                    EditableReceiptItem(item.name, item.price, category)
                }
                _items.value = editable
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Error al analizar el ticket con DeepSeek."
            } finally {
                _isProcessingOcr.value = false
            }
        }
    }

    fun onDateChanged(newDate: String) {
        val current = _receiptMeta.value ?: return
        _receiptMeta.value = current.copy(date = newDate)
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

    fun saveReceipt(checkDuplicates: Boolean = true) {
        val meta = _receiptMeta.value ?: return
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dateMillis = runCatching { dateFormat.parse(meta.date)?.time }
                    .getOrNull()
                    ?: System.currentTimeMillis()
                val correctedTotal = _items.value.sumOf { it.price }
                val totalCents = Math.round(correctedTotal * 100)

                if (checkDuplicates) {
                    val existing = receiptRepository.findSimilarReceipt(
                        purchaseDate = dateMillis,
                        totalCents = totalCents
                    )
                    if (existing != null) {
                        _duplicateWarning.value = DuplicateWarning(existing)
                        _isSaving.value = false
                        return@launch
                    }
                }

                val entity = ReceiptEntity(
                    storeName = meta.storeName,
                    purchaseDate = dateMillis,
                    totalCents = totalCents,
                    rawText = lastRawText
                )

                val itemEntities = _items.value.map { editableItem ->
                    ItemEntity(
                        receiptId = 0,
                        name = editableItem.name.uppercase().trim(),
                        priceCents = Math.round(editableItem.price * 100),
                        category = editableItem.category.name
                    )
                }

                receiptRepository.saveReceipt(entity, itemEntities)

                _selectedImageUri.value = null
                _receiptMeta.value = null
                _items.value = emptyList()
                _duplicateWarning.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Error al guardar el ticket."
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun confirmSaveDespiteDuplicate() {
        _duplicateWarning.value = null
        saveReceipt(checkDuplicates = false)
    }

    fun dismissDuplicateWarning() {
        _duplicateWarning.value = null
    }
}