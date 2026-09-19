package com.easyreceiptanalyzer.ui.addreceipt

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easyreceiptanalyzer.data.ReceiptRepository
import com.easyreceiptanalyzer.data.local.ReceiptEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddReceiptViewModel @Inject constructor(
    private val receiptRepository: ReceiptRepository
) : ViewModel() {

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    fun selectImage(uri: Uri?) {
        _selectedImageUri.value = uri
    }

    fun saveReceipt(storeName: String) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val receipt = ReceiptEntity(
                    storeName = storeName,
                    purchaseDate = null,
                    totalCents = null,
                    rawText = null
                )
                receiptRepository.saveReceipt(receipt)
                _selectedImageUri.value = null
            } finally {
                _isSaving.value = false
            }
        }
    }
}