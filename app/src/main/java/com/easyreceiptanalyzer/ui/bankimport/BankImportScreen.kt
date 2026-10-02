package com.easyreceiptanalyzer.ui.bankimport

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
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
import com.easyreceiptanalyzer.ui.theme.Accent
import com.easyreceiptanalyzer.ui.theme.AccentCyan
import com.easyreceiptanalyzer.ui.theme.Danger
import com.easyreceiptanalyzer.ui.theme.Ink
import com.easyreceiptanalyzer.ui.theme.InkSurface
import com.easyreceiptanalyzer.ui.theme.InkSurfaceHigh
import com.easyreceiptanalyzer.ui.theme.Mist
import com.easyreceiptanalyzer.ui.theme.MistMuted
import com.easyreceiptanalyzer.ui.theme.Warning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BankImportScreen(
    onBackClick: () -> Unit,
    onImported: () -> Unit,
    viewModel: BankImportViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val importedCount by viewModel.importedCount.collectAsState()

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) viewModel.loadFile(uri)
    }

    // Cuando la importación termina, avisamos al usuario y volvemos
    LaunchedEffect(importedCount) {
        if (importedCount != null && importedCount!! > 0) {
            onImported()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .width(56.dp)
                .height(3.dp)
                .background(Warning)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "05 / IMPORTAR EXTRACTO",
            style = MaterialTheme.typography.labelLarge,
            color = Warning,
            letterSpacing = 4.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "BANCO",
            style = MaterialTheme.typography.displaySmall,
            color = Mist,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Selecciona el archivo TXT del extracto bancario.",
            style = MaterialTheme.typography.bodySmall,
            color = MistMuted,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ─── Botón de seleccionar archivo ───
        Button(
            onClick = { filePicker.launch(arrayOf("text/plain", "application/octet-stream", "*/*")) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(0.dp),
            enabled = !isLoading && !isImporting,
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentCyan,
                contentColor = Ink
            )
        ) {
            Text(
                text = if (items.isEmpty()) "SELECCIONAR ARCHIVO" else "CAMBIAR ARCHIVO",
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ─── Estado de carga ───
        if (isLoading) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    color = Warning,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "PROCESANDO EXTRACTO...",
                    color = MistMuted,
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 2.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ─── Mensaje de error ───
        errorMessage?.let {
            Text(
                text = "Error: $it",
                color = Danger,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ─── Lista de movimientos ───
        if (items.isEmpty() && !isLoading) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Selecciona un archivo para empezar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MistMuted
            )
            Spacer(modifier = Modifier.weight(1f))
        } else if (items.isNotEmpty()) {
            // Resumen
            val selectedCount = items.count { it.isSelected }
            val duplicateCount = items.count { it.isDuplicate }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$selectedCount SELECCIONADOS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Accent,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
                if (duplicateCount > 0) {
                    Text(
                        text = "$duplicateCount DUPLICADOS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Warning,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(items) { index, item ->
                    BankMovementRow(
                        item = item,
                        onToggleSelection = { viewModel.toggleSelection(index) },
                        onCategoryChange = { category -> viewModel.setCategory(index, category) }
                    )
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ─── Botón de importar ───
            Button(
                onClick = { viewModel.importSelected() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = selectedCount > 0 && !isImporting,
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Accent,
                    contentColor = Ink,
                    disabledContainerColor = InkSurfaceHigh,
                    disabledContentColor = MistMuted
                )
            ) {
                Text(
                    text = if (isImporting) "IMPORTANDO..." else "IMPORTAR $selectedCount MOVIMIENTOS",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
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
}

@Composable
private fun BankMovementRow(
    item: BankImportItem,
    onToggleSelection: () -> Unit,
    onCategoryChange: (ExpenseCategory) -> Unit
) {
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .background(if (item.isDuplicate) InkSurfaceHigh else InkSurface)
            .clickable { onToggleSelection() }
    ) {
        // Franja lateral
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(
                    when {
                        item.isDuplicate -> Warning
                        item.isSelected -> Accent
                        else -> MistMuted.copy(alpha = 0.3f)
                    }
                )
        )

        // Checkbox
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .align(Alignment.CenterVertically)
        ) {
            Checkbox(
                checked = item.isSelected,
                onCheckedChange = { onToggleSelection() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Accent,
                    uncheckedColor = MistMuted,
                    checkmarkColor = Ink
                )
            )
        }

        // Contenido
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            Text(
                text = item.movement.concept,
                color = Mist,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatDate(item.movement.date),
                    color = MistMuted,
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(8.dp))

                // Selector de categoría
                Box {
                    Text(
                        text = item.category.displayName.uppercase(),
                        color = AccentCyan,
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable { categoryMenuExpanded = true }
                            .padding(vertical = 2.dp)
                    )
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

                if (item.isDuplicate) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DUPLICADO",
                        color = Warning,
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Importe
        Text(
            text = String.format(Locale.getDefault(), "-%.2f€", item.movement.amountCents / 100.0),
            color = Danger,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(end = 12.dp)
        )
    }
}

private fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}
