package com.easyreceiptanalyzer.ui.analysis

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.easyreceiptanalyzer.data.ProductCategory
import com.easyreceiptanalyzer.ui.theme.Accent
import com.easyreceiptanalyzer.ui.theme.AccentCyan
import com.easyreceiptanalyzer.ui.theme.Ink
import com.easyreceiptanalyzer.ui.theme.InkSurface
import com.easyreceiptanalyzer.ui.theme.Mist
import com.easyreceiptanalyzer.ui.theme.MistMuted
import com.easyreceiptanalyzer.ui.theme.Warning
import java.util.Locale

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

        Spacer(modifier = Modifier.height(20.dp))

        if (state.isLoading) {
            CircularProgressIndicator(color = Warning)
        } else {
            val summary = state.summary
            if (summary == null || summary.numTickets == 0) {
                Text(
                    text = "No hay tickets guardados en este mes.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MistMuted
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // ─── Panel del total ───
                    item {
                        TotalPanel(
                            totalCents = summary.totalCents,
                            numTickets = summary.numTickets,
                            avgCents = summary.avgTicketCents
                        )
                    }

                    // ─── Cabecera de la tabla ───
                    item {
                        Spacer(modifier = Modifier.height(28.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "CATEGORÍA",
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

                    // ─── Filas de categorías ───
                    items(state.categories) { cat ->
                        CategoryRow(
                            categoryName = cat.category,
                            totalCents = cat.totalCents,
                            numItems = cat.numItems,
                            monthTotalCents = summary.totalCents,
                            onClick = { viewModel.openCategory(cat.category) }
                        )
                    }

                    // ─── Separador + Top 5 ───
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "TOP 5 PRODUCTOS",
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

                    items(state.topProducts) { p ->
                        TopProductRow(p.name, p.veces, p.totalCents)
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
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

    // ─── Modal de detalle de categoría ───
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

// Helper para height(IntrinsicSize.Min) sin importar IntrinsicSize
@Composable
private fun IntrinsicHeightHelper() = androidx.compose.foundation.layout.IntrinsicSize.Min

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

private fun formatCents(cents: Long): String =
    String.format(Locale.getDefault(), "€%.2f", cents / 100.0)

private fun safeCategoryName(name: String): String =
    runCatching { ProductCategory.valueOf(name).displayName }.getOrDefault(name)