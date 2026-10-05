package com.easyreceiptanalyzer.ui.manual

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.easyreceiptanalyzer.data.ExpenseCategory
import com.easyreceiptanalyzer.data.ProductCategory
import com.easyreceiptanalyzer.ui.theme.Accent
import com.easyreceiptanalyzer.ui.theme.AccentCyan
import com.easyreceiptanalyzer.ui.theme.Danger
import com.easyreceiptanalyzer.ui.theme.Ink
import com.easyreceiptanalyzer.ui.theme.InkSurface
import com.easyreceiptanalyzer.ui.theme.Mist
import com.easyreceiptanalyzer.ui.theme.MistMuted
import com.easyreceiptanalyzer.ui.theme.Warning
import java.util.Calendar
import java.util.Locale

@Composable
fun ManualReceiptScreen(
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    viewModel: ManualReceiptViewModel = hiltViewModel()
) {
    val isSaving by viewModel.isSaving.collectAsState()
    val saved by viewModel.saved.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var entryType by remember { mutableStateOf(ManualEntryType.TICKET) }
    var storeName by remember { mutableStateOf("") }
    var totalInput by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ProductCategory.OTROS) }
    var selectedExpenseCategory by remember { mutableStateOf(ExpenseCategory.OTROS) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    var selectedDateMillis by remember { mutableStateOf(Calendar.getInstance().timeInMillis) }
    val datePickerContext = LocalContext.current

    val openDatePicker: () -> Unit = {
        val cal = Calendar.getInstance()
        cal.timeInMillis = selectedDateMillis

        DatePickerDialog(
            datePickerContext,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance()
                newCal.clear()
                newCal.set(year, month, dayOfMonth, 0, 0, 0)
                selectedDateMillis = newCal.timeInMillis
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    LaunchedEffect(saved) {
        if (saved) {
            viewModel.reset()
            onSaved()
        }
    }

    val totalParsed = totalInput.replace(",", ".").toDoubleOrNull()
    val canSave = totalParsed != null && totalParsed > 0 && !isSaving

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .statusBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .width(56.dp)
                .height(3.dp)
                .background(AccentCyan)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "04 / AÑADIR MANUAL",
            style = MaterialTheme.typography.labelLarge,
            color = AccentCyan,
            letterSpacing = 4.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when (entryType) {
                ManualEntryType.TICKET -> "TICKET"
                ManualEntryType.GENERAL -> "GASTO"
                ManualEntryType.INCOME -> "INGRESO"
            },
            style = MaterialTheme.typography.displaySmall,
            color = Mist,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Para tickets que no tienes a mano o son demasiado simples.",
            style = MaterialTheme.typography.bodySmall,
            color = MistMuted,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // ─── Selector de tipo de gasto ───
        Text(
            text = "TIPO DE GASTO",
            style = MaterialTheme.typography.labelSmall,
            color = MistMuted,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(InkSurface)
        ) {
            ManualEntryType.entries.forEach { type ->
                val selected = type == entryType
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            entryType = type
                            // Al cambiar de tipo, reseteamos la categoría
                            if (type == ManualEntryType.TICKET) {
                                selectedCategory = ProductCategory.OTROS
                            } else {
                                selectedExpenseCategory = ExpenseCategory.OTROS
                            }
                        }
                        .background(if (selected) Ink else InkSurface)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = type.label,
                        color = if (selected) AccentCyan else MistMuted,
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 0.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(AccentCyan)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ─── Tienda / Concepto ───
        Text(
            text = when (entryType) {
                ManualEntryType.TICKET -> "TIENDA"
                ManualEntryType.GENERAL -> "CONCEPTO"
                ManualEntryType.INCOME -> "DESCRIPCIÓN"
            },
            style = MaterialTheme.typography.labelSmall,
            color = MistMuted,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = storeName,
            onValueChange = { storeName = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = {
                Text(
                    text = when (entryType) {
                        ManualEntryType.TICKET -> "Ej: Frutería del barrio"
                        ManualEntryType.GENERAL -> "Ej: Alquiler piso"
                        ManualEntryType.INCOME -> "Ej: Propinas semana"
                    },
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

        Spacer(modifier = Modifier.height(20.dp))

        // ─── Total ───
        Text(
            text = "IMPORTE TOTAL (€)",
            style = MaterialTheme.typography.labelSmall,
            color = MistMuted,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = totalInput,
            onValueChange = { totalInput = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Ej: 4.50", color = MistMuted) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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

        Spacer(modifier = Modifier.height(20.dp))

        // ─── Fecha ───
        Text(
            text = "FECHA",
            style = MaterialTheme.typography.labelSmall,
            color = MistMuted,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(InkSurface)
                .clickable { openDatePicker() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatDateForDisplay(selectedDateMillis),
                color = AccentCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "›",
                color = AccentCyan,
                fontWeight = FontWeight.Light,
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ─── Categoría ───
        if (entryType != ManualEntryType.INCOME) {
            Text(
                text = "CATEGORÍA",
                style = MaterialTheme.typography.labelSmall,
                color = MistMuted,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(InkSurface)
                        .clickable { categoryMenuExpanded = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (entryType == ManualEntryType.TICKET) {
                            selectedCategory.displayName.uppercase()
                        } else {
                            selectedExpenseCategory.displayName.uppercase()
                        },
                        color = Mist,
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
                    if (entryType == ManualEntryType.TICKET) {
                        ProductCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.displayName) },
                                onClick = {
                                    selectedCategory = category
                                    categoryMenuExpanded = false
                                }
                            )
                        }
                    } else {
                        ExpenseCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.displayName) },
                                onClick = {
                                    selectedExpenseCategory = category
                                    categoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // ─── Notas ───
        Text(
            text = "NOTAS (OPCIONAL)",
            style = MaterialTheme.typography.labelSmall,
            color = MistMuted,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            placeholder = { Text("Ej: tomates, lechuga, cebollas", color = MistMuted) },
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

        errorMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Error: $it",
                color = Danger,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ─── Botón guardar ───
        Button(
            onClick = {
                val total = totalParsed ?: return@Button
                when (entryType) {
                    ManualEntryType.TICKET -> viewModel.saveManualReceipt(
                        storeName = storeName,
                        totalEuros = total,
                        category = selectedCategory,
                        notes = notes,
                        dateMillis = selectedDateMillis
                    )
                    ManualEntryType.GENERAL -> viewModel.saveManualGeneralExpense(
                        concept = storeName,
                        totalEuros = total,
                        category = selectedExpenseCategory,
                        notes = notes,
                        dateMillis = selectedDateMillis
                    )
                    ManualEntryType.INCOME -> viewModel.saveManualIncome(
                        concept = storeName,
                        totalEuros = total,
                        notes = notes,
                        dateMillis = selectedDateMillis
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = canSave,
            shape = RoundedCornerShape(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = when (entryType) {
                    ManualEntryType.TICKET -> Accent
                    ManualEntryType.GENERAL -> Warning
                    ManualEntryType.INCOME -> AccentCyan
                },
                contentColor = Ink,
                disabledContainerColor = InkSurface,
                disabledContentColor = MistMuted
            )
        ) {
            Text(
                text = if (isSaving) "GUARDANDO..." else "GUARDAR",
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

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

private fun formatDateForDisplay(millis: Long): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = millis
    return String.format(
        Locale.getDefault(),
        "%02d/%02d/%04d",
        cal.get(Calendar.DAY_OF_MONTH),
        cal.get(Calendar.MONTH) + 1,
        cal.get(Calendar.YEAR)
    )
}
