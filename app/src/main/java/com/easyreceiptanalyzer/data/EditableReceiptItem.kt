package com.easyreceiptanalyzer.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class EditableReceiptItem(
    val name: String,
    price: Double,
    category: ProductCategory
) {
    var price by mutableStateOf(price)
    var category by mutableStateOf(category)
}
