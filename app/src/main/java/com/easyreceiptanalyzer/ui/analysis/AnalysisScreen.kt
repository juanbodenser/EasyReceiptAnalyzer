package com.easyreceiptanalyzer.ui.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.easyreceiptanalyzer.data.ExpenseCategory
import com.easyreceiptanalyzer.data.ProductCategory
import com.easyreceiptanalyzer.data.local.MonthlyBalanceEntity
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
import kotlin.math.abs

private val MONTH_NAMES = listOf(
    "ENERO", "FEBRERO", "MARZO", "ABRIL", "MAYO", "JUNIO",
    "JULIO", "AGOSTO", "SEPTIEMBRE", "OCTUBRE", "NOVIEMBRE", "DICIEMBRE"
)

@Composable
fun AnalysisScreen(
    onBackClick: () -> Unit,
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val categoryDetail by viewModel.categoryDetail.collectAsState()
    val expenseCategoryDetail by viewModel.expenseCategoryDetail.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        // ─── Cabecera ───
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(3.dp)
                .background(Warning)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "03 / ANÁLISIS",
            style = MaterialTheme.typography.labelLarge,
            color = Warning,
            letterSpacing = 4.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "MÉTRICAS",
            style = MaterialTheme.typography.displaySmall,
            color = Mist,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ─── Selector de mes ───
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(InkSurface)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { viewModel.previousMonth() }) {
                Text("◀", color = Warning, fontWeight = FontWeight.Bold)
            }
            Text(
                text = "${MONTH_NAMES[state.month]} ${state.year}",
                style = MaterialTheme.typography.titleMedium,
                color = Mist,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            TextButton(onClick = { viewModel.nextMonth() }) {
                Text("▶", color = Warning, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ─── Pestañas de modo ───
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(InkSurface)
        ) {
            AnalysisMode.entries.forEach { mode ->
                val selected = mode == state.mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.setMode(mode) }
                        .background(if (selected) Ink else InkSurface)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.label,
                        color = if (selected) Warning else MistMuted,
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 0.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(Warning)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (state.isLoading) {
            CircularProgressIndicator(color = Warning)
        } else {
            when (state.mode) {
                AnalysisMode.EXPENSES -> ExpensesContent(
                    state = state,
                    onCategoryClick = { viewModel.openCategory(it) },
                    onExpenseCategoryClick = { viewModel.openExpenseCategory(it) }
                )
                AnalysisMode.SUMMARY -> SummaryContent(
                    state = state,
                    onCloseMonth = { viewModel.closeCurrentMonth() },
                    onReopenMonth = { viewModel.reopenCurrentMonth() },
                    onSortChange = { viewModel.setHistorySortMode(it) }
                )
            }
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

    // ─── Modal de detalle de categoría de supermercado ───
    categoryDetail?.let { detail ->
        AlertDialog(
            onDismissRequest = { viewModel.closeCategory() },
            title = {
                Text(
                    safeCategoryName(detail.categoryName).uppercase(),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Mist
                )
            },
            text = {
                if (detail.products.isEmpty()) {
                    Text("No hay productos en esta categoría.", color = MistMuted)
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        items(detail.products) { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(InkSurface)
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        p.name,
                                        fontWeight = FontWeight.Medium,
                                        color = Mist
                                    )
                                    Text(
                                        text = "${p.veces} ${if (p.veces == 1) "vez" else "veces"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MistMuted
                                    )
                                }
                                Text(formatCents(p.totalCents), fontWeight = FontWeight.Bold, color = Accent)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.closeCategory() }) {
                    Text("CERRAR", color = Warning, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        )
    }

    // ─── Modal de detalle de categoría de gasto general ───
    expenseCategoryDetail?.let { detail ->
        AlertDialog(
            onDismissRequest = { viewModel.closeExpenseCategory() },
            title = {
                Text(
                    safeExpenseCategoryName(detail.categoryName).uppercase(),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Mist
                )
            },
            text = {
                if (detail.movements.isEmpty()) {
                    Text("No hay movimientos en esta categoría.", color = MistMuted)
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(detail.movements) { m ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(InkSurface)
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = m.concept.uppercase(),
                                            fontWeight = FontWeight.Medium,
                                            color = Mist,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = formatShortDate(m.date),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MistMuted
                                        )
                                    }
                                    Text(
                                        text = formatCents(m.amountCents),
                                        fontWeight = FontWeight.Bold,
                                        color = Warning
                                    )
                                }
                                if (!m.notes.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = m.notes,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AccentCyan,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.closeExpenseCategory() }) {
                    Text("CERRAR", color = Warning, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        )
    }
}

@Composable
private fun SectionTitle(title: String, rightText: String = "GASTO") {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MistMuted,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = rightText,
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
}

@Composable
private fun ColumnScope.ExpensesContent(
    state: AnalysisUiState,
    onCategoryClick: (String) -> Unit,
    onExpenseCategoryClick: (String) -> Unit
) {
    val grocery = state.summary
    val general = state.generalSummary
    val totalGrocery = grocery?.totalCents ?: 0L
    val totalGeneral = general?.totalCents ?: 0L
    val totalAll = totalGrocery + totalGeneral
    val numAll = (grocery?.numTickets ?: 0) + (general?.numMovements ?: 0)

    if (totalAll == 0L) {
        Text(
            text = "No hay gastos guardados en este mes.",
            style = MaterialTheme.typography.bodyLarge,
            color = MistMuted
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().weight(1f),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // ─── Tarjeta resumen general ───
        item {
            TotalPanel(
                totalCents = totalAll,
                numTickets = numAll,
                avgCents = if (totalAll > 0 && numAll > 0) totalAll / numAll else 0L
            )
        }

        // ─── Bloque SUPERMERCADO ───
        if (grocery != null && grocery.numTickets > 0) {
            item {
                Spacer(modifier = Modifier.height(28.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "SUPERMERCADO  ·  ${formatCents(totalGrocery)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MistMuted,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "GASTO",
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
            }
            items(state.categories) { cat ->
                CategoryRow(
                    categoryName = cat.category,
                    totalCents = cat.totalCents,
                    numItems = cat.numItems,
                    monthTotalCents = totalGrocery,
                    onClick = { onCategoryClick(cat.category) }
                )
            }
        }

        // ─── Bloque GASTOS GENERALES ───
        if (general != null && general.numMovements > 0) {
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "GASTOS GENERALES  ·  ${formatCents(totalGeneral)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MistMuted,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "GASTO",
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
            }
            items(state.expenseCategories) { cat ->
                ExpenseCategoryRow(
                    categoryName = cat.category,
                    totalCents = cat.totalCents,
                    numMovements = cat.numMovements,
                    monthTotalCents = totalGeneral,
                    onClick = { onExpenseCategoryClick(cat.category) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun ColumnScope.SummaryContent(
    state: AnalysisUiState,
    onCloseMonth: () -> Unit,
    onReopenMonth: () -> Unit,
    onSortChange: (HistorySortMode) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().weight(1f),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // ─── Balance del mes ───
        item {
            BalanceCard(
                incomeCents = state.balanceIncomeCents,
                expenseCents = state.balanceExpenseCents,
                balanceCents = state.balanceCents,
                closed = state.balanceClosed
            )
        }

        // ─── Botón cerrar/reabrir ───
        item {
            Spacer(modifier = Modifier.height(16.dp))
            if (state.balanceClosed) {
                Button(
                    onClick = onReopenMonth,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = InkSurfaceHigh,
                        contentColor = Mist
                    )
                ) {
                    Text(
                        "REABRIR MES",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            } else {
                Button(
                    onClick = onCloseMonth,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Accent,
                        contentColor = Ink
                    )
                ) {
                    Text(
                        "CERRAR MES",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        // ─── Desglose de ingresos del mes ───
        if (state.incomeDetails.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "INGRESOS DEL MES",
                        style = MaterialTheme.typography.labelLarge,
                        color = MistMuted,
                        letterSpacing = 3.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "IMPORTE",
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
            }
            items(state.incomeDetails) { income ->
                IncomeRow(income.concept, income.date, income.amountCents, income.notes)
            }
        }

        // ─── Histórico de balances ───
        if (state.closedBalances.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle("HISTÓRICO DE BALANCES", rightText = "BALANCE")
                    HistorySortSelector(
                        currentMode = state.historySortMode,
                        onModeChange = { onSortChange(it) }
                    )
                }
            }

            val sortedBalances = when (state.historySortMode) {
                HistorySortMode.DATE_DESC -> state.closedBalances.sortedByDescending { it.yearMonth }
                HistorySortMode.DATE_ASC -> state.closedBalances.sortedBy { it.yearMonth }
                HistorySortMode.AMOUNT_DESC -> state.closedBalances.sortedByDescending { it.balanceCents }
                HistorySortMode.AMOUNT_ASC -> state.closedBalances.sortedBy { it.balanceCents }
            }

            items(sortedBalances) { b ->
                ClosedBalanceRow(b)
            }

            // ─── Total acumulado ───
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(InkSurface)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL ACUMULADO",
                        color = MistMuted,
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatSignedCents(state.totalSavings),
                        color = if (state.totalSavings >= 0) Accent else Danger,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}



// ─────────────────────────────────────────────
// Panel del total (el protagonista de la pantalla)
// ─────────────────────────────────────────────
@Composable
private fun TotalPanel(totalCents: Long, numTickets: Int, avgCents: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicHeightHelper())
            .background(InkSurface)
    ) {
        Box(
            modifier = Modifier
                .width(6.dp)
                .fillMaxHeight()
                .background(Accent)
        )
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "GASTO TOTAL DEL MES",
                style = MaterialTheme.typography.labelMedium,
                color = MistMuted,
                letterSpacing = 3.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatCents(totalCents),
                style = MaterialTheme.typography.displayMedium,
                color = Accent,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MistMuted.copy(alpha = 0.3f))
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "TICKETS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MistMuted,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "$numTickets",
                        style = MaterialTheme.typography.titleLarge,
                        color = Mist,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "TICKET MEDIO",
                        style = MaterialTheme.typography.labelSmall,
                        color = MistMuted,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        formatCents(avgCents),
                        style = MaterialTheme.typography.titleLarge,
                        color = Mist,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun GeneralTotalPanel(totalCents: Long, numMovements: Int, avgMovementCents: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(InkSurface)
    ) {
        Box(
            modifier = Modifier
                .width(6.dp)
                .height(160.dp)
                .background(Warning)
        )
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "GASTO TOTAL DEL MES",
                style = MaterialTheme.typography.labelMedium,
                color = MistMuted,
                letterSpacing = 3.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatCents(totalCents),
                style = MaterialTheme.typography.displayMedium,
                color = Warning,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MistMuted.copy(alpha = 0.3f))
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "MOVIMIENTOS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MistMuted,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "$numMovements",
                        style = MaterialTheme.typography.titleLarge,
                        color = Mist,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "MEDIA",
                        style = MaterialTheme.typography.labelSmall,
                        color = MistMuted,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        formatCents(avgMovementCents),
                        style = MaterialTheme.typography.titleLarge,
                        color = Mist,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun BalanceCard(
    incomeCents: Long,
    expenseCents: Long,
    balanceCents: Long,
    closed: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(InkSurface)
    ) {
        Box(
            modifier = Modifier
                .width(6.dp)
                .height(180.dp)
                .background(if (balanceCents >= 0) Accent else Danger)
        )
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = if (closed) "BALANCE CERRADO" else "BALANCE DEL MES",
                style = MaterialTheme.typography.labelMedium,
                color = MistMuted,
                letterSpacing = 3.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatSignedCents(balanceCents),
                style = MaterialTheme.typography.displayMedium,
                color = if (balanceCents >= 0) Accent else Danger,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MistMuted.copy(alpha = 0.3f))
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "INGRESOS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MistMuted,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "+ " + formatCents(incomeCents),
                        style = MaterialTheme.typography.titleMedium,
                        color = Accent,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "GASTOS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MistMuted,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "- " + formatCents(expenseCents),
                        style = MaterialTheme.typography.titleMedium,
                        color = Danger,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ClosedBalanceRow(balance: MonthlyBalanceEntity) {
    val monthName = MONTH_NAMES.getOrNull(balance.month) ?: "?"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = monthName.lowercase().replaceFirstChar { it.uppercase() } + " ${balance.year}",
                color = Mist,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = formatSignedCents(balance.balanceCents),
            color = if (balance.balanceCents >= 0) Accent else Danger,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

private fun formatSignedCents(cents: Long): String {
    val sign = if (cents >= 0) "+" else "-"
    val absCents = abs(cents)
    return "$sign " + formatCents(absCents)
}

// Helper para height(IntrinsicSize.Min) sin importar IntrinsicSize
@Composable
private fun IntrinsicHeightHelper() = IntrinsicSize.Min

// ─────────────────────────────────────────────
// Fila de categoría (tabla comparativa)
// ─────────────────────────────────────────────
@Composable
private fun CategoryRow(
    categoryName: String,
    totalCents: Long,
    numItems: Int,
    monthTotalCents: Long,
    onClick: () -> Unit
) {
    val displayName = safeCategoryName(categoryName).uppercase()
    val fraction = if (monthTotalCents > 0) totalCents.toFloat() / monthTotalCents else 0f
    val percent = (fraction * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nombre de categoría
            Text(
                text = displayName,
                color = Mist,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            // Importe
            Text(
                text = formatCents(totalCents),
                color = Accent,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            // Chevron
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "›",
                color = MistMuted,
                fontWeight = FontWeight.Light,
                style = MaterialTheme.typography.titleLarge
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Barra fina con % y nº productos a los lados
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$percent%",
                color = MistMuted,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(40.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .background(MistMuted.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction)
                        .background(Warning)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "$numItems",
                color = MistMuted,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(24.dp)
            )
        }
    }
}

@Composable
private fun ExpenseCategoryRow(
    categoryName: String,
    totalCents: Long,
    numMovements: Int,
    monthTotalCents: Long,
    onClick: () -> Unit
) {
    val displayName = safeExpenseCategoryName(categoryName).uppercase()
    val fraction = if (monthTotalCents > 0) totalCents.toFloat() / monthTotalCents else 0f
    val percent = (fraction * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayName,
                color = Mist,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = formatCents(totalCents),
                color = Warning,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "›",
                color = MistMuted,
                fontWeight = FontWeight.Light,
                style = MaterialTheme.typography.titleLarge
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$percent%",
                color = MistMuted,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(40.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .background(MistMuted.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction)
                        .background(Warning)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "$numMovements",
                color = MistMuted,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(24.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────
// Fila del top 5 productos
// ─────────────────────────────────────────────
@Composable
private fun TopProductRow(name: String, veces: Int, totalCents: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name.uppercase(),
                color = Mist,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium,
                letterSpacing = 0.5.sp,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$veces ${if (veces == 1) "VEZ" else "VECES"}",
                style = MaterialTheme.typography.labelSmall,
                color = MistMuted,
                letterSpacing = 1.sp
            )
        }
        Text(
            text = formatCents(totalCents),
            color = AccentCyan,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun MovementRow(concept: String, date: Long, amountCents: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = concept.uppercase(),
                color = Mist,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium,
                letterSpacing = 0.5.sp,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formatShortDate(date),
                style = MaterialTheme.typography.labelSmall,
                color = MistMuted,
                letterSpacing = 1.sp
            )
        }
        Text(
            text = formatCents(amountCents),
            color = Warning,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

private fun safeExpenseCategoryName(name: String): String =
    runCatching { ExpenseCategory.valueOf(name).displayName }
        .getOrDefault(name)

private fun formatShortDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}

private fun formatCents(cents: Long): String =
    String.format(Locale.getDefault(), "€%.2f", cents / 100.0)

private fun safeCategoryName(name: String): String =
    runCatching { ProductCategory.valueOf(name).displayName }.getOrDefault(name)

@Composable
private fun IncomeRow(concept: String, date: Long, amountCents: Long, notes: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = concept.uppercase(),
                    color = Mist,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium,
                    letterSpacing = 0.5.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatShortDate(date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MistMuted,
                    letterSpacing = 1.sp
                )
            }
            Text(
                text = "+ " + formatCents(amountCents),
                color = Accent,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        if (!notes.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notes,
                style = MaterialTheme.typography.labelSmall,
                color = AccentCyan,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun HistorySortSelector(
    currentMode: HistorySortMode,
    onModeChange: (HistorySortMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentMode.label,
                style = MaterialTheme.typography.labelSmall,
                color = AccentCyan,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "▾",
                color = AccentCyan,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelSmall
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            HistorySortMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(mode.label) },
                    onClick = {
                        onModeChange(mode)
                        expanded = false
                    }
                )
            }
        }
    }
}
