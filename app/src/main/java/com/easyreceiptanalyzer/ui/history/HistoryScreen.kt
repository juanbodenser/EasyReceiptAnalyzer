package com.easyreceiptanalyzer.ui.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.easyreceiptanalyzer.data.local.ReceiptEntity
import com.easyreceiptanalyzer.ui.theme.Accent
import com.easyreceiptanalyzer.ui.theme.AccentCyan
import com.easyreceiptanalyzer.ui.theme.Danger
import com.easyreceiptanalyzer.ui.theme.Ink
import com.easyreceiptanalyzer.ui.theme.Mist
import com.easyreceiptanalyzer.ui.theme.MistMuted
import com.easyreceiptanalyzer.ui.theme.Warning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    onReceiptClick: (Long) -> Unit,
    onImportClick: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val receipts by viewModel.receipts.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var menuOpenForReceiptId by remember { mutableStateOf<Long?>(null) }
    var pendingDeleteReceiptId by remember { mutableStateOf<Long?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // ─── Cabecera ───
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(3.dp)
                .background(AccentCyan)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "02 / HISTORIAL",
            style = MaterialTheme.typography.labelLarge,
            color = AccentCyan,
            letterSpacing = 4.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "REGISTROS",
            style = MaterialTheme.typography.displaySmall,
            color = Mist,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(24.dp))

        // ─── Selector de orden ───
        Box {
            Row(
                modifier = Modifier
                    .clickable { sortMenuExpanded = true }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ORDENAR POR:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MistMuted,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = sortOption.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentCyan,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "▾",
                    color = AccentCyan,
                    fontWeight = FontWeight.Bold
                )
            }
            DropdownMenu(
                expanded = sortMenuExpanded,
                onDismissRequest = { sortMenuExpanded = false }
            ) {
                SortOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            viewModel.setSortOption(option)
                            sortMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ─── Cabecera de la lista ───
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "ESTABLECIMIENTO",
                style = MaterialTheme.typography.labelSmall,
                color = MistMuted,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "TOTAL",
                style = MaterialTheme.typography.labelSmall,
                color = MistMuted,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MistMuted.copy(alpha = 0.3f))
        )

        // ─── Lista o mensaje vacío ───
        if (receipts.isEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Todavía no has guardado ningún ticket.",
                style = MaterialTheme.typography.bodyLarge,
                color = MistMuted
            )
            Spacer(modifier = Modifier.weight(1f))
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(receipts, key = { it.id }) { receipt ->
                    ReceiptRow(
                        receipt = receipt,
                        onClick = { onReceiptClick(receipt.id) },
                        onLongClick = { menuOpenForReceiptId = receipt.id },
                        isMenuOpen = menuOpenForReceiptId == receipt.id,
                        onDismissMenu = { menuOpenForReceiptId = null },
                        onDeleteClick = {
                            menuOpenForReceiptId = null
                            pendingDeleteReceiptId = receipt.id
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }

        TextButton(
            onClick = onImportClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "IMPORTAR EXTRACTO BANCARIO ›",
                color = Warning,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )
        }

        TextButton(
            onClick = onBackClick,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = "‹  VOLVER",
                color = MistMuted,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    pendingDeleteReceiptId?.let { deleteId ->
        AlertDialog(
            onDismissRequest = { pendingDeleteReceiptId = null },
            title = { Text("Eliminar ticket", fontWeight = FontWeight.Bold) },
            text = { Text("¿Seguro que quieres eliminar este ticket? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteReceipt(deleteId)
                    pendingDeleteReceiptId = null
                }) {
                    Text("Eliminar", color = Danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteReceiptId = null }) {
                    Text("Cancelar", color = MistMuted)
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReceiptRow(
    receipt: ReceiptEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isMenuOpen: Boolean,
    onDismissMenu: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .padding(vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = receipt.storeName.uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        color = Mist,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatDate(receipt.purchaseDate),
                        style = MaterialTheme.typography.labelSmall,
                        color = MistMuted,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = formatCents(receipt.totalCents),
                    style = MaterialTheme.typography.titleMedium,
                    color = Accent,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "›",
                    style = MaterialTheme.typography.titleLarge,
                    color = AccentCyan,
                    fontWeight = FontWeight.Light
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MistMuted.copy(alpha = 0.15f))
            )
        }
        DropdownMenu(
            expanded = isMenuOpen,
            onDismissRequest = onDismissMenu
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        "ELIMINAR",
                        color = Danger,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                },
                onClick = onDeleteClick
            )
        }
    }
}

private fun formatDate(millis: Long?): String {
    if (millis == null) return "FECHA DESCONOCIDA"
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}

private fun formatCents(cents: Long?): String {
    if (cents == null) return "—"
    return String.format(Locale.getDefault(), "€%.2f", cents / 100.0)
}
