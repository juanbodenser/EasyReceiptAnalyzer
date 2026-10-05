package com.easyreceiptanalyzer.ui.bankimport

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easyreceiptanalyzer.data.ExpenseCategory
import com.easyreceiptanalyzer.data.ReceiptRepository
import com.easyreceiptanalyzer.data.bank.BankMovement
import com.easyreceiptanalyzer.data.bank.BankStatementParser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Representa un movimiento bancario en la UI de importación.
 */
data class BankImportItem(
    val movement: BankMovement,
    val isDuplicate: Boolean = false,
    val isSelected: Boolean = true,
    val category: ExpenseCategory = ExpenseCategory.OTROS
)

@HiltViewModel
class BankImportViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val receiptRepository: ReceiptRepository
) : ViewModel() {

    private val _items = MutableStateFlow<List<BankImportItem>>(emptyList())
    val items: StateFlow<List<BankImportItem>> = _items.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _importedEvents = Channel<Int>(Channel.BUFFERED)
    val importedEvents: Flow<Int> = _importedEvents.receiveAsFlow()

    /**
     * Carga y parsea el archivo TXT del banco. Detecta duplicados
     * comparando cada movimiento con los tickets escaneados existentes.
     */
    fun loadFile(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val inputStream = appContext.contentResolver.openInputStream(uri)
                    ?: throw IllegalStateException("No se pudo abrir el archivo")

                val movements = inputStream.use { stream ->
                    BankStatementParser.parse(stream)
                }

                // Para cada movimiento, comprobamos si ya hay un ticket escaneado
                // con fecha ±2 días y mismo importe.
                val items = movements.map { movement ->
                    val duplicate = receiptRepository.findScanDuplicate(
                        date = movement.date,
                        totalCents = movement.amountCents
                    )
                    BankImportItem(
                        movement = movement,
                        isDuplicate = duplicate != null,
                        isSelected = duplicate == null && !movement.isSuspicious,
                        category = suggestCategory(movement.concept)
                    )
                }

                _items.value = items
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Error al leer el archivo"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Cambia la categoría de un movimiento.
     */
    fun setCategory(index: Int, category: ExpenseCategory) {
        _items.value = _items.value.mapIndexed { i, item ->
            if (i == index) item.copy(category = category) else item
        }
    }

    /**
     * Marca o desmarca un movimiento para la importación.
     */
    fun toggleSelection(index: Int) {
        _items.value = _items.value.mapIndexed { i, item ->
            if (i == index) item.copy(isSelected = !item.isSelected) else item
        }
    }

    fun selectAll() {
        _items.value = _items.value.map { it.copy(isSelected = true) }
    }

    fun deselectAll() {
        _items.value = _items.value.map { it.copy(isSelected = false) }
    }

    fun selectOnlyNew() {
        _items.value = _items.value.map {
            it.copy(isSelected = !it.isDuplicate && !it.movement.isSuspicious)
        }
    }

    /**
     * Importa los movimientos seleccionados. Los que ya estaban guardados
     * se ignoran (no se duplican).
     */
    fun importSelected() {
        viewModelScope.launch {
            _isImporting.value = true
            _errorMessage.value = null
            try {
                val toImport = _items.value.filter { it.isSelected }
                var count = 0
                for (item in toImport) {
                    receiptRepository.saveBankMovement(
                        concept = item.movement.concept,
                        date = item.movement.date,
                        amountCents = item.movement.amountCents,
                        category = item.category.name,
                        isExpense = item.movement.isExpense
                    )
                    count++
                }
                _importedEvents.send(count)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Error al importar"
            } finally {
                _isImporting.value = false
            }
        }
    }

    /**
     * Reset del estado (para volver a empezar).
     */
    fun reset() {
        _items.value = emptyList()
        _errorMessage.value = null
    }

    /**
     * Sugiere una categoría según el concepto del movimiento.
     * Heurística simple basada en palabras clave.
     */
    private fun suggestCategory(concept: String): ExpenseCategory {
        val upper = concept.uppercase()
        return when {
            upper.contains("MERCADONA") ||
            upper.contains("CARREFOUR") ||
            upper.contains("DIA ") ||
            upper.contains("AHORRAMAS") ||
            upper.contains("ALIMENTACION") ||
            upper.contains("SUPER") ||
            upper.contains("MARKET") -> ExpenseCategory.SUPERMERCADO

            // ─── COCHE ───
            upper.contains("PLENERGY") ||
            upper.contains("REPSOL") ||
            upper.contains("CEPSA") ||
            upper.contains("GALP") ||
            upper.contains("SHELL") ||
            upper.contains("BP ") ||
            upper.contains("ITV") ||
            upper.contains("TALLER") ||
            upper.contains("PARKING") ||
            upper.contains("APARCAMIENTO") ||
            upper.contains("MOVILIDAD MMD") ||
            upper.contains("LENDROCK") ||
            upper.contains("PRESTAMO") ||
            upper.contains("CUOTA COCHE") ||
            upper.contains("FINANCIACION") -> ExpenseCategory.COCHE

            // ─── TRANSPORTE ───
            upper.contains("METRO") ||
            upper.contains("UBER") ||
            upper.contains("CABIFY") ||
            upper.contains("TAXI") ||
            upper.contains("BICIMAD") ||
            upper.contains("RENFE") ||
            upper.contains("CERCANIAS") ||
            upper.contains("MOVILIDAD") -> ExpenseCategory.TRANSPORTE

            upper.contains("MILA PUB") ||
            upper.contains("RINCON") ||
            upper.contains("BUBBLETEA") ||
            upper.contains("MISKI") ||
            upper.contains("RESTAURANT") ||
            upper.contains("BAR ") -> ExpenseCategory.RESTAURANTES

            upper.contains("NETFLIX") ||
            upper.contains("SPOTIFY") ||
            upper.contains("AMAZON") -> ExpenseCategory.SERVICIOS

            upper.contains("O2") ||
            upper.contains("VODAFONE") ||
            upper.contains("MOVISTAR") ||
            upper.contains("ORANGE") ||
            upper.contains("TELEFONICA") -> ExpenseCategory.TELEFONIA

            upper.contains("WIZINK") ||
            upper.contains("SEGURO") ||
            upper.contains("MAPFRE") ||
            upper.contains("AXA") -> ExpenseCategory.SEGUROS

            upper.contains("LIBRERIA") ||
            upper.contains("LIBRO") -> ExpenseCategory.OCIO

            upper.contains("OPTICA") ||
            upper.contains("FARMACIA") ||
            upper.contains("CLINICA") -> ExpenseCategory.SALUD

            else -> ExpenseCategory.OTROS
        }
    }
}
