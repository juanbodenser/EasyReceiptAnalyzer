package com.easyreceiptanalyzer.ui.documents

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class DocumentItem(
    val uri: Uri,
    val name: String,
    val dateMillis: Long
)

@HiltViewModel
class DocumentsViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _documents = MutableStateFlow<List<DocumentItem>>(emptyList())
    val documents: StateFlow<List<DocumentItem>> = _documents.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            val list = withContext(Dispatchers.IO) { loadDocuments() }
            _documents.value = list
            _isLoading.value = false
        }
    }

    private fun loadDocuments(): List<DocumentItem> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            loadFromMediaStore()
        } else {
            loadFromFileSystem()
        }
    }

    private fun loadFromMediaStore(): List<DocumentItem> {
        val result = mutableListOf<DocumentItem>()
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_ADDED
        )
        val selection = "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
        val selectionArgs = arrayOf("%EasyReceiptAnalyzer/Documents%")
        val sortOrder = "${MediaStore.MediaColumns.DATE_ADDED} DESC"

        appContext.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val name = cursor.getString(nameCol) ?: "Documento"
                val dateSeconds = cursor.getLong(dateCol)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Files.getContentUri("external"),
                    id
                )
                result.add(DocumentItem(uri, name, dateSeconds * 1000L))
            }
        }
        return result
    }

    private fun loadFromFileSystem(): List<DocumentItem> {
        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val appDir = File(documentsDir, "EasyReceiptAnalyzer/Documents")
        if (!appDir.exists()) return emptyList()

        return appDir.listFiles { file -> file.extension.lowercase() == "pdf" }
            ?.sortedByDescending { it.lastModified() }
            ?.map { file ->
                DocumentItem(
                    uri = Uri.fromFile(file),
                    name = file.name,
                    dateMillis = file.lastModified()
                )
            }
            ?: emptyList()
    }

    fun deleteDocument(document: DocumentItem) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        appContext.contentResolver.delete(document.uri, null, null)
                    } else {
                        val file = File(document.uri.path ?: return@withContext)
                        if (file.exists()) file.delete()
                    }
                } catch (e: Exception) {
                    Log.e("DocumentsVM", "Error al eliminar", e)
                }
            }
            refresh()
        }
    }
}
