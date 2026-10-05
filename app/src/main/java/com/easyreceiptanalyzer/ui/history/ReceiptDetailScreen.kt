package com.easyreceiptanalyzer.ui.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.easyreceiptanalyzer.data.ExpenseCategory
import com.easyreceiptanalyzer.data.ProductCategory
import com.easyreceiptanalyzer.data.local.ItemEntity
import com.easyreceiptanalyzer.data.local.ReceiptEntity
import com.easyreceiptanalyzer.ui.theme.Accent
import com.easyreceiptanalyzer.ui.theme.AccentCyan
import com.easyreceiptanalyzer.ui.theme.Danger
import com.easyreceiptanalyzer.ui.theme.Ink
import com.easyreceiptanalyzer.ui.theme.InkSurface
import com.easyreceiptanalyzer.ui.theme.InkSurfaceHigh
import com.easyreceiptanalyzer.ui.theme.Mist
import com.easyreceiptanalyzer.ui.theme.MistMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReceiptDetailScreen(
    receiptId: Long,
    onBackClick: () -> Unit,
    viewModel: ReceiptDetailViewModel = hiltViewModel()
) {
    val receipt by viewModel.receipt.collectAsState()
    val items by viewModel.items.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isDeleting by viewModel.isDeleting.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(receiptId) {
        viewModel.load(receiptId)
    }

    // Escucha el evento de borrado (solo se dispara una vez)
    LaunchedEffect(Unit) {
        viewModel.deletedEvents.collect {
            onBackClick()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        val r = receipt
        when {
            isLoading -> Text("Cargando...")
            r == null -> Text("No se encontró el ticket.")
            else -> {
                if (r.source == "bank") {
                    BankMovementDetailContent(
                        receipt = r,
                        onCategoryChange = { newCategory ->
                            viewModel.updateCategory(r.id, newCategory.name)
                        },
                        onNotesChange = { newNotes ->
                            viewModel.updateNotes(r.id, newNotes)
                        }
                    )
                } else {
                    Text(
                        text = r.storeName.uppercase(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Mist,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = formatDate(r.purchaseDate),
                        style = MaterialTheme.typography.labelMedium,
                        color = MistMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items, key = { it.id }) { item ->
                            ItemRow(
                                item = item,
                                onCategoryChange = { newCategory ->
                                    viewModel.updateItemCategory(
                                        itemId = item.id,
                                        newCategory = newCategory,
                                        productName = item.name
                                    )
                                }
                            )
                        }
                    }

                    // ─── Notas (solo si el ticket tiene rawText) ───
                    val notes = r.rawText
                    if (!notes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(InkSurface)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "NOTAS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MistMuted,
                                letterSpacing = 3.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = notes,
                                color = Mist,
                                style = MaterialTheme.typography.bodyMedium,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TOTAL", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = String.format(Locale.getDefault(), "%.2f€", (r.totalCents ?: 0) / 100.0),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !isDeleting,
                    shape = RoundedCornerShape(0.dp),
                    border = BorderStroke(1.dp, Danger),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Danger
                    )
                ) {
                    Text(
                        text = if (isDeleting) "ELIMINANDO..." else "ELIMINAR TICKET",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onBackClick) {
            Text("Volver")
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar ticket") },
            text = { Text("¿Seguro que quieres eliminar este ticket? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteReceipt(receiptId)
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ColumnScope.BankMovementDetailContent(
    receipt: ReceiptEntity,
    onCategoryChange: (ExpenseCategory) -> Unit,
    onNotesChange: (String) -> Unit
) {
    // Categoría actual (del rawText)
    val currentCategory = remember(receipt.rawText) {
        runCatching {
            ExpenseCategory.valueOf(receipt.rawText ?: "OTROS")
        }.getOrDefault(ExpenseCategory.OTROS)
    }

    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var notesInput by remember(receipt.id) {
        mutableStateOf(receipt.notes ?: "")
    }
    var notesSaved by remember { mutableStateOf(false) }

    // ─── Cabecera ───
    Text(
        text = receipt.storeName.uppercase(),
        style = MaterialTheme.typography.headlineSmall,
        color = Mist,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.5.sp
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = formatDate(receipt.purchaseDate),
        style = MaterialTheme.typography.labelMedium,
        color = MistMuted,
        letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(24.dp))

    // ─── Categoría de gasto ───
    Text(
        text = "CATEGORÍA",
        style = MaterialTheme.typography.labelSmall,
        color = MistMuted,
        letterSpacing = 3.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(6.dp))
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(InkSurfaceHigh)
                .clickable { categoryMenuExpanded = true }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentCategory.displayName.uppercase(),
                color = AccentCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "▾",
                color = AccentCyan,
                fontWeight = FontWeight.Bold
            )
        }
        DropdownMenu(
            expanded = categoryMenuExpanded,
            onDismissRequest = { categoryMenuExpanded = false }
        ) {
            ExpenseCategory.entries.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.displayName) },
                    onClick = {
                        onCategoryChange(category)
                        categoryMenuExpanded = false
                    }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // ─── Notas ───
    Text(
        text = "NOTAS",
        style = MaterialTheme.typography.labelSmall,
        color = MistMuted,
        letterSpacing = 3.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(6.dp))
    OutlinedTextField(
        value = notesInput,
        onValueChange = {
            notesInput = it
            notesSaved = false
        },
        modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
        placeholder = {
            Text(
                "Ej: Diesel para el coche, aceite de motor...",
                color = MistMuted
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Mist,
            unfocusedTextColor = Mist,
            focusedBorderColor = Accent,
            unfocusedBorderColor = MistMuted.copy(alpha = 0.4f),
            cursorColor = Accent,
            focusedPlaceholderColor = MistMuted,
            unfocusedPlaceholderColor = MistMuted
        )
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Botón de guardar notas (solo si hay cambios)
    if (notesInput != (receipt.notes ?: "")) {
        Button(
            onClick = {
                onNotesChange(notesInput)
                notesSaved = true
            },
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = RoundedCornerShape(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentCyan,
                contentColor = Ink
            )
        ) {
            Text(
                text = "GUARDAR NOTAS",
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                style = MaterialTheme.typography.labelMedium
            )
        }
    } else if (notesSaved) {
        Text(
            text = "✓ NOTAS GUARDADAS",
            color = AccentCyan,
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )
    }

    Spacer(modifier = Modifier.weight(1f))

    // ─── Total ───
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(InkSurface)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (receipt.isExpense) "TOTAL" else "INGRESO",
            color = MistMuted,
            style = MaterialTheme.typography.labelLarge,
            letterSpacing = 3.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (receipt.isExpense) {
                String.format(Locale.getDefault(), "- %.2f€", (receipt.totalCents ?: 0) / 100.0)
            } else {
                String.format(Locale.getDefault(), "+ %.2f€", (receipt.totalCents ?: 0) / 100.0)
            },
            color = if (receipt.isExpense) Danger else Accent,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun ItemRow(
    item: ItemEntity,
    onCategoryChange: (ProductCategory) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .background(InkSurface)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(Accent.copy(alpha = 0.6f))
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = item.name,
                color = Mist,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Box {
                Text(
                    text = safeCategoryName(item.category).uppercase(),
                    color = AccentCyan,
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { menuExpanded = true }
                        .padding(vertical = 2.dp)
                )
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    ProductCategory.entries.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.displayName) },
                            onClick = {
                                onCategoryChange(category)
                                menuExpanded = false
                            }
                        )
                    }
                }
            }
        }
        Text(
            text = String.format(Locale.getDefault(), "€%.2f", item.priceCents / 100.0),
            color = Mist,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 16.dp, top = 16.dp)
        )
    }
}

private fun formatDate(millis: Long?): String {
    if (millis == null) return "Fecha desconocida"
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}

private fun safeCategoryName(name: String): String =
    runCatching { ProductCategory.valueOf(name).displayName }.getOrDefault(name)
